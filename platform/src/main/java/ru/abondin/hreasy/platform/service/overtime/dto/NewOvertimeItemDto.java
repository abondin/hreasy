package ru.abondin.hreasy.platform.service.overtime.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"notes"})
public class NewOvertimeItemDto {
    /**
     * YYYY-MM-DD format
     */
    private LocalDate date;
    private int projectId;
    private float hours;
    private String notes;
}
