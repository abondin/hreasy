package ru.abondin.hreasy.platform.repo.assessment;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeAssessmentEntry extends AssessmentEntry {
    private Integer employeeId;
    private String employeeLastname;
    private String employeeFirstname;
    private String employeePatronymicName;
    private int employeeCurrentProjectId;
    private String employeeCurrentProjectName;
    private LocalDate employeeDateOfEmployment;
}