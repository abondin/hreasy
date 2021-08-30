package ru.abondin.hreasy.platform.service.assessment.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Short info about employee and his last assessment
 */
@Data
public class EmployeeAssessmentsSummary {
    private int employeeId;
    private String displayName;
    private Integer lastAssessmentId;
    private LocalDate lastAssessmentDate;
    private OffsetDateTime lastAssessmentCompletedDate;
    private LocalDate employeeDateOfEmployment;
    /**
     * Latest planned assessment or date of employment
     */
    private LocalDate latestActivity;
    private Long daysWithoutAssessment;
    private SimpleDictDto currentProject;
}
