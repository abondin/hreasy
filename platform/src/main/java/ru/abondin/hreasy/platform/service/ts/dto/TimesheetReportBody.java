package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Update employee's timesheet on given project
 */
@Data
@ToString
@Builder()
public class TimesheetReportBody {
    public record TimesheetReportOneDay(@NotNull
                                        LocalDate date, short hoursSpent) {
    }

    @NotNull
    private int businessAccount;
    private Integer project;
    private String comment;

    private List<TimesheetReportOneDay> hours = new ArrayList<>();
}
