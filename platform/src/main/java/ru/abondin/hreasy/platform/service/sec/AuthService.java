package ru.abondin.hreasy.platform.service.sec;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.repo.sec.SecLoginHistoryEntry;
import ru.abondin.hreasy.platform.repo.sec.SecLoginHistoryRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;

import java.util.Optional;
import java.util.UUID;

/**
 * Simple wrapper on {@link ru.abondin.hreasy.platform.auth.AuthHandler} which persist all login history
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthHandler authHandler;
    private final SecLoginHistoryRepo loginHistoryRepo;
    private final DateTimeService dateTimeService;

    public Mono<AuthHandler.LoginResponse> login(UsernamePasswordAuthenticationToken authenticationToken, WebSession webSession) {
        final String logAttemtId = UUID.randomUUID().toString();
        log.info("Logging attempt {}. User={}", logAttemtId, authenticationToken.getPrincipal());
        var loginHistory = new SecLoginHistoryEntry();
        loginHistory.setLogin(authenticationToken.getName());
        loginHistory.setLoginTime(dateTimeService.now());
        return authHandler.login(authenticationToken, webSession)
                .flatMap(loginResponse -> {
                    var employeeId = Optional.ofNullable(loginResponse.getCurrentUser())
                            .map(AuthHandler.CurrentUserDto::getEmployee)
                            .map(AuthContext.EmployeeInfo::getEmployeeId)
                            .orElse(null);
                    log.info("Logging attempt {} successfully completed. Logged employeeId={}", logAttemtId, employeeId);
                    loginHistory.setLoggedEmployeeId(employeeId);
                    return loginHistoryRepo.save(loginHistory).map((r) -> loginResponse);
                })
                .onErrorResume((ex) -> {
                    log.info("Logging attempt {} failed", logAttemtId, ex);
                    // Replace to avoid PostgresqlBadGrammarException: [22021] invalid byte sequence for encoding "UTF8": 0x00
                    loginHistory.setError(ex.getMessage()==null? null : ex.getMessage().replaceAll("\u0000", ""));
                    return loginHistoryRepo.save(loginHistory).flatMap((r) -> Mono.error(ex));
                });
    }

    public Mono<AuthHandler.LogoutResponse> logout(WebSession webSession) {
        return authHandler.logout(webSession);
    }
}
