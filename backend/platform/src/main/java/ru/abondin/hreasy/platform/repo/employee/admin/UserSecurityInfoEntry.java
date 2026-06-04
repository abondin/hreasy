package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;

import java.util.List;

@Data
public class UserSecurityInfoEntry extends EmployeeEntry {
    /**
     * List of accessible projects
     */
    private List<Integer> accessibleProjects;
    /**
     * List of accessible departments
     */
    private List<Integer> accessibleDepartments;
    /**
     * List of accessible business accounts
     */
    private List<Integer> accessibleBas;
    /**
     * List of roles
     */
    private List<String> roles;
}
