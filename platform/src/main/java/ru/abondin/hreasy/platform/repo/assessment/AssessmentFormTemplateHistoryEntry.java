package ru.abondin.hreasy.platform.repo.assessment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("assessment_form_template_history")
public class AssessmentFormTemplateHistoryEntry {
    @Id
    private Integer id;
    /**
     * Form Type
     * <ul>
     *     <li>1 - self assessment</li>
     *     <li>2 - manager feedback</li>
     *     <li>3 - meeting notes</li>
     *     <li>4 - conclusion and decision</li>
     * </ul>
     */
    private Integer formType;
    private String content;
    private OffsetDateTime createdAt;
    private Integer createdBy;
}
