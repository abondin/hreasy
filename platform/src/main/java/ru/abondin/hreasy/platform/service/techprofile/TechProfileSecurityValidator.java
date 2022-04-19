package ru.abondin.hreasy.platform.service.techprofile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;

import java.util.List;

/**
 * Validate security rules to  update employee current project
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TechProfileSecurityValidator {

    private final ProjectHierarchyAccessor projectHierarchyService;

    public Mono<Boolean> validateUploadOrDeleteTechProfile(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (employeeId == auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.just(true);
            }
            if (!auth.getAuthorities().contains("techprofile_upload")) {
                return Mono.just(false);
            }
            return projectHierarchyService.isManager(auth, employeeId);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only logged in user or user with permission techprofile_upload can upload or delete tech profile"));
        });
    }

    public Mono<Boolean> validateDownloadTechProfile(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (employeeId == auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.just(true);
            }
            if (!auth.getAuthorities().contains("techprofile_download")) {
                return Mono.just(false);
            }
            return projectHierarchyService.isManager(auth, employeeId);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only logged in user or user with permission techprofile_download can download tech profile"));
        });
    }

}
