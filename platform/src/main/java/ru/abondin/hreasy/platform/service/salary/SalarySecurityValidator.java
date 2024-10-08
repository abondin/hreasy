package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestApprovalEntry;
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


    public enum ViewSalaryRequestMode {
        /**
         * request was created by logged-in user
         */
        ONLY_MY,
        /**
         * logged in user has role finance and has access to request's budgeting business account
         */
        FROM_MY_BAS
    }

    private final ProjectHierarchyAccessor projectHierarchyService;

    /**
     * @param auth
     * @return true
     */
    public Mono<ViewSalaryRequestMode> validateView(AuthContext auth) {
        return Mono.defer(() -> {
            if (auth.getAuthorities().contains("approve_salary_request")) {
                return Mono.just(ViewSalaryRequestMode.FROM_MY_BAS);
            } else if (auth.getAuthorities().contains("report_salary_request")) {
                return Mono.just(ViewSalaryRequestMode.ONLY_MY);
            } else {
                return Mono.error(new AccessDeniedException("Only employee with report_salary_request or approve_salary_request permissions can view salary requests"));
            }
        });
    }

    /**
     * @param auth
     * @return true
     */
    public Mono<Boolean> validateViewAll(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_salary_request")) {
                return Mono.just(false);
            }
            return Mono.just(true);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only employee with admin_salary_request permission can view all salary requests"));
        });
    }

    public Mono<Boolean> validateExport(AuthContext auth) {
        return validateViewAll(auth);
    }

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

    public Mono<Boolean> validateAdminSalaryRequest(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_salary_request")) {
                return Mono.just(false);
            }
            return Mono.just(true);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only employee with admin_salary_request permission can admin salary requests"));
        });
    }

    /**
     * Check if user has permission to approve salary request
     *
     * <ul>
     *     <li>user has 'admin_salary_request'</li>
     *     <li>user has 'approve_salary_request' and is business account manager</li>
     * </ul>
     * @param auth
     * @param businessAccount
     * @return
     */
    public Mono<Boolean> validateApproveSalaryRequest(AuthContext auth, Integer businessAccount) {
        return Mono.defer(() -> Mono.just(validateApproveSalaryRequestSync(auth, businessAccount))).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException(
                    """
                      User can approve requests if he/she has 'admin_salary_request' or has 'approve_salary_request' and is business account manager
                                                        """
            ));
        });
    }

    public Mono<Boolean> validateDeleteApproval(AuthContext auth, SalaryRequestApprovalEntry entry) {
        if (auth.getEmployeeInfo().getEmployeeId().equals(entry.getCreatedBy())){
            return Mono.just(true);
        }
        if (auth.getAuthorities().contains("admin_salary_request")){
            return Mono.just(true);
        }
        return Mono.error(new AccessDeniedException("Employee can delete request which he/she created or with admin_salary_request permission."));
    }



    private boolean validateApproveSalaryRequestSync(AuthContext auth, Integer businessAccount) {
        if (auth.getAuthorities().contains("admin_salary_request")){
            return true;
        }
        if (!auth.getAuthorities().contains("approve_salary_request")) {
            return false;
        }
        return projectHierarchyService.isBaManager(auth, businessAccount);
    }

    public Mono<Boolean> validateUpdateOrDeleteSalaryRequest(AuthContext auth, SalaryRequestEntry salaryRequest) {
        return Mono.defer(() -> {
            // Manager who report the request can delete it
            if (auth.getEmployeeInfo().getEmployeeId().equals(salaryRequest.getCreatedBy())) {
                return Mono.just(true);
            }
            // Manager who can update the request can delete it
            if (auth.getAuthorities().contains("admin_salary_request")) {
                return Mono.just(true);
            }
            return Mono.just(false);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("""
                    Employee can delete request which he/she created.  
                    Employee with admin_salary_request permission can delete any salary request.
                                        """));
        });
    }

    public Mono<Boolean> validateViewSalaryRequest(AuthContext auth, SalaryRequestView salaryRequest) {
        return Mono.defer(() -> {
            // Manager who report the request can view it
            if (auth.getEmployeeInfo().getEmployeeId().equals(salaryRequest.getCreatedBy())) {
                return Mono.just(true);
            }
            // Manager who can update the request can view it
            if (auth.getAuthorities().contains("admin_salary_request")) {
                return Mono.just(true);
            }
            // Manager who can approve request can view it
            return Mono.just(validateApproveSalaryRequestSync(auth, salaryRequest.getBudgetBusinessAccount()));
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("""
                                        Employee can view request which he/she created.  
                                        Employee with admin_salary_request permission can view any salary request.
                    Employee with 'approve_salary_request' permission can view salary request only if he/she is 
                    manager of the budgeting business account                     
                                                            """));
        });

    }


    public Mono<Boolean> validateCloseSalaryRequestPeriod(AuthContext auth) {
        return Mono.defer(() -> {
            if (auth.getAuthorities().contains("admin_salary_request")) {
                return Mono.just(true);
            }
            return Mono.just(false);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only employee with admin_salary_request permission may close report period"));
        });
    }


}
