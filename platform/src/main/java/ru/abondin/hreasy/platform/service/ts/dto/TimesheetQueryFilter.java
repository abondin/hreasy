package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;
import java.time.LocalDate;

/**
 *
 */
@Data
@Builder
@ToString
public class TimesheetQueryFilter {
    @NonNull
    private LocalDate from;
    @NonNull
    private LocalDate to;

    @Nullable
    private Integer ba;
    @Nullable
    private Integer project;
}
