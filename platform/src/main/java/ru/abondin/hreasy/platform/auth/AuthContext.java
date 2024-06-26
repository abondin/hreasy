package ru.abondin.hreasy.platform.auth;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Authorization context for business logic service layer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = "username")
public class AuthContext {
    private String username;
    private String email;
    private Collection<String> authorities = new ArrayList<>();
    @NotNull
    private EmployeeInfo employeeInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeInfo {
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
         * List of ids of departments accessible to the employee.
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
         * 1 - LDAP,
         * 2 - INTERNAL,
         * 3 - Master password (only in developer environment)
         * 4 - Telegram Bot
         */
        @Nullable
        private Short loggedInType;

        /**
         * Populates only if request comes from telegram bot
         */
        @Nullable
        private String telegramAccount;


        public EmployeeInfo(EmployeeInfo employeeInfo) {
            this(employeeInfo.getEmployeeId(),
                    employeeInfo.getDepartmentId(),
                    employeeInfo.getCurrentProjectId(),
                    employeeInfo.getAccessibleDepartments(),
                    employeeInfo.getAccessibleBas(),
                    employeeInfo.getAccessibleProjects(),
                    employeeInfo.getLoggedInType(),
                    employeeInfo.getTelegramAccount());
        }
    }


    @AllArgsConstructor
    public enum LoginType {
        LDAP((short) 1), INTERNAL((short) 2), MASTER_PASSWORD((short) 3),
        TELEGRAM_BOT_SERVICE((short) 4);
        @Getter
        private final short value;

        public static String prettyPrint(Short value) {
            return value == null ? "NOT SPECIFIED" : Arrays.stream(LoginType.values()).filter(s -> s.value == value).findFirst()
                    .map(Enum::toString)
                    .orElse("Unknown Login Type " + value);
        }
    }
}
