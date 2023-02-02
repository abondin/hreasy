package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@ToString
public class TimesheetReportBody {
    @NotNull
    private int businessAccount;
    private Integer project;
    @NotNull
    private LocalDate date;
    private short hours;
}
