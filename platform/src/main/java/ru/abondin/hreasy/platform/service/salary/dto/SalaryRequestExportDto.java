package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import org.springframework.lang.NonNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;

@Data
public class SalaryRequestExportDto {
    private String employee;
    private String type;
    private String budgetBusinessAccount;
    private LocalDate budgetExpectedFundingUntil;

    private String assessment;
    private String employeePosition;

    private String employeeProject;
    private String employeeProjectRole;
    private OffsetDateTime createdAt;

    private String createdBy;
    private BigDecimal reqIncreaseAmount;

    private String reqReason;

    private BigDecimal implIncreaseAmount;
    private YearMonth implIncreaseStartPeriod;
    private String implRejectReason;
    private String implState;
    private String implNewPosition;
    private String implemented;
}
