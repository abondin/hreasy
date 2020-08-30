package ru.abondin.hreasy.platform.service.overtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyService;

/**
 * Validate security rules to get and update overtimes
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OvertimeSecurityValidator {

    private final ProjectHierarchyService projectHierarchyService;

    public Mono<Boolean> validateEditOvertimeItem(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (employeeId == auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.just(true);
            }
            if (!auth.getAuthorities().contains("overtime_edit")) {
                return Mono.just(false);
            }
            return projectHierarchyService.isManager(auth, employeeId);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only logged in user or user with permission overtime_edit can update the overtime item"));
        });
    }

    public Mono<Boolean> validateViewOvertime(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (employeeId == auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.just(true);
            }
            if (!auth.getAuthorities().contains("overtime_view")) {
                return Mono.error(new AccessDeniedException("Only logged in user or user with permission overtime_view can view the overtime report"));
            }
            return Mono.just(true);
        });
    }


    public Mono<Boolean> validateViewOvertimeSummary(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("overtime_view")) {
                return Mono.error(new AccessDeniedException("Only user with permission overtime_view can view the overtime report"));
            }
            return Mono.just(true);
        });
    }
}
