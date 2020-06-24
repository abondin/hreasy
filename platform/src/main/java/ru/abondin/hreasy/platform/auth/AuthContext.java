package ru.abondin.hreasy.platform.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;

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

        public EmployeeInfo(EmployeeInfo employeeInfo) {
            this(employeeInfo.getEmployeeId());
        }
    }
}
