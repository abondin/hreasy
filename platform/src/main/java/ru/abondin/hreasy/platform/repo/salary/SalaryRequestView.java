package ru.abondin.hreasy.platform.repo.salary;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SalaryRequestView extends SalaryRequestEntry {

    private String employeeDisplayName;

    private String budgetBusinessAccountName;
    private LocalDate assessmentPlannedDate;
    private String createdByDisplayName;
    private String implementedByDisplayName;

    private Integer infoEmplPosition;
    private String infoEmplPositionName;
    private String infoEmplProjectName;
    private String infoEmplBaName;
    private String implNewPositionName;

}
