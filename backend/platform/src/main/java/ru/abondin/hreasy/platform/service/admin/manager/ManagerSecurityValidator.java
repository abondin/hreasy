package ru.abondin.hreasy.platform.service.admin.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.repo.manager.ManagerEntry;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;

import java.util.Objects;

/**
 * Validate security rules to admin managers
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ManagerSecurityValidator {

    private final AdminSecurityValidator adminSecurityValidator;

    public Mono<Boolean> validateCreateManagerForProject(AuthContext auth, DictProjectEntry entry) {
        return Mono.defer(() -> {
            if (auth.getAuthorities().contains("admin_managers")) {
                return Mono.just(true);
            }
            return adminSecurityValidator.validateUpdateProject(auth, entry);
        });
    }

    public Mono<Boolean> validateAdminManagers(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_managers")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_managers can see all managers"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateDeleteManager(AuthContext auth, ManagerEntry manager) {
        if (auth.getAuthorities().contains("admin_managers")
                || Objects.equals(manager.getCreatedBy(), auth.getEmployeeInfo().getEmployeeId())) {
            return Mono.just(true);
        }
        return Mono.error(new AccessDeniedException(
                "Only admin_managers or the employee who created the manager link can delete it"));
    }

    /**
     * Everyone can see manager of given project/ba/department
     *
     * @param auth
     * @param objectType
     * @param objectId
     * @return
     */
    public Mono<Boolean> validateViewManagers(AuthContext auth, String objectType, int objectId) {
        return Mono.just(true);
    }
}
