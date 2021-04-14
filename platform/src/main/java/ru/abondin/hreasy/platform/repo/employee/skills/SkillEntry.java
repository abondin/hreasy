package ru.abondin.hreasy.platform.repo.employee.skills;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("skill")
public class SkillEntry {
    @Id
    private Integer id;

    private Integer employeeId;

    private Integer groupId;

    private String name;

    private boolean shared;

    private OffsetDateTime createdAt;

    private Integer createdBy;

    private OffsetDateTime deletedAt;

    private Integer deletedBy;
}
