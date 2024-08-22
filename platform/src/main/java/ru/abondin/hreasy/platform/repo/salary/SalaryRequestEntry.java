package ru.abondin.hreasy.platform.repo.salary;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@Table("sal.salary_request")
public class SalaryRequestEntry {
    // <editor-fold desc="Common fields">
    @Id
    private Integer id;

    @NonNull
    private Integer employeeId;
    /**
     * @see SalaryRequestType
     */
    @NonNull
    private Short type;
    @NonNull
    private Integer budgetBusinessAccount;
    private LocalDate budgetExpectedFundingUntil;

    @NonNull
    private OffsetDateTime createdAt;
    @NonNull

    private Integer createdBy;
    private Integer assessmentId;

    private OffsetDateTime deletedAt;
    private Integer deletedBy;
// </editor-fold>

// <editor-fold desc="Request fields">

    @NonNull
    private BigDecimal reqIncreaseAmount;
    private BigDecimal reqPlannedSalaryAmount;
    /**
     * YYYYMM period. Month starts with 0. 202308 - September of 2023
     */
    @NonNull
    private Integer reqIncreaseStartPeriod;
    private Integer reqNewPosition;
    @NonNull
    private String reqReason;
    private String reqComment;
// </editor-fold>

// <editor-fold desc="Info fields">

    private Integer infoEmplProject;
    private String infoEmplProjectRole;
    private LocalDate infoDateOfEmployment;
    private Integer infoEmplBa;
    private Integer infoEmplPosition;
    //TODO Should be stored in separate table
    private BigDecimal infoCurrentSalaryAmount;
    //TODO Should be stored in separate table
    private String infoPreviousSalaryIncreaseText;
// </editor-fold>



    // <editor-fold desc="Implementation fields">
    private Integer implState;
    private OffsetDateTime implementedAt;
    private Integer implementedBy;
    private BigDecimal implIncreaseAmount;
    private BigDecimal implSalaryAmount;
    /**
     * YYYYMM period. Month starts with 0. 202308 - September of 2023
     */
    private Integer implIncreaseStartPeriod;
    private String implIncreaseText;
    private String implRejectReason;
    private String implComment;
    private Integer implNewPosition;
// </editor-fold>

}
