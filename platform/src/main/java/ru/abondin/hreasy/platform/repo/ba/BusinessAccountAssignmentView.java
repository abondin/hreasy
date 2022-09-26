package ru.abondin.hreasy.platform.repo.ba;

import lombok.Data;


@Data
public class BusinessAccountAssignmentView extends BusinessAccountAssignmentEntry {
    private String businessAccountName;
    private String employeeDisplayName;
    private String projectName;
    private String closedByDisplayName;
}
