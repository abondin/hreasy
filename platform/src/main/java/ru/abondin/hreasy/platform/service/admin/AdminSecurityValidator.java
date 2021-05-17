package ru.abondin.hreasy.platform.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;

/**
 * Validate security rules to work in admin area
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSecurityValidator {

    private final ProjectHierarchyAccessor projectHierarchyService;

    public Mono<Boolean> validateCreateProject(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("create_project")) {
                return Mono.error(new AccessDeniedException("Only user with permission create_project can create new project"));
            }
            return Mono.just(true);
        });
    }


    public Mono<Boolean> validateUpdateProject(AuthContext auth, DictProjectEntry entry) {
        return Mono.defer(() -> {
            if (auth.getEmployeeInfo().getEmployeeId().equals(entry.getCreatedBy())) {
                return Mono.just(true);
            }
            if (!auth.getAuthorities().contains("update_project")) {
                return Mono.error(new AccessDeniedException("Only user with permission update_project or project creator" +
                        " can update new project"));
            }
            return Mono.just(true);
        });
    }


    public Mono<Boolean> validateFindAllProject(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("project_admin_area")) {
                return Mono.error(new AccessDeniedException("Only user with permission project_admin_area can access project admin area"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateGetEmployeeWithSecurity(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_users")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_users can view employee " +
                        "info with security info"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateUpdateUserRoles(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_users")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_users can update roles"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateAddOrUpdateBusinessAccount(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("edit_business_account")) {
                return Mono.error(new AccessDeniedException("Only user with permission edit_business_account can create" +
                        " add or update business accounts"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateGetBusinessAccountDetailed(AuthContext auth) {
        return validateAddOrUpdateBusinessAccount(auth);
    }

    public Mono<Boolean> validateUpdateBAPosition(AuthContext auth, int baId) {
        return validateAddOrUpdateBusinessAccount(auth);
    }

    public Mono<Boolean> validateEditArticle(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("edit_articles")) {
                return Mono.error(new AccessDeniedException("Only user with permission edit_articles can create" +
                        " or update article"));
            }
            return Mono.just(true);
        });
    }
}
