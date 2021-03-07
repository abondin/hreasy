package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("sec_user_role_history")
public class UserRoleHistoryEntry {
    @Id
    private Integer id;
    private int employeeId;
    private int userId;
    private String roles;
    private String accessibleProjects;
    private String accessibleDepartments;

    private int updatedBy;
    private OffsetDateTime updatedAt;
}
