package ru.abondin.hreasy.platform.repo.employee.admin.kids;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class EmployeeKidView extends EmployeeKidEntry{
    private String parentDisplayName;
    private boolean parentNotDismissed;
}
