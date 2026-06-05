package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Update employee's timesheet on given project
 */
@Data
@ToString
@Builder()
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetReportBody {
    public record TimesheetReportOneDay(@NonNull
                                        LocalDate date, short hoursSpent) {
    }

    @NonNull
    private int businessAccount;
    private Integer project;
    private String comment;

    private List<TimesheetReportOneDay> hours = new ArrayList<>();
}
