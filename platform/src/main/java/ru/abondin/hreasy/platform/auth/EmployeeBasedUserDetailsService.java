package ru.abondin.hreasy.platform.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.repo.employee.EmployeeAuthDomainService;
import ru.abondin.hreasy.platform.sec.UserDetailsWithEmployeeInfo;

@Service
@RequiredArgsConstructor
public class EmployeeBasedUserDetailsService {

    private final DbAuthoritiesPopulator authoritiesPopulator;
    private final EmployeeAuthDomainService employeeAuthDomainService;

    public Mono<UserDetails> findByUsername(String username, AuthContext.LoginType loginType) {
        return employeeAuthDomainService.findIdByEmail(username)
                .switchIfEmpty(Mono.error(new BusinessError("errors.no.employee.found", username)))
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
                                                            employeeAuthInfoEntry.getAccessibleBas(),
                                                            employeeAuthInfoEntry.getAccessibleProjects(),
                                                            loginType.getValue());
                                                    return user;
                                                }
                                        )
                );
    }
}
