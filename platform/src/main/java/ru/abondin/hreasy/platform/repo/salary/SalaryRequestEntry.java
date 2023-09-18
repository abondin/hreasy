package ru.abondin.hreasy.platform.repo.salary;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Table("sal.salary_request")
public class SalaryRequestEntry {
    @Id
    private Integer id;

    @NotNull
    private Integer employeeId;
    /**
     * @see ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportType
     */
    @NotNull
    private Short type;
    @NotNull
    private Integer budgetBusinessAccount;
    private LocalDate budgetExpectedFundingUntil;

    /**
     * @see ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportStat
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
    private OffsetDateTime createdAt;
    private Integer createdBy;
    private OffsetDateTime deletedAt;
    private Integer deletedBy;
}
