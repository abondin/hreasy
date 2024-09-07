package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;

@Data
public class SalaryRequestExportDto {

    private String employee;
    private Short typeValue;
    private String type;
    private String budgetBusinessAccount;
    private LocalDate budgetExpectedFundingUntil;
    private LocalDate dateOfEmployment;

    private String assessment;
    private String employeePosition;

    private String employeeProject;
    private String employeeProjectRole;
    private String employeeBusinessAccount;
    private OffsetDateTime createdAt;

    private String createdBy;
    private BigDecimal currentSalaryAmount;

    private BigDecimal reqIncreaseAmount;
    private BigDecimal reqPlannedSalaryAmount;
    private String reqNewPosition;

    private String reqReason;

    private BigDecimal implIncreaseAmount;
    private BigDecimal implSalaryAmount;
    private YearMonth implIncreaseStartPeriod;
    private String implRejectReason;
    private String implState;
    private String implNewPosition;
    private String implComment;
    private String implIncreaseText;
    private String implemented;

    private String previousSalaryIncreaseText;
    private LocalDate previousSalaryIncreaseDate;
}
