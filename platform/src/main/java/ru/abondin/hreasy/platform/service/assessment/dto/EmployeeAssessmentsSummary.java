package ru.abondin.hreasy.platform.service.assessment.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;

import java.time.LocalDate;

/**
 * Short info about employee and his last assessment
 */
@Data
public class EmployeeAssessmentsSummary {
    private int employeeId;
    private String displayName;
    private Integer lastAssessmentId;
    private LocalDate lastAssessmentDate;
    // In summary table we need only date when assessment completed. Do not forget to convert from OffsetDateTime
    private LocalDate lastAssessmentCompletedDate;
    private LocalDate employeeDateOfEmployment;
    /**
     * Latest planned assessment or date of employment
     */
    private LocalDate latestActivity;
    private Long daysWithoutAssessment;
    private CurrentProjectDictDto currentProject;
}
