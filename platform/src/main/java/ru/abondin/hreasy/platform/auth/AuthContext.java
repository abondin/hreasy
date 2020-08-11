package ru.abondin.hreasy.platform.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Authorization context for business logic service layer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthContext {
    private String username;
    private String email;
    private Collection<String> authorities = new ArrayList<>();
    @Nullable
    private EmployeeInfo employeeInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeInfo {
        private Integer employeeId;
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


        public EmployeeInfo(EmployeeInfo employeeInfo) {
            this(employeeInfo.getEmployeeId(), employeeInfo.getAccessibleDepartments(), employeeInfo.getAccessibleProjects());
        }
    }
}
