package ru.abondin.hreasy.platform.repo.salary;

import java.time.LocalDate;

public class SalaryRequestView extends SalaryRequestEntry {
    private String employeeDisplayName;
    private String budgetBusinessAccountName;
    private LocalDate assessmentPlannedDate;
    private String createdByDisplayName;

    private Integer employeeDepartmentId;
    private String employeeDepartmentName;
}
