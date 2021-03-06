package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;

import java.time.LocalDate;

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
