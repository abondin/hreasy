package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import org.springframework.lang.NonNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class SalaryRequestDto {

    private Integer id;

    @NonNull
    private SimpleDictDto employee;

    private CurrentProjectDictDto employeeCurrentProject;

    /**
     * @see SalaryRequestType
     */
    @NonNull
    private Short type;
    @NonNull
    private SimpleDictDto budgetBusinessAccount;
    private LocalDate budgetExpectedFundingUntil;


    private SimpleDictDto assessment;
    private SimpleDictDto employeePosition;

    @NonNull
    private OffsetDateTime createdAt;
    @NonNull
    private SimpleDictDto createdBy;

    private SalaryRequestReq req = new SalaryRequestReq();
    private SalaryRequestImpl impl = null;


    @Data
    @NoArgsConstructor
    public static class SalaryRequestReq {
        @NonNull
        private BigDecimal increaseAmount;

        /**
         * YYYYMM period. Month starts with 0. 202308 - September of 2023
         */
        @NonNull
        private Integer increaseStartPeriod;
        @NonNull
        private String reason;
        private String comment;
    }

    @Data
    @NoArgsConstructor
    public static class SalaryRequestImpl {
        /**
         * @see SalaryRequestImplementationState
         */
        private short state;

        private BigDecimal increaseAmount;
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
}
