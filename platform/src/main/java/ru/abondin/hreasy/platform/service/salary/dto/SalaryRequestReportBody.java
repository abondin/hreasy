package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.*;

import org.springframework.lang.NonNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data to report new salary request
 */
@Data
@Builder
public class SalaryRequestReportBody {

    @NonNull
    private Integer employeeId;
    /**
     * <ul>
     *     <li>1 - salary increase</li>
     *     <li>2 - bonus</li>
     * </ul>
     * @see SalaryRequestType
     */
    @NonNull
    private Short type;
    @NonNull
    private Integer budgetBusinessAccount;
    private LocalDate budgetExpectedFundingUntil;

    @NonNull
    private BigDecimal increaseAmount;

    /**
     * YYYYMM period. Month starts with 0. 202308 - September of 2023
     */
    @NonNull
    private Integer increaseStartPeriod;
    private Integer assessmentId;
    @NonNull
    private String reason;
    private String comment;
}
