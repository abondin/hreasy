package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestApprovalDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SalaryRequestDto {

    private Integer id;

    @NonNull
    private SimpleDictDto employee;
    /**
     * @see SalaryRequestType
     */
    @NonNull
    private Short type;
    @NonNull
    private SimpleDictDto budgetBusinessAccount;
    private LocalDate budgetExpectedFundingUntil;


    private SimpleDictDto assessment;

    @NonNull
    private OffsetDateTime createdAt;
    @NonNull
    private SimpleDictDto createdBy;

    private SalaryRequestReq req = new SalaryRequestReq();
    private SalaryRequestImpl impl = null;
    private EmployeeInfo employeeInfo = new EmployeeInfo();

    private List<SalaryRequestApprovalDto> approvals = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class SalaryRequestReq {
        @NonNull
        private BigDecimal increaseAmount;
        private BigDecimal plannedSalaryAmount;

        /**
         * YYYYMM period. Month starts with 0. 202308 - September of 2023
         */
        @NonNull
        private Integer increaseStartPeriod;
        @NonNull
        private String reason;
        private String comment;
        private SimpleDictDto newPosition;
    }

    @Data
    @NoArgsConstructor
    public static class SalaryRequestImpl {
        /**
         * @see SalaryRequestImplementationState
         */
        private short state;

        private BigDecimal increaseAmount;
        private BigDecimal salaryAmount;
        /**
         * YYYYMM period. Month starts with 0. 202308 - September of 2023
         */
        private Integer increaseStartPeriod;
        private SimpleDictDto newPosition;
        private String rejectReason;
        private String comment;

        private OffsetDateTime implementedAt;
        private SimpleDictDto implementedBy;
    }

    @Data
    public static class EmployeeInfo {
        private CurrentProjectDictDto currentProject;
        private LocalDate dateOfEmployment;
        private SimpleDictDto ba;
        private SimpleDictDto position;
        //TODO After salary storing feature implemented populate this field automatically
        private BigDecimal currentSalaryAmount;
        //TODO After salary storing feature implemented populate this field automatically
        private String previousSalaryIncreaseText;
        private LocalDate previousSalaryIncreaseDate;
    }
}
