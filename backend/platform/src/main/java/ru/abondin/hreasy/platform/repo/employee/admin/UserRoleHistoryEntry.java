package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Table("sec.user_role_history")
public class UserRoleHistoryEntry {
    @Id
    private Integer id;
    private int employeeId;
    private List<String> roles;
    private List<Integer> accessibleProjects;
    private List<Integer> accessibleDepartments;
    private List<Integer> accessibleBas;

    private int createdBy;
    private OffsetDateTime createdAt;
}
