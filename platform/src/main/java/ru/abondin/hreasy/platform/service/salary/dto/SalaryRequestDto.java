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
     * @see SalaryRequestType
     */
    @NotNull
    private Short type;
    @NotNull
    private SimpleDictDto budgetBusinessAccount;
    private LocalDate budgetExpectedFundingUntil;


    private SimpleDictDto assessment;
    private SimpleDictDto employeeDepartment;
    private SimpleDictDto employeePosition;

    @NotNull
    private OffsetDateTime createdAt;
    @NotNull
    private SimpleDictDto createdBy;

    private SalaryRequestReq req = new SalaryRequestReq();
    private SalaryRequestImpl impl = null;


    @Data
    public static class SalaryRequestReq {
        @NotNull
        private BigDecimal salaryIncrease;

        /**
         * YYYYMM period. Month starts with 0. 202308 - September of 2023
         */
        @NotNull
        private Integer increaseStartPeriod;
        @NotNull
        private String reason;
        private String comment;
    }

    @Data
    public static class SalaryRequestImpl {
        /**
         * @see SalaryRequestImplementationState
         */
        @NotNull
        private short state;

        @NotNull
        private BigDecimal salaryIncrease;
        /**
         * YYYYMM period. Month starts with 0. 202308 - September of 2023
         */
        @NotNull
        private Integer increaseStartPeriod;
        private SimpleDictDto newPosition;
        @NotNull
        private String reason;
        private String comment;

        private OffsetDateTime implementedAt;
        private SimpleDictDto implementedBy;
    }
}
