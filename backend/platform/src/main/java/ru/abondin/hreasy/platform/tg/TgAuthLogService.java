package ru.abondin.hreasy.platform.tg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.sec.UserDetailsWithEmployeeInfo;
import ru.abondin.hreasy.platform.service.DateTimeService;

/**
 * Save to the database daily unique chat bot activities for employee
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TgAuthLogService {
    private final DatabaseClient db;
    private final DateTimeService dateTimeService;

    @Transactional
    public Mono<Void> log(Authentication authentication) {
        var day = dateTimeService.now().toLocalDate();
        log.info("Logging via telegram bot {}", authentication);
        if (authentication.getPrincipal() instanceof UserDetailsWithEmployeeInfo userInfo) {
            return db.sql("""
                            insert into sec.tg_auth_log (employee_id, telegram_account, day, cnt)
                                values(:employeeId, :telegram, :day, 1)
                                on conflict(employee_id, telegram_account, day)
                                do update set cnt = sec.tg_auth_log.cnt + 1;
                            """)
                    .bind("employeeId", userInfo.getEmployeeId())
                    .bind("telegram", userInfo.getTelegramAccount())
                    .bind("day", day)
                    .then();
        } else {
            return Mono.error(new BadCredentialsException("UserDetailsWithEmployeeInfo expected as a principal"));
        }

    }

}
