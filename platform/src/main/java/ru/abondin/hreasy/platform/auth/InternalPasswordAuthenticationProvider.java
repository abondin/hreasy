package ru.abondin.hreasy.platform.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractUserDetailsReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.employee.EmployeeAuthDomainService;
import ru.abondin.hreasy.platform.sec.UserDetailsWithEmployeeInfo;

/**
 * Login with internal password from local database
 */
@RequiredArgsConstructor
@Slf4j
public class InternalPasswordAuthenticationProvider extends AbstractUserDetailsReactiveAuthenticationManager {
    private final DbAuthoritiesPopulator authoritiesPopulator;
    private final EmployeeAuthDomainService employeeAuthDomainService;


    @Override
    protected Mono<UserDetails> retrieveUser(String username) {
        return employeeAuthDomainService.findIdByEmail(username)
                .flatMap(
                        employeeAuthInfoEntry ->
                                employeeAuthDomainService.getInternalPassword(employeeAuthInfoEntry.getId())
                                        .flatMap(password ->
                                                authoritiesPopulator.getGrantedAuthorities(username).collectList()
                                                        .map(
                                                                authorities -> {
                                                                    var user = new UserDetailsWithEmployeeInfo(new User(
                                                                            username,
                                                                            password,
                                                                            authorities),
                                                                            employeeAuthInfoEntry.getId(),
                                                                            employeeAuthInfoEntry.getDepartmentId(),
                                                                            employeeAuthInfoEntry.getCurrentProjectId(),
                                                                            employeeAuthInfoEntry.getAccessibleDepartments(),
                                                                            employeeAuthInfoEntry.getAccessibleBas(),
                                                                            employeeAuthInfoEntry.getAccessibleProjects(),
                                                                            AuthContext.LoginType.INTERNAL.getValue());
                                                                    return user;
                                                                }
                                                        )
                                        ));
    }
}
