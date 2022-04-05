package ru.abondin.hreasy.platform.repo.employee.admin.kids;

import lombok.Data;

@Data
public class EmployeeKidView extends EmployeeKidEntry{
    private String parentFirstname;
    private String parentLastname;
    private String parentPatronymicName;
    private boolean parentNotDismissed;
}
