package ru.abondin.hreasy.platform.service.skills.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillRatingDto {
    /**
     * Who rate the skill
     */
    private int employeeId;

    private Float rating;
}
