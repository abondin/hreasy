package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 *
 */
@Data
@Builder
@ToString
public class TimesheetQueryFilter {
    @NotNull
    private LocalDate from;
    @NotNull
    private LocalDate to;

    private int ba;

    private Integer project;
}
