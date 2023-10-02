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
    // <editor-fold desc="Common fields">
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
    private OffsetDateTime createdAt;
    @NotNull

    private Integer createdBy;
    private Integer assessmentId;

    private OffsetDateTime deletedAt;
    private Integer deletedBy;
// </editor-fold>

// <editor-fold desc="Request fields">

    @NotNull
    private BigDecimal reqSalaryIncrease;
    /**
     * YYYYMM period. Month starts with 0. 202308 - September of 2023
     */
    @NotNull
    private Integer reqIncreaseStartPeriod;
    @NotNull
    private String reqReason;
    private String reqComment;
// </editor-fold>


    // <editor-fold desc="Implementation fields">
    private Integer implState;
    private OffsetDateTime implementedAt;
    private Integer implementedBy;
    @NotNull
    private BigDecimal implSalaryIncrease;
    /**
     * YYYYMM period. Month starts with 0. 202308 - September of 2023
     */
    @NotNull
    private Integer implIncreaseStartPeriod;
    @NotNull
    private String implReason;
    private String implComment;
    private Integer implNewPosition;
// </editor-fold>

}
