package ru.abondin.hreasy.platform.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.config.HrEasySecurityProps;
import ru.abondin.hreasy.platform.repo.employee.EmployeeAuthDomainService;
import ru.abondin.hreasy.platform.sec.UserDetailsWithEmployeeInfo;

/**
 * For development purposes only.
 * Allow any user with valid employee email with fixed master password
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MasterPasswordAuthenticationProvider implements ReactiveAuthenticationManager {
    private final HrEasySecurityProps securityProps;
    private final DbAuthoritiesPopulator authoritiesPopulator;
    private final EmployeeAuthDomainService employeeAuthDomainService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var inputAuth = (UsernamePasswordAuthenticationToken) authentication;
        var username = inputAuth.getName();
        var password = inputAuth.getCredentials();
        if (StringUtils.isBlank(securityProps.getMasterPassword()) || !securityProps.getMasterPassword().equals(password)) {
            // throw BadCredentialsException("Master password is invalid");
            // Move control to default LDAP provider
            return Mono.empty();
        }
        return employeeAuthDomainService.findIdByEmail(username)
                .flatMap(
                        employeeAuthInfoEntry ->
                                authoritiesPopulator.getGrantedAuthorities(username).collectList()
                                        .map(
                                                authorities -> {
                                                    var user = new UserDetailsWithEmployeeInfo(new User(
                                                            username,
                                                            "",
                                                            authorities),
                                                            employeeAuthInfoEntry.getId(),
                                                            employeeAuthInfoEntry.getDepartmentId(),
                                                            employeeAuthInfoEntry.getCurrentProjectId(),
                                                            employeeAuthInfoEntry.getAccessibleDepartments(),
                                                            employeeAuthInfoEntry.getAccessibleProjects());
                                                    var result = new UsernamePasswordAuthenticationToken(user, password, authorities);
                                                    result.setDetails(inputAuth.getDetails());
                                                    result.eraseCredentials();
                                                    return result;
                                                }
                                        )
                );
    }

}
