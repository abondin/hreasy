package ru.abondin.hreasy.platform.service.currentproject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;

import java.util.ArrayList;

/**
 * Validate security rules to  update employee current project
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeProjectSecurityValidator {

    private final ProjectHierarchyAccessor projectHierarchyService;
    private final DictProjectRepo projectRepo;

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


    public Mono<Boolean> validateAddOrDeleteSkill(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (employeeId == auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.just(true);
            }
            if (!auth.getAuthorities().contains("edit_skills")) {
                return Mono.just(false);
            }
            return projectHierarchyService.isManager(auth, employeeId);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only logged in user or user with permission edit_skills can update employee skills"));
        });
    }


    public Mono<Boolean> validateUpdateRating(AuthContext auth, int skillEmployeeId, Integer skillId) {
        // Anyone add or update rating for skill
        return Mono.just(true);
    }
}
