package ru.abondin.hreasy.platform.sec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsWithEmployeeInfo implements UserDetails {
    private UserDetails delegate;
    private Integer employeeId;
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
     * List of ids of business accounts accessible to the employee.
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

    /**
     * Which authentication provider was used to logging user
     * 1 - LDAP,
     * 2 - INTERNAL,
     * 3 - Master password (only in developer environment)
     */
    private Short loggedInType;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getPassword() {
        return delegate.getPassword();
    }

    @Override
    public String getUsername() {
        return delegate.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return delegate.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return delegate.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return delegate.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }
}
