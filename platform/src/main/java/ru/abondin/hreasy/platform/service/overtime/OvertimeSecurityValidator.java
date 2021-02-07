package ru.abondin.hreasy.platform.service.overtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeClosedPeriodDto;

/**
 * Validate security rules to get and update overtimes
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OvertimeSecurityValidator {

    private final ProjectHierarchyAccessor projectHierarchyService;

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

    //TODO Add separate permission
    public Mono<Boolean> validateApproveOvertime(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("overtime_edit")) {
                return Mono.just(false);
            }
            return projectHierarchyService.isManager(auth, employeeId);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission overtime_edit can approve the overtime"));
        });
    }


    //TODO Add separate permission
    public Mono<Boolean> validateExportOvertimes(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("overtime_edit")) {
                return Mono.just(false);
            }
            return Mono.just(true);
        });
    }

    // Check if user has acess to admin all overtime stuff
    public Mono<Boolean>  validateAdminOvertime(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("overtime_admin")) {
                return Mono.just(false);
            }
            return Mono.just(true);
        });
    }
}
