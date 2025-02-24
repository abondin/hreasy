package ru.abondin.hreasy.platform.repo.employee;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("empl.v_employee_project_changes")
public class EmployeeProjectChangesEntry {
    @Id
    private Integer id;
    private Integer employeeId;
    private String employeeDisplayName;
    private Integer projectId;
    private String projectName;
    private String projectRole;
    private Integer baId;
    private String baName;
    private Integer changedById;
    private String changedByDisplayName;
    private OffsetDateTime changedAt;
}
