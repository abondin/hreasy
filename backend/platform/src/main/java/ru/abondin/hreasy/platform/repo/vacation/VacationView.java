package ru.abondin.hreasy.platform.repo.vacation;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class VacationView extends VacationEntry {
    private String employeeDisplayName;
    private String employeeEmail;
    private Integer employeeCurrentProject;
    private String employeeCurrentProjectName;
    private String employeeCurrentProjectRole;
}
