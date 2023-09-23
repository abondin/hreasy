package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data to report new salary request
 */
@Data
@Builder
public class SalaryRequestReportBody {

    @NotNull
    private Integer employeeId;
    /**
     * <ul>
     *     <li>1 - salary increase</li>
     *     <li>2 - bonus</li>
     * </ul>
     * @see SalaryRequestType
     */
    @NotNull
    private Short type;
    @NotNull
    private Integer budgetBusinessAccount;
    private LocalDate budgetExpectedFundingUntil;

    @NotNull
    private BigDecimal salaryIncrease;

    /**
     * YYYYMM period. Month starts with 0. 202308 - September of 2023
     */
    @NotNull
    private Integer increaseStartPeriod;
    private Integer assessmentId;
    @NotNull
    private String reason;
    private String comment;
}
