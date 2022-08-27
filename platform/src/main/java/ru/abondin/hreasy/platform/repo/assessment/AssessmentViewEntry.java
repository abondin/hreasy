package ru.abondin.hreasy.platform.repo.assessment;

import lombok.Data;

@Data
public class AssessmentViewEntry extends AssessmentEntry{
    private String employeeLastname;
    private String employeeFirstname;
    private String employeePatronymicName;


    private String createdByLastname;
    private String createdByFirstname;
    private String createdByPatronymicName;

    private String completedByLastname;
    private String completedByFirstname;
    private String completedByPatronymicName;

    private String canceledByLastname;
    private String canceledByFirstname;
    private String canceledByPatronymicName;
}
