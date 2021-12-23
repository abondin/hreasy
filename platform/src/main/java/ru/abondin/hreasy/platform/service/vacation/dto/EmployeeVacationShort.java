package ru.abondin.hreasy.platform.service.vacation.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeVacationShort {
    private int id;
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;
}
