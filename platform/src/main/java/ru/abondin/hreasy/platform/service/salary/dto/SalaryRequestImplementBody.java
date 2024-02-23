package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Builder;
import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import org.springframework.lang.NonNull;
import java.math.BigDecimal;

/**
 * Reject salary increase or bonus request
 */
@Data
@Builder
public class SalaryRequestImplementBody {
    @NonNull
    private BigDecimal increaseAmount;
    private BigDecimal salaryAmount;
    @NonNull
    private Integer increaseStartPeriod;
    private Integer newPosition;
    private String comment;
}
