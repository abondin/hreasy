package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Builder;
import lombok.Data;

import org.springframework.lang.NonNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reject salary increase or bonus request
 */
@Data
@Builder
public class SalaryRequestRejectBody {
    @NonNull
    private String reason;
    private String comment;
}
