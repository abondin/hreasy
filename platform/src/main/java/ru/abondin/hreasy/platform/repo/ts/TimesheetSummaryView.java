package ru.abondin.hreasy.platform.repo.ts;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetQueryFilter;

/**
 * Aggregated by employee timesheet filtered by {@link TimesheetQueryFilter}
 *
 * @see TimesheetRecordEntry
 * @see TimesheetQueryFilter
 */
@Data
public class TimesheetSummaryView {
    private int employeeId;
    private String employeeDisplayName;
    private Integer employeeBa;
    private Integer employeeCurrentProject;
    private Json timesheet;
    private Json vacations;
}
