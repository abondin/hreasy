package ru.abondin.hreasy.platform.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Employee information, available to any authenticated user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProjectChangesDto {
    private Integer id;
    private SimpleDictDto employee;
    private CurrentProjectDictDto project;
    private SimpleDictDto ba;
    private SimpleDictDto changedBy;
    private OffsetDateTime changedAt;
}
