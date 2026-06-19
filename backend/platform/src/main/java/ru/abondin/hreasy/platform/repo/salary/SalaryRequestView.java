package ru.abondin.hreasy.platform.repo.salary;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
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
    private String reqNewPositionName;

    private Json approvals;
    private Json links;

}
