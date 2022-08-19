package ru.abondin.hreasy.platform.service.vacation.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Vacation projection for exported excel document
 */
@Data
public class VacationExportDto {
    private String employeeDisplayName;
    private String employeeCurrentProject;
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private String status;
    private String documents;
    private int daysNumber;
}
