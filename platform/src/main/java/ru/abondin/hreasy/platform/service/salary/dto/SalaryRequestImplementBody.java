package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Builder;
import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Reject salary increase or bonus request
 */
@Data
@Builder
public class SalaryRequestImplementBody {
    @NotNull
    private BigDecimal salaryIncrease;
    @NotNull
    private Integer increaseStartPeriod;
    private Integer newPosition;
    @NotNull
    private String reason;
    private String comment;
}
