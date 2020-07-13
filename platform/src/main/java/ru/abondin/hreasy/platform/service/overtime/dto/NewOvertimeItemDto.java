package ru.abondin.hreasy.platform.service.overtime.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NewOvertimeItemDto {
    /**
     * YYYY-MM-DD format
     */
    private LocalDate date;
    private int projectId;
    private int hours;
    private String notes;
}
