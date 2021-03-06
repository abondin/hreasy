package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

@Data
public class EmployeeWithSecurityInfoEntry {
    private int employeeId;
    private Integer userId;
    private String lastname;
    private String firstname;
    private String patronymicName;

    private LocalDate dateOfDismissal;

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
     * Comma separated list of projects
     */
    private String accessibleProjects;
    /**
     * Comma separated list of departments
     */
    private String accessibleDepartments;
    /**
     * Comma separated list of roles
     */
    private String roles;
}
