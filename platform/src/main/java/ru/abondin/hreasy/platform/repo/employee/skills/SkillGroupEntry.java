package ru.abondin.hreasy.platform.repo.employee.skills;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("empl.skill_group")
public class SkillGroupEntry {
    @Id
    private Integer id;

    private String name;

    private Boolean archived;
}
