package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserSecurityInfoEntry extends EmployeeEntry {
    /**
     * List of accessible projects
     */
    private List<Integer> accessibleProjects;
    /**
     * List of projects where employee is assigned as manager
     */
    private List<Integer> managedProjects;
    /**
     * List of accessible departments
     */
    private List<Integer> accessibleDepartments;
    /**
     * List of departments where employee is assigned as manager
     */
    private List<Integer> managedDepartments;
    /**
     * List of accessible business accounts
     */
    private List<Integer> accessibleBas;
    /**
     * List of business accounts where employee is assigned as manager
     */
    private List<Integer> managedBas;
    /**
     * List of roles
     */
    private List<String> roles;
}
