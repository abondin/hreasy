package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Update salary request or bonus
 */
@Data
@Builder
public class SalaryRequestUpdateBody {

    private LocalDate budgetExpectedFundingUntil;
    //TODO After salary storing feature implemented populate this field automatically
    private BigDecimal currentSalaryAmount;
    private BigDecimal plannedSalaryAmount;
    //TODO After salary storing feature implemented populate this field automatically
    private String previousSalaryIncreaseText;
    private LocalDate previousSalaryIncreaseDate;
    private Integer newPosition;
    private Integer assessmentId;
    private String comment;
}
