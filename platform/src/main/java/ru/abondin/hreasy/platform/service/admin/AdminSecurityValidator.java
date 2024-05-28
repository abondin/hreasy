package ru.abondin.hreasy.platform.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Validate security rules to work in admin area
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSecurityValidator {

    private final ProjectHierarchyAccessor projectHierarchyService;
    private final DictProjectRepo projectRepo;

    public Mono<Boolean> validateCreateProject(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("create_project")) {
                return Mono.error(new AccessDeniedException("Only user with permission create_project can create new project"));
            }
            return Mono.just(true);
        });
    }


    public Mono<Boolean> validateUpdateProject(AuthContext auth, DictProjectEntry entry) {
        return Mono.defer(() -> {
            // 1. Allow update project if I created project
            if (auth.getEmployeeInfo().getEmployeeId().equals(entry.getCreatedBy())) {
                return Mono.just(true);
            }

            // 2. Allow update project if I have update_project_globally permissions
            if (auth.getAuthorities().contains("update_project_globally")) {
                return Mono.just(true);
            }

            // 2. Allow update project if I have update_project permissions and I manager of the project/ba/department
            if (auth.getAuthorities().contains("update_project")) {
                return projectHierarchyService.isManagerOfAllProject(auth, Arrays.asList(entry.getId()))
                        .filter(manager -> manager)
                        .switchIfEmpty(
                                Mono.error(new AccessDeniedException("User with update_project" +
                                        " should be manager of the project or ba or department"))
                        );

            } else {
                return Mono.error(new AccessDeniedException("Only user with permission update_project, update_project_globally or project creator" +
                        " can update new project"));

            }
        });
    }


    public Mono<Boolean> validateFindAllProject(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("project_admin_area")) {
                return Mono.error(new AccessDeniedException("Only user with permission project_admin_area can access project admin area"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateGetEmployeeWithSecurity(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_users")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_users can view employee " +
                        "info with security info"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateUpdateUserRoles(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("admin_users")) {
                return Mono.error(new AccessDeniedException("Only user with permission admin_users can update roles"));
            }
            return Mono.just(true);
        });
    }

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

    public Mono<Boolean> validateUpdateBAPosition(AuthContext auth, int baId) {
        return validateAddOrUpdateBusinessAccount(auth);
    }

    public Mono<Boolean> validateEditArticle(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("edit_articles")) {
                return Mono.error(new AccessDeniedException("Only user with permission edit_articles can create" +
                        " or update article"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateEditEmployee(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("edit_employee_full")) {
                return Mono.error(new AccessDeniedException("Only user with permission edit_employee_full can create " +
                        " or update employee all information"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateImportEmployee(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("import_employee")) {
                return Mono.error(new AccessDeniedException("Only user with permission import_employee can import " +
                        "employees from file"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateViewEmployeeFull(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("view_employee_full")) {
                return Mono.error(new AccessDeniedException("Only user with permission view_employee_full can see" +
                        " full employee info"));
            }
            return Mono.just(true);
        });
    }

    public Mono<Boolean> validateUpdateTelegram(AuthContext auth, int employeeId) {
        // Now only current employee can update own telegram.
        // Also telegram can be updated by admin when all employee information updates (see AdminEmployeeService)
        if (employeeId == auth.getEmployeeInfo().getEmployeeId()) {
            return Mono.just(true);
        } else {
            return Mono.error(new AccessDeniedException("Employee can update only own telegram account"));
        }
    }

    public Mono<Boolean> validateUpdateCurrentProject(AuthContext auth, int employeeId, Integer newProject) {
        return Mono.defer(() -> {
            // Allow to update my own project
            if (employeeId == auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.just(true);
            }
            // Allow to update project with update_current_project_global permission
            if (auth.getAuthorities().contains("update_current_project_global")) {
                return Mono.just(true);
            }
            // Allow to update project with update_current_project permission if
            // 1. Manager of whole department
            // 2. Manager of old project (if exists) and new project
            if (auth.getAuthorities().contains("update_current_project")) {
                final var projectsToCheck = new ArrayList<Integer>();
                if (newProject != null) {
                    projectsToCheck.add(newProject);
                }
                return projectRepo.getEmployeeCurrentProject(employeeId)
                        .map(currentProject -> {
                            projectsToCheck.add(currentProject.getId());
                            return projectsToCheck;
                        })
                        .flatMap(projects -> projectHierarchyService.isManagerOfAllProject(auth, projectsToCheck))
                        .switchIfEmpty(projectHierarchyService.isManagerOfAllProject(auth, projectsToCheck))
                        .flatMap(isManager -> isManager
                                ? Mono.just(true) :
                                Mono.error(new AccessDeniedException("You must be manager of current employee project and new employee project")));
            }
            return Mono.error(new AccessDeniedException("Only logged in user or user with permission update_current_project or" +
                    " update_current_project_global can update the current project"));
        });
    }

    public Mono<Boolean> validateConfirmTelegramAccount(AuthContext auth, int employeeId, String telegramAccount) {
        return Mono.defer(() -> {
            // Allow to update my own project
            if (employeeId == auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only logged in user can confirm the telegram account"));
        });
    }
}
