package ru.abondin.hreasy.platform.sec;

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
                .map(
                        currentProject -> managerAuth.getEmployeeInfo().getAccessibleProjects().contains(currentProject.getId())
                                || (currentProject.getDepartmentId() != null && managerAuth.getEmployeeInfo().getAccessibleDepartments().contains(currentProject.getDepartmentId())))
                .defaultIfEmpty(true);
    }


    /**
     * @param managerAuth
     * @param projectIds  - ids of the projects
     * @return true if employee from auth is manager for <b>all</b> given projects. Manager means (or):
     * <ul>
     *     <li>Manager of current project of employee</li>
     *     <li>Manager of department of current project of employee</li>
     * </ul>
     */
    public Mono<Boolean> isManagerOfAllProject(AuthContext managerAuth, List<Integer> projectIds) {
        return projectRepo.findByIds(projectIds).reduce(true,
                (result, project) -> result &&
                        (managerAuth.getEmployeeInfo().getAccessibleProjects().contains(project.getId())
                                || (project.getDepartmentId() != null && managerAuth.getEmployeeInfo().getAccessibleDepartments().contains(project.getDepartmentId()))))
                .defaultIfEmpty(true);
    }
}
