package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VacationDto {
    private int id;
    private int employee;
    private String employeeDisplayName;
    private Integer employeeCurrentProject;
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
}
