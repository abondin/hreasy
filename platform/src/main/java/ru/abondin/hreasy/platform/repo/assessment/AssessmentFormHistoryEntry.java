package ru.abondin.hreasy.platform.repo.assessment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("assessment_form_history")
public class AssessmentFormHistoryEntry {
    @Id
    private Integer id;
    private Integer assessmentFormId;
    private String content;
    private OffsetDateTime createdAt;
    private Integer createdBy;
}
