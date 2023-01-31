package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Filter timesheet grouped by employee
 */
@Data
public class TimesheetAggregatedFilter {
    @NotNull
    private LocalDate from;
    @NotNull
    private LocalDate to;
}
