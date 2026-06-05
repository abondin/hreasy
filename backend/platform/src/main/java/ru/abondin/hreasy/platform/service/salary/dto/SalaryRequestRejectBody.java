package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Reject salary increase or bonus request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryRequestRejectBody {
    @NonNull
    private String reason;
    private String comment;

    /**
     * Reject current request and reschedule it to future period
     */
    @Nullable
    private Integer rescheduleToNewPeriod;
}
