package ru.abondin.hreasy.platform.service.udr;

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
public class JuniorSecurityValidator {
    public enum ViewFilter {
        /**
         * Request all juniors
         */
        ALL,
        /**
         * Request only if I mentor or if I was a mentor
         */
        MY
    }

    public Mono<ViewFilter> get(AuthContext authContext) {
        return Mono.defer(() -> {
            ViewFilter filter;
            if (authContext.getAuthorities().contains("admin_junior_reg")) {
                filter = ViewFilter.ALL;
            } else if (authContext.getAuthorities().contains("access_junior_reg")) {
                filter = ViewFilter.MY;
            } else {
                return null;
            }
            return Mono.just(filter);
        }).flatMap(r -> {
            if (r == null) {
                return Mono.error(new AccessDeniedException("No access to view junior registry"));
            } else {
                return Mono.just(r);
            }
        });
    }

    public Mono<Boolean> add(AuthContext auth) {
        return Mono.defer(() -> {
            if (auth.getAuthorities().contains("admin_junior_reg")) {
                return Mono.just(true);
            } else {
                return Mono.just(false);
            }
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission admin_junior_reg can add juniors to registry"));
        });
    }
}
