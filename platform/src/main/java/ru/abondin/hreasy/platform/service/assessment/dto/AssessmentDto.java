package ru.abondin.hreasy.platform.service.assessment.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Base assessment information
 */
@Data
public class AssessmentDto {
    private int id;
    private int employee;
    private LocalDate plannedDate;
    private OffsetDateTime createdAt;
    private SimpleDictDto createdBy;
    private OffsetDateTime completedAt;
    private SimpleDictDto completedBy;
    private OffsetDateTime canceledAt;
    private SimpleDictDto canceledBy;
}
