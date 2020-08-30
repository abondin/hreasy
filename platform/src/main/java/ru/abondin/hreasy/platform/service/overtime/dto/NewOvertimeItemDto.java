package ru.abondin.hreasy.platform.service.overtime.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewOvertimeItemDto {
    /**
     * YYYY-MM-DD format
     */
    private LocalDate date;
    private int projectId;
    private int hours;
    private String notes;
}
