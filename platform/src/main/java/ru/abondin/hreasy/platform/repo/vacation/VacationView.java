package ru.abondin.hreasy.platform.repo.vacation;

import lombok.Data;

@Data
public class VacationView extends VacationEntry {
    private String employeeDisplayName;
    private String employeeEmail;
    private Integer employeeCurrentProject;
    private String employeeCurrentProjectName;
}
