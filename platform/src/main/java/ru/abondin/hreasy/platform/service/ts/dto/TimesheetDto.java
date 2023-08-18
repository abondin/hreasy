package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class TimesheetDto {
    @NotNull
    private int employee;
    private int businessAccount;
    private Integer project;
    @NotNull
    private LocalDate date;
    private short hoursSpent;
    private String comment;
}
