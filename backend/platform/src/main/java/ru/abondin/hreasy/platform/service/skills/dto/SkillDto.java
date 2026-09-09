package ru.abondin.hreasy.platform.service.skills.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Employee skill and its available ratings.")
public class SkillDto {

    /**
     * Programming language, framework, foreign languages, etc.
     */
    @NonNull
    @Schema(description = "Skill category, such as programming languages or frameworks.")
    private SimpleDictDto group;

    @Schema(description = "HR Easy skill identifier.")
    private Integer id;

    /**
     * Java, Spring, French, etc
     */
    @NonNull
    @Schema(description = "Skill display name.", example = "Java")
    private String name;

    @Nullable
    @Schema(description = "Skill ratings, when available.", nullable = true)
    private Ratings ratings;

    @Data
    @Schema(name = "SkillRatings", description = "Ratings for an employee skill.")
    public static class Ratings{
        /**
         * From 1 to 5
         */
        @Nullable
        @Schema(description = "Average skill rating.", minimum = "1", maximum = "5", nullable = true)
        private Float averageRating;

        /**
         * From 1 to 5
         */
        @Nullable
        @Schema(description = "Skill rating from the acting user.", minimum = "1", maximum = "5", nullable = true)
        private Float myRating;


        @Nullable
        @Schema(description = "Number of ratings.", minimum = "0")
        private int ratingsCount;
    }
}
