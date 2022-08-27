package ru.abondin.hreasy.platform.repo.manager;

import lombok.Data;

@Data
public class ManagerView extends ManagerEntry {
    private String employeeDisplayName;
    private Boolean employeeActive;
    private String objectName;
}
