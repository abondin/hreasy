package ru.abondin.hreasy.platform.repo.manager;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ManagerView extends ManagerEntry {
    private String employeeDisplayName;
    private Boolean employeeActive;
    private String objectName;
}
