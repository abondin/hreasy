package ru.abondin.hreasy.platform.service.skills.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import org.springframework.lang.NonNull;

/**
 * Employee skill
 */
@Data
@NoArgsConstructor
public class SkillDto {

    /**
     * Programming language, framework, foreign languages, etc.
     */
    @NonNull
    private SimpleDictDto group;

    private Integer id;

    /**
     * Java, Spring, French, etc
     */
    @NonNull
    private String name;

    @Nullable
    private Ratings ratings;

    @Data
    public static class Ratings{
        /**
         * From 1 to 5
         */
        @Nullable
        private Float averageRating;

        /**
         * From 1 to 5
         */
        @Nullable
        private Float myRating;


        @Nullable
        private int ratingsCount;
    }
}
