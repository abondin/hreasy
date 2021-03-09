package ru.abondin.hreasy.platform.repo.employee.skills;

import lombok.Data;

@Data
public class SkillWithRatingEntry extends SkillEntry {
    private float averageRating;

    private String groupName;
}
