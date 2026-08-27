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
        return auth.getAuthorities().contains(EDIT_GLOBALLY_PERMISSION)
                || projectHierarchyAccessor.hasProjectAccess(auth, null,
                new ProjectHierarchyAccessor.ProjectInfo(project.id(), project.departmentId(), project.baId()));
    }

    /**
     * Rejects a mutation when the user cannot edit the supplied project.
     */
    public void validateEditProject(AuthContext auth, ResourceAllocationProjectView project) {
        if (!canEditProject(auth, project)) {
            throw new AccessDeniedException("No access to resource allocation project " + project.id());
        }
    }
}
