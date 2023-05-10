package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class TimesheetRecordDto {
    @NotNull
    private Integer id;
    @NotNull
    private int employee;
    @NotNull
    private int businessAccount;
    private Integer project;
    @NotNull
    private LocalDate date;
    private short hoursSpent;
}
