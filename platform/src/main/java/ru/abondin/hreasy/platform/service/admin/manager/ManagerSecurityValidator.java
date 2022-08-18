package ru.abondin.hreasy.platform.service.admin.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;

/**
 * Validate security rules to admin managers
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ManagerSecurityValidator {

    public Mono<Boolean> validateAdminManagers(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_managers")) {
                return Mono.just(false);
            }
            return Mono.just(true);
        });
    }
}
