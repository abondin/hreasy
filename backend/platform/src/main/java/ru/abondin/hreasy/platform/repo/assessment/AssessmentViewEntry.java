package ru.abondin.hreasy.platform.repo.assessment;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AssessmentViewEntry extends AssessmentEntry{
    private String employeeDisplayName;


    private String createdByDisplayName;

    private String completedByDisplayName;

    private String canceledByDisplayName;
}
