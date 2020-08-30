package ru.abondin.hreasy.platform.sec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;

/**
 * Build and validate project and departments hierarchy related to given employees
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class ProjectHierarchyService {
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
}
