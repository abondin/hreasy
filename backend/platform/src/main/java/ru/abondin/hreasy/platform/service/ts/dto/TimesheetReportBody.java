package ru.abondin.hreasy.platform.service.ts.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
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
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@AllArgsConstructor
public class TimesheetReportBody {
    public record TimesheetReportOneDay(@NonNull
                                        LocalDate date, short hoursSpent) {
    }

    @NonNull
    private int businessAccount;
    private Integer project;
    private String comment;

    @Builder.Default
    private List<TimesheetReportOneDay> hours = new ArrayList<>();
}
