package ru.abondin.hreasy.platform.service.admin.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;

/**
 * Validate security rules to work in admin area
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDictValidator {


    public Mono<Boolean> validateAdminDepartment(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_department")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_department can admin department"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateAdminOrganizations(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_organization")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_organizations can admin organizations"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateAdminLevel(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_level")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_level can admin level"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateAdminPosition(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_position")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_department can admin position"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateAdminOfficeLocation(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_office_location")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_office_location can admin office location"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateAdminOffice(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_office")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_office can admin offices"));
            }
            return Mono.just(true);
        });
    }
}
