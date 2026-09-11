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
    public static final String READ_PERMISSION = "resource_allocation_read";
    public static final String WRITE_PERMISSION = "resource_allocation_write";
    public static final String ADMIN_PERMISSION = "resource_allocation_admin";

    private final ProjectHierarchyAccessor projectHierarchyAccessor;

    /**
     * Requires access to the resource allocation feature.
     */
    public Mono<Boolean> validateCanReadAllocations(AuthContext auth) {
        return auth.getAuthorities().contains(READ_PERMISSION)
                ? Mono.just(true)
                : Mono.error(new AccessDeniedException("Missing permission " + READ_PERMISSION));
    }

    /**
     * Requires permission to change allocations.
     */
    public Mono<Boolean> validateCanWriteAllocations(AuthContext auth) {
        return canWriteAllocations(auth)
                ? Mono.just(true)
                : Mono.error(new AccessDeniedException("Missing permission " + WRITE_PERMISSION));
    }

    public boolean canWriteAllocations(AuthContext auth) {
        return auth.getAuthorities().contains(WRITE_PERMISSION);
    }

    /**
     * Returns whether the user may edit allocations for a managed or explicitly accessible project.
     */
    public boolean canWriteProject(AuthContext auth, ResourceAllocationProjectView project) {
        return canWriteAllocations(auth) && canReadProject(auth, project);
    }

    /** Project scope is shared by readers and writers; reading does not require write permission. */
    public boolean canReadProject(AuthContext auth, ResourceAllocationProjectView project) {
        return projectHierarchyAccessor.hasProjectAccess(auth, null,
                new ProjectHierarchyAccessor.ProjectInfo(project.id(), project.departmentId(), project.baId()));
    }

    /**
     * Requires permission to close and reopen allocation periods.
     */
    public Mono<Boolean> validateAdmin(AuthContext auth) {
        return auth.getAuthorities().contains(ADMIN_PERMISSION)
                ? Mono.just(true)
                : Mono.error(new AccessDeniedException("Missing permission " + ADMIN_PERMISSION));
    }
}
