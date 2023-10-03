package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reject salary increase or bonus request
 */
@Data
@Builder
public class SalaryRequestRejectBody {
    @NotNull
    private String reason;
    private String comment;
}
