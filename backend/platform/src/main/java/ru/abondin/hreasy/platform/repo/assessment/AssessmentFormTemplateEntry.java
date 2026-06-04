package ru.abondin.hreasy.platform.repo.assessment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("assmnt.assessment_form_template")
public class AssessmentFormTemplateEntry {
    /**
     * Form Type
     * <ul>
     *     <li>1 - self assessment</li>
     *     <li>2 - manager feedback</li>
     *     <li>3 - meeting notes</li>
     *     <li>4 - conclusion and decision</li>
     * </ul>
     */
    @Id
    private Integer formType;
    private String content;
}
