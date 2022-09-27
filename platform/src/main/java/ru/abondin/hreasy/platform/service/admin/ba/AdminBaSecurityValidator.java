package ru.abondin.hreasy.platform.service.admin.ba;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;

/**
 * Validate security rules to work in admin area
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBaSecurityValidator {

    private final ProjectHierarchyAccessor projectHierarchyService;
    private final DictProjectRepo projectRepo;

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

    public Mono<Boolean> validateAdminBaAssignments(AuthContext auth, int baId) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("edit_business_account")) {
                // TODO Check that employee has access to the business account with given baId
                if (!auth.getAuthorities().contains("admin_ba_assignment")) {
                    return Mono.error(new AccessDeniedException("Only user with permission edit_business_account or admin_ba_assignment with specific business account can create" +
                            " add or update business accounts assignments"));
                }
            }
            return Mono.just(true);
        });
    }
}
