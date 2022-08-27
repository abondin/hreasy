package ru.abondin.hreasy.platform.service.vacation.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MyVacationDto {
    private int id;
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;

    private VacationDto.VacationStatus status = VacationDto.VacationStatus.PLANNED;

    private String documents;

    private int daysNumber;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
}
