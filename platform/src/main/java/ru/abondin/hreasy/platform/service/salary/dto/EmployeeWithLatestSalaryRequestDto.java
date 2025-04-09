package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class EmployeeWithLatestSalaryRequestDto {

    private Integer employeeId;

    private String employeeDisplayName;

    private String employeeEmail;

    private SimpleDictDto employeeBusinessAccount;

    private CurrentProjectDictDto employeeCurrentProject;

    private LocalDate employeeDateOfEmployment;

    private OffsetDateTime requestCreatedAt;

    private Integer requestId;

    private Integer requestStartPeriod;

    private BigDecimal requestReqIncreaseAmount;

    private BigDecimal requestImplIncreaseAmount;

    private BigDecimal requestImplSalaryAmount;

    private Integer requestImplState;

    private Integer getId() {
        return employeeId;
    }
}
