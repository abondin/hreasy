package ru.abondin.hreasy.platform.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;

import java.nio.file.AccessDeniedException;

/**
 * Validate security rules to get and update notifications
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSecurityValidator {

    public Mono<Boolean> validateCreateNewNotification(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_notifications")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_notifications can create new notification"));
            }
            return Mono.just(true);
        });
    }
}
