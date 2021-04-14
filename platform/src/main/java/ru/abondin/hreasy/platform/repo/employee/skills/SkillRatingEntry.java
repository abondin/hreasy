package ru.abondin.hreasy.platform.repo.employee.skills;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("skill_rating")
public class SkillRatingEntry {
    @Id
    private Integer id;

    private Integer skillId;

    private Float rating;

    private String notes;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private Integer createdBy;

    private OffsetDateTime deletedAt;

    private Integer deletedBy;
}
