package ru.abondin.hreasy.platform.repo.employee;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("v_employee_detailed")
public class EmployeeDetailedEntry extends EmployeeEntry {
    private String departmentName;
    private String positionName;
    private String positionCategory;
    private String levelName;
    private String currentProjectName;
    private String officeLocationName;
}
