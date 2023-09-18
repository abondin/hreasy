package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data to report new salary request
 */
@Data
public class SalaryRequestReportBody {
     @NotNull
    private Integer employeeId;
    /**
     * <ul>
     *     <li>1 - salary increase</li>
     *     <li>1 - bonus</li>
     * </ul>
     */
    @NotNull
    private Integer type;
    @NotNull
    private Integer budgetBusinessAccount;
    private LocalDate budgetExpectedFundingUntil;

    /**
     * <ul>
     *     <li>0 - created: when PM creates a request</li>
     *     <li>1 - in progress: when HR move it to in progress</li>
     *     <li>2 - implemented: when Finance implements</li>
     *     <li>3 - approved: when all required approvals collected and request is ready for the implementation by Finance</li>
     *     <li>4 - declined: when HR or Finance or BA Manager declines the request</li>
     * </ul>
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
    private Integer assessmentId;
    @NotNull
    private String reason;
    private String comment;
}
