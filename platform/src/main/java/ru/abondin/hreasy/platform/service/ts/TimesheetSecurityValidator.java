package ru.abondin.hreasy.platform.service.ts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordRepo;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;

/**
 * Validate security rules to  update employee current project
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimesheetSecurityValidator {

    private final ProjectHierarchyAccessor projectHierarchyService;
    private final TimesheetRecordRepo repo;

    public Mono<Boolean> validateViewTimesheet(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (employeeId == auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.just(true);
            }
            if (!auth.getAuthorities().contains("view_timesheet")) {
                return Mono.just(false);
            }
            return projectHierarchyService.isManager(auth, employeeId);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only logged in user or user with permission view_timesheet can view timesheet"));
        });
    }

    public Mono<Boolean> validateViewTimesheetSummary(AuthContext auth) {
        return Mono.defer(() -> {
            if (auth.getAuthorities().contains("view_timesheet_summary")) {
                return Mono.just(true);
            } else {
                return Mono.error(new AccessDeniedException("Only user with permission view_timesheet_summary can view timesheet summary"));
            }
        });
    }

    public Mono<Boolean> validateReportTimesheet(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (employeeId == auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.just(true);
            }
            if (!auth.getAuthorities().contains("report_timesheet")) {
                return Mono.just(false);
            }
            return projectHierarchyService.isManager(auth, employeeId);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only logged in user or user with permission report_timesheet can report timesheet"));
        });
    }

    public Mono<Boolean> validateUpdateOrDeleteTimesheet(AuthContext auth, int employeeId, int timesheetRecordId) {
        return repo.findById(timesheetRecordId)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", employeeId + ":" + timesheetRecordId)))
                .flatMap(t -> {
                    if (employeeId != t.getEmployee()) {
                        return Mono.error(new BusinessError("errors.entity.invalid.parent", Integer.toString(timesheetRecordId),
                                Integer.toString(employeeId)));
                    }
                    return validateReportTimesheet(auth, employeeId);
                });
    }
}
