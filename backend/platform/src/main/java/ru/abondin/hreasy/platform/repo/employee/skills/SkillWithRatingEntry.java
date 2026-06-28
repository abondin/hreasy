package ru.abondin.hreasy.platform.repo.employee.skills;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SkillWithRatingEntry extends SkillEntry {
    private String ratings;

    private String groupName;
}
