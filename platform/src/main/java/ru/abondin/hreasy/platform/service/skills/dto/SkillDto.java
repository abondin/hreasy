package ru.abondin.hreasy.platform.service.skills.dto;

import lombok.Data;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import javax.validation.constraints.NotNull;

/**
 * Employee skill
 */
@Data
public class SkillDto {

    /**
     * Programming language, framework, foreign languages, etc.
     */
    @NotNull
    private SimpleDictDto group;

    /**
     * Java, Spring, French, etc
     */
    @NotNull
    private String name;

    /**
     * From 1 to 5
     */
    @Nullable
    private Float rating;
}
