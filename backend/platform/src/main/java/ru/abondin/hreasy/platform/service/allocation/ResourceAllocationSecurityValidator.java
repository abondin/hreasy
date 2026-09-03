package ru.abondin.hreasy.platform.service.allocation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationProjectView;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;

/**
 * Enforces coarse allocation permissions and project-scoped edit access.
 */
@Component
@RequiredArgsConstructor
public class ResourceAllocationSecurityValidator {
    public static final String EDIT_PERMISSION = "resource_allocation_edit";
    public static final String EDIT_GLOBALLY_PERMISSION = "resource_allocation_edit_globally";
    public static final String MANAGE_PERIODS_PERMISSION = "resource_allocation_period_manage";

    private final ProjectHierarchyAccessor projectHierarchyAccessor;

    /**
     * Requires access to the resource allocation feature.
     */
    public Mono<Boolean> validateCanEditAllocations(AuthContext auth) {
        return auth.getAuthorities().contains(EDIT_PERMISSION)
                ? Mono.just(true)
                : Mono.error(new AccessDeniedException("Missing permission " + EDIT_PERMISSION));
    }

    /**
     * Returns whether the user may edit allocations for the supplied project hierarchy.
     */
    public boolean canEditProject(AuthContext auth, ResourceAllocationProjectView project) {
        return canEditGlobally(auth)
                || projectHierarchyAccessor.hasProjectAccess(auth, null,
                new ProjectHierarchyAccessor.ProjectInfo(project.id(), project.departmentId(), project.baId()));
    }

    /**
     * Returns whether the user may edit allocations outside their managed project line.
     */
    public boolean canEditGlobally(AuthContext auth) {
        return auth.getAuthorities().contains(EDIT_GLOBALLY_PERMISSION);
    }

    /**
     * Returns whether the user may close and reopen allocation periods.
     */
    public boolean canManagePeriods(AuthContext auth) {
        return auth.getAuthorities().contains(MANAGE_PERIODS_PERMISSION);
    }

    /**
     * Rejects a mutation when the user cannot edit the supplied project.
     */
    public void validateEditProject(AuthContext auth, ResourceAllocationProjectView project) {
        if (!canEditProject(auth, project)) {
            throw new AccessDeniedException("No access to resource allocation project " + project.id());
        }
    }

    /**
     * Requires permission to close and reopen allocation periods.
     */
    public Mono<Boolean> validateManagePeriods(AuthContext auth) {
        return canManagePeriods(auth)
                ? Mono.just(true)
                : Mono.error(new AccessDeniedException("Missing permission " + MANAGE_PERIODS_PERMISSION));
    }
}
