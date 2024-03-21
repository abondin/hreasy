package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Data;

import org.springframework.lang.NonNull;
import java.time.LocalDate;

@Data
public class TimesheetDto {
    @NonNull
    private int employee;
    private int businessAccount;
    private Integer project;
    @NonNull
    private LocalDate date;
    private short hoursSpent;
    private String comment;
}
