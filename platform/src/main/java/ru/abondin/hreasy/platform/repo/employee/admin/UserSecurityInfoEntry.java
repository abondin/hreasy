package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;

@Data
public class UserSecurityInfoEntry extends EmployeeEntry {
    private Integer userId;


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
