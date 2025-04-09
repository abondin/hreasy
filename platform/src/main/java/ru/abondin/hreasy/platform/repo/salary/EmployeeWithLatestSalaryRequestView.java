package ru.abondin.hreasy.platform.repo.salary;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class EmployeeWithLatestSalaryRequestView {

    private Integer employeeId;

    private String employeeDisplayName;

    private String employeeEmail;

    private Integer employeeBusinessAccountId;

    private String employeeBusinessAccountName;

    private Integer employeeCurrentProjectId;

    private String employeeCurrentProjectName;

    private String employeeCurrentProjectRole;

    private LocalDate employeeDateOfEmployment;

    private Integer requestId;

    private OffsetDateTime requestCreatedAt;

    private Integer requestStartPeriod;

    private BigDecimal requestReqIncreaseAmount;

    private BigDecimal requestImplIncreaseAmount;

    private BigDecimal requestImplSalaryAmount;

    private Integer requestImplState;
}
