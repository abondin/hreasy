package ru.abondin.hreasy.platform.service.skills.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RatingsMapperTest {
    private final RatingsMapper mapper = new RatingsMapper() {};

    @Test
    void preservesUnratedSkillsAlongsideRatedSkills() {
        var skills = mapper.parseAssembledSkills(
                "301|#|Java|#|501|#|Backend|#||$|302|#|SQL|#|501|#|Backend|#|201,4/202,2", 201);

        assertEquals(2, skills.size());
        var unrated = skills.getFirst();
        assertEquals(301, unrated.getId());
        assertEquals("Java", unrated.getName());
        assertEquals(501, unrated.getGroup().getId());
        assertEquals("Backend", unrated.getGroup().getName());
        assertEquals(0, unrated.getRatings().getRatingsCount());
        assertNull(unrated.getRatings().getAverageRating());
        assertNull(unrated.getRatings().getMyRating());

        var rated = skills.getLast();
        assertEquals(2, rated.getRatings().getRatingsCount());
        assertEquals(3f, rated.getRatings().getAverageRating());
        assertEquals(4f, rated.getRatings().getMyRating());
    }
}
