package ru.abondin.hreasy.platform.sec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;

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
    private final EmployeeRepo employeeRepo;


    /**
     * @param auth
     * @param employeeId
     * @return true if employee from auth has effective access to given employee. Access means (or):
     * <ul>
     *     <li>Access to current project of employee</li>
     *     <li>Access to department of current project of employee</li>
     *     <li>Access to department of employee</li>
     * </ul>
     */
    public Mono<Boolean> hasProjectAccess(AuthContext auth, int employeeId) {
        return Mono.zip(projectRepo.getEmployeeCurrentProject(employeeId),
                        employeeRepo.findById(employeeId))
                .map(tuple -> {
                    var project = tuple.getT1();
                    var employee = tuple.getT2();
                    return hasProjectAccess(auth, employee.getDepartmentId(),
                            new ProjectInfo(project.getId(), project.getDepartmentId(), project.getBaId()));
                })
                .defaultIfEmpty(true);
    }

    /**
     * Check project access without additional database queries.
     *
     * @param auth
     * @param employeeDepartmentId
     * @param currentProject
     * @return
     */
    public Boolean hasProjectAccess(AuthContext auth, Integer employeeDepartmentId, ProjectInfo currentProject) {
        return currentProject == null ||
                (
                        auth.getEmployeeInfo().getAccessibleProjects().contains(currentProject.getId())
                                || (currentProject.getDepartmentId() != null && auth.getEmployeeInfo().getAccessibleDepartments().contains(currentProject.getDepartmentId()))
                                || (employeeDepartmentId != null && auth.getEmployeeInfo().getAccessibleDepartments().contains(employeeDepartmentId))
                                || (currentProject.getBaId() != null && auth.getEmployeeInfo().getAccessibleBas().contains(currentProject.getBaId()))
                );
    }

    public Boolean hasBusinessAccountAccess(AuthContext auth, Integer businessAccountId) {
        return businessAccountId == null || auth.getEmployeeInfo().getAccessibleBas().contains(businessAccountId);
    }


    /**
     * @param auth
     * @param projectIds  - ids of the projects
     * @return true if employee from auth has effective access to <b>all</b> given projects. Access means (or):
     * <ul>
     *     <li>Access to current project of employee</li>
     *     <li>Access to department of current project of employee</li>
     *     <li>Access to business account of current project of employee</li>
     * </ul>
     */
    public Mono<Boolean> hasProjectAccessToAll(AuthContext auth, List<Integer> projectIds) {
        return projectRepo.findByIds(projectIds).reduce(true,
                        (result, project) -> result &&
                                (auth.getEmployeeInfo().getAccessibleProjects().contains(project.getId())
                                        || (project.getDepartmentId() != null && auth.getEmployeeInfo().getAccessibleDepartments().contains(project.getDepartmentId()))
                                        || (project.getBaId() != null && auth.getEmployeeInfo().getAccessibleBas().contains(project.getBaId()))
                                ))
                .defaultIfEmpty(true);
    }
}
