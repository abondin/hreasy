package ru.abondin.hreasy.platform.repo.employee;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EmployeeAuthInfoEntry {
    private Integer id;

    /**
     * Id of department, employee assigned to
     */
    private Integer departmentId;

    /**
     * Id of project, employee assigned to
     */
    private Integer currentProjectId;

    /**
     * List of ids of departments accessible to the employee.
     * Works for roles like overtime_view, overtime_edit, vacation_view, vacation_edit.
     * For example employee with role overtime_edit can edit overtimes only for employees with current project from department from given list
     */
    private List<Integer> accessibleDepartments = new ArrayList<>();


    /**
     * List of ids of business account accessible to the employee.
     * Works for roles like overtime_view, overtime_edit, vacation_view, vacation_edit.
     * For example employee with role overtime_edit can edit overtimes only for employees with current project from department from given list
     */
    private List<Integer> accessibleBas = new ArrayList<>();

    /**
     * Works for roles like overtime_view, overtime_edit, vacation_view, vacation_edit.
     * For example employee with role overtime_edit can edit overtimes only for employees with current project from given list
     * Means nothing if employee has access to the whole department
     */
    private List<Integer> accessibleProjects = new ArrayList<>();



}
