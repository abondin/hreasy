package ru.abondin.hreasy.platform.service.overtime.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class OvertimeItemDto {
    private Integer id;

    /**
     * YYYY-MM-DD format
     */
    private LocalDate date;
    private int projectId;
    private int hours;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime deletedAt;

}
