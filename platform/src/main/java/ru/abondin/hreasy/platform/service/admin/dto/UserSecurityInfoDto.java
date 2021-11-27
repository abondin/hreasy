package ru.abondin.hreasy.platform.service.admin.dto;

import lombok.Data;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for admin page with list of all employees to assign roles, projects and departments
 */
@Data
public class UserSecurityInfoDto {
    private SimpleDictDto employee;

    private List<String> roles = new ArrayList<>();

    /**
     * Id of department, employee assigned to
     */
    @Nullable
    private Integer departmentId;

    /**
     * Id of project, employee assigned to
     */
    @Nullable
    private Integer currentProjectId;


    /**
     * List of ids of departments accessible to the employee.
     * Works for roles like overtime_view, overtime_edit, vacation_view, vacation_edit.
     * For example employee with role overtime_edit can edit overtimes only for employees with current project from department from given list
     */
    private List<Integer> accessibleDepartments = new ArrayList<>();

    /**
     * Works for roles like overtime_view, overtime_edit, vacation_view, vacation_edit.
     * For example employee with role overtime_edit can edit overtimes only for employees with current project from given list
     * Means nothing if employee has access to the whole department
     */
    private List<Integer> accessibleProjects = new ArrayList<>();

    private LocalDate dateOfDismissal;
}
