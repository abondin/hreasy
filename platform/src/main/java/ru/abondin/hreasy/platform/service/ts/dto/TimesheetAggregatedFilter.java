package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Filter timesheet grouped by employee
 */
@Data
@Builder
@ToString
public class TimesheetAggregatedFilter {
    @NotNull
    private LocalDate from;
    @NotNull
    private LocalDate to;
}
