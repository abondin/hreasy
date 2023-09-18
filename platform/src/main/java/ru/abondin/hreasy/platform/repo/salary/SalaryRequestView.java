package ru.abondin.hreasy.platform.repo.salary;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SalaryRequestView extends SalaryRequestEntry {
    private String employeeDisplayName;
    private String budgetBusinessAccountName;
    private LocalDate assessmentPlannedDate;
    private String createdByDisplayName;

    private Integer employeeDepartmentId;
    private String employeeDepartmentName;
}
