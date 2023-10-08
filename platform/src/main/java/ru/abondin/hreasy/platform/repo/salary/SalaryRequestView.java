package ru.abondin.hreasy.platform.repo.salary;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SalaryRequestView extends SalaryRequestEntry {
    private String employeeDisplayName;
    private Integer employeeCurrentProjectId;
    private String employeeCurrentProjectName;
    private String employeeCurrentProjectRole;
    private String budgetBusinessAccountName;
    private LocalDate assessmentPlannedDate;
    private String createdByDisplayName;
    private String implementedByDisplayName;

    private Integer employeePositionId;
    private String employeePositionName;

    private String implNewPositionName;

}
