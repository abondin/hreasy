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

    public Mono<UserDetails> findInternal(String username){
        return findNotDismissedByUsername(username, AuthContext.LoginType.INTERNAL, null);
    }
    public Mono<UserDetails> findForMasterPassword(String username){
        return findNotDismissedByUsername(username, AuthContext.LoginType.MASTER_PASSWORD, null);
    }

    public Mono<UserDetails> findForTelegram(String username, String telegramAccount){
        return findNotDismissedByUsername(username, AuthContext.LoginType.TELEGRAM_BOT_SERVICE, telegramAccount);
    }

    private Mono<UserDetails> findNotDismissedByUsername(String username, AuthContext.LoginType loginType, String telegramName) {
        return employeeAuthDomainService.findNotDismissedByUsername(username)
                .switchIfEmpty(Mono.error(new BusinessError("errors.no.employee.found", username)))
                .flatMap(
                        employeeAuthInfoEntry ->
                                authoritiesPopulator.getGrantedAuthorities(username).collectList()
                                        .map(
                                                authorities -> new UserDetailsWithEmployeeInfo(new User(
                                                        username,
                                                        "",
                                                        authorities),
                                                        employeeAuthInfoEntry.getId(),
                                                        employeeAuthInfoEntry.getDepartmentId(),
                                                        employeeAuthInfoEntry.getCurrentProjectId(),
                                                        employeeAuthInfoEntry.getAccessibleDepartments(),
                                                        employeeAuthInfoEntry.getAccessibleBas(),
                                                        employeeAuthInfoEntry.getAccessibleProjects(),
                                                        loginType.getValue(),
                                                        telegramName)
                                        )
                );
    }


}
