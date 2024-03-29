package ru.abondin.hreasy.platform.repo.assessment;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeAssessmentEntry extends AssessmentEntry {
    private Integer employeeId;
    private String employeeDisplayName;
    private int employeeCurrentProjectId;
    private String employeeCurrentProjectName;
    private String employeeCurrentProjectRole;
    private int baId;
    private String baName;
    private LocalDate employeeDateOfEmployment;
}
