package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@ToString
@Builder()
public class TimesheetReportBody {
    @NotNull
    private int businessAccount;
    private Integer project;
    @NotNull
    private LocalDate date;
    private short hoursPlanned;
    private short hoursSpent;
}
