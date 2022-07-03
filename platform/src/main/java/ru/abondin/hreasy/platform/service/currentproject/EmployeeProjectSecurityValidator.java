package ru.abondin.hreasy.platform.service.currentproject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Validate security rules to  update employee current project
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeProjectSecurityValidator {

    private final ProjectHierarchyAccessor projectHierarchyService;

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

    public void setToNullUngrantedFields(EmployeeDto empl, AuthContext auth) {
        // Do nothing for my attributes
        if (empl == null || Objects.equals(empl.getId(), auth.getEmployeeInfo().getEmployeeId())) {
            return;
        }
        // Do nothing is has admin access to view employee
        if (auth.getAuthorities().contains("view_employee_full")) {
            return;
        }

        var hasViewCurrentProjectRolePerm = auth.getAuthorities().contains("view_empl_current_project_role");
        var hasViewSkillsPerm = auth.getAuthorities().contains("view_empl_skills");
        boolean isManager = projectHierarchyService.isManager(auth,
                empl.getCurrentProject() == null ? null
                        :
                        new ProjectHierarchyAccessor.ProjectInfo(empl.getCurrentProject().getId(),
                                empl.getDepartment() == null ? null : empl.getDepartment().getId(),
                                empl.getBa() == null ? null : empl.getBa().getId()
                        ));
        // Remove current project role
        if ((!hasViewCurrentProjectRolePerm || !isManager) && empl.getCurrentProject() != null) {
            empl.getCurrentProject().setRole(null);
        }

        // Remove skill
        if ((!hasViewSkillsPerm || !isManager)) {
            empl.setSkills(Arrays.asList());
        }

    }
}
