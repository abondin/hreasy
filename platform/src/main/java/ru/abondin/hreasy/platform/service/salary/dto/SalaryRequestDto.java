package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class SalaryRequestDto {

    private Integer id;

    @NotNull
    private SimpleDictDto employee;
    /**
     * @see SalaryRequestReportType
     */
    @NotNull
    private Short type;
    @NotNull
    private SimpleDictDto budgetBusinessAccount;
    private LocalDate budgetExpectedFundingUntil;

    /**
     * @see SalaryRequestReportStat
     */
    @NotNull
    private Short stat;

    @NotNull
    private BigDecimal salaryIncrease;

    /**
     * YYYYMM period. Month starts with 0. 202308 - September of 2023
     */
    @NotNull
    private Integer increaseStartPeriod;
    private SimpleDictDto assessment;
    private SimpleDictDto employeeDepartment;

    @NotNull
    private String reason;
    private String comment;

    @NotNull
    private OffsetDateTime createdAt;
    @NotNull
    private SimpleDictDto createdBy;

    private OffsetDateTime inprogressAt;
    private SimpleDictDto inprogressBy;

    private OffsetDateTime implementedAt;
    private SimpleDictDto implementedBy;
}
