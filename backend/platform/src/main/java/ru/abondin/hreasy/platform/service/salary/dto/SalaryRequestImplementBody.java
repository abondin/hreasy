package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;

/**
 * Reject salary increase or bonus request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryRequestImplementBody {
    @NonNull
    private BigDecimal increaseAmount;
    private BigDecimal salaryAmount;
    @NonNull
    private Integer increaseStartPeriod;
    private Integer newPosition;
    private String comment;
}
