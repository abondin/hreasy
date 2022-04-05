package ru.abondin.hreasy.platform.service.vacation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;

/**
 * Validate security rules to  update employee current project
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VacationSecurityValidator {

    public Mono<Boolean> validateCanViewOvertimes(AuthContext auth) {
        return Mono.defer(() -> {
            if (auth.getAuthorities().contains("vacation_view")) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission vacation_view can view all vacations"));
        });
    }

    public Mono<Boolean> validateCanEditOvertimes(AuthContext auth) {
        return Mono.defer(() -> {
            if (auth.getAuthorities().contains("vacation_edit")) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission vacation_edit can update all vacations"));
        });
    }

    public Mono<Boolean> validateCanExportOvertimes(AuthContext auth) {
        return Mono.defer(() -> {
            if (auth.getAuthorities().contains("vacation_edit")) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission vacation_edit can export vacations"));
        });
    }
}
