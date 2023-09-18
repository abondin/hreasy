package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestView;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;

/**
 * Validate security rules for salary requests
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalarySecurityValidator {

    private final ProjectHierarchyAccessor projectHierarchyService;

    public Mono<Boolean> validateReportSalaryRequest(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("report_salary_request")) {
                return Mono.just(false);
            }
            return Mono.just(true);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only employee with report_salary_request permission can report salary request increase or bonus"));
        });
    }

    public Mono<Boolean> validateUpdateSalaryRequest(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("update_salary_request")) {
                return Mono.just(false);
            }
            return Mono.just(true);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only employee with update_salary_request permission can update report salary request state"));
        });
    }

    public Mono<Boolean> validateApproveSalaryRequest(AuthContext auth, Integer businessAccount, Integer department) {
        return Mono.defer(() -> Mono.just(validateApproveSalaryRequestSync(auth, businessAccount, department))).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException(
                    """
                            Only employee with 'approve_salary_request_globally' permission can approve any salary request.
                            Employee with 'approve_salary_request' permission can approve salary request only if he/she is department manager of the employee
                            or manager of the budgeting business account 
                                                        """
            ));
        });
    }

    private boolean validateApproveSalaryRequestSync(AuthContext auth, Integer businessAccount, Integer department) {
        if (auth.getAuthorities().contains("approve_salary_request_globally")) {
            return true;
        }
        if (!auth.getAuthorities().contains("approve_salary_request")) {
            return false;
        }
        return projectHierarchyService.isBaOrDepartmentManager(auth, businessAccount, department);
    }


    public Mono<Boolean> validateViewSalaryRequestsOfBusinessAccount(AuthContext auth, int baId) {
        return Mono.defer(() -> Mono.just(validateApproveSalaryRequestSync(auth, baId, null))).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException(
                    """
                            Only employee with 'approve_salary_request_globally' permission can view any salary request.
                            Employee with 'approve_salary_request' permission can view salary request only if he/she is 
                            manager of the budgeting business account 
                                                        """
            ));
        });
    }

    public Mono<Boolean> validateViewSalaryRequestsOfDepartments(AuthContext auth, int departmentId) {
        return Mono.defer(() -> Mono.just(validateApproveSalaryRequestSync(auth, null, departmentId))).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException(
                    """
                            Only employee with 'approve_salary_request_globally' permission can view any salary request.
                            Employee with 'approve_salary_request' permission can view salary request only if he/she is department manager 
                                                        """
            ));
        });
    }

    public Mono<Boolean> validateViewSalaryRequest(AuthContext auth, SalaryRequestView salaryRequest) {
        return Mono.defer(() -> {
            // Manager who report the request can view it
            if (auth.getEmployeeInfo().getEmployeeId().equals(salaryRequest.getCreatedBy())) {
                return Mono.just(true);
            }
            // Manager who can update the request can view it
            if (auth.getAuthorities().contains("update_salary_request")) {
                return Mono.just(true);
            }
            // Manager who can approve request can view it
            return Mono.just(validateApproveSalaryRequestSync(auth, salaryRequest.getBudgetBusinessAccount(), salaryRequest.getEmployeeDepartmentId()));
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("""
                                        Employee can view request which he/she created.  
                                        Employee with 'approve_salary_request_globally' permission can view any salary request.
                    Employee with 'approve_salary_request' permission can view salary request only if he/she is 
                    manager of the budgeting business account                     
                                                            """));
        });

    }
}
