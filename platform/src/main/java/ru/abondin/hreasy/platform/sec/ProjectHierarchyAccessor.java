package ru.abondin.hreasy.platform.sec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;

import java.util.List;

/**
 * Build and validate project and departments hierarchy related to given employees
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class ProjectHierarchyAccessor {

    @Data
    @AllArgsConstructor
    public static class ProjectInfo {
        private Integer id;
        private Integer departmentId;
        private Integer baId;
    }

    private final DictProjectRepo projectRepo;


    /**
     * @param managerAuth
     * @param employeeId
     * @return true if employee from auth is manager for given employee. Manager means (or):
     * <ul>
     *     <li>Manager of current project of employee</li>
     *     <li>Manager of department of current project of employee</li>
     * </ul>
     */
    public Mono<Boolean> isManager(AuthContext managerAuth, int employeeId) {
        return projectRepo.getEmployeeCurrentProject(employeeId)
                .map(entry -> isManager(managerAuth, new ProjectInfo(entry.getId(), entry.getDepartmentId(), entry.getBaId())))
                .defaultIfEmpty(true);
    }

    /**
     * Check if manager without additional access to database
     *
     * @param auth
     * @param currentProject
     * @return
     */
    public Boolean isManager(AuthContext auth, ProjectInfo currentProject) {
        return currentProject == null ||
                (
                        auth.getEmployeeInfo().getAccessibleProjects().contains(currentProject.getId())
                                || (currentProject.getDepartmentId() != null && auth.getEmployeeInfo().getAccessibleDepartments().contains(currentProject.getDepartmentId()))
                                || (currentProject.getBaId() != null && auth.getEmployeeInfo().getAccessibleBas().contains(currentProject.getBaId()))
                );
    }

    public Boolean isBaOrDepartmentManager(AuthContext auth, Integer businessAccountId, Integer departmentId) {
        return (departmentId == null || auth.getEmployeeInfo().getAccessibleDepartments().contains(departmentId))
                || (businessAccountId == null || auth.getEmployeeInfo().getAccessibleBas().contains(businessAccountId));
    }


    /**
     * @param managerAuth
     * @param projectIds  - ids of the projects
     * @return true if employee from auth is manager for <b>all</b> given projects. Manager means (or):
     * <ul>
     *     <li>Manager of current project of employee</li>
     *     <li>Manager of department of current project of employee</li>
     *     <li>Manager of business account of current project of employee</li>
     * </ul>
     */
    public Mono<Boolean> isManagerOfAllProject(AuthContext managerAuth, List<Integer> projectIds) {
        return projectRepo.findByIds(projectIds).reduce(true,
                        (result, project) -> result &&
                                (managerAuth.getEmployeeInfo().getAccessibleProjects().contains(project.getId())
                                        || (project.getDepartmentId() != null && managerAuth.getEmployeeInfo().getAccessibleDepartments().contains(project.getDepartmentId()))
                                        || (project.getBaId() != null && managerAuth.getEmployeeInfo().getAccessibleBas().contains(project.getBaId()))
                                ))
                .defaultIfEmpty(true);
    }
}
