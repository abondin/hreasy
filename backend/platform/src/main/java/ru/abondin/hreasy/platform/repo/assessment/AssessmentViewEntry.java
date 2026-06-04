package ru.abondin.hreasy.platform.repo.assessment;

import lombok.Data;

@Data
public class AssessmentViewEntry extends AssessmentEntry{
    private String employeeDisplayName;


    private String createdByDisplayName;

    private String completedByDisplayName;

    private String canceledByDisplayName;
}
