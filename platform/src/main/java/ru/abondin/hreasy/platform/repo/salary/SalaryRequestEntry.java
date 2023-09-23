package ru.abondin.hreasy.platform.repo.salary;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestType;

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
    @NotNull
    private OffsetDateTime createdAt;
    @NotNull

    private Integer createdBy;

    private OffsetDateTime inprogressAt;
    private Integer inprogressBy;

    private OffsetDateTime implementedAt;
    private Integer implementedBy;


    private OffsetDateTime deletedAt;
    private Integer deletedBy;
}
