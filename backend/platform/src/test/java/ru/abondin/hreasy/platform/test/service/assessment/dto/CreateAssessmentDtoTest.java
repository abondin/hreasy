package ru.abondin.hreasy.platform.test.service.assessment.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import ru.abondin.hreasy.platform.service.assessment.dto.CreateAssessmentDto;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateAssessmentDtoTest {

    /**
     * Test goal: verifies that the assessment creation request body can be decoded from JSON.
     * <p>Precondition: JSON contains the payload sent by the frontend for scheduling an assessment.
     * <p>Action: deserialize JSON into {@link CreateAssessmentDto} with the platform Java time module.
     * <p>Verification: planned date and manager ids are populated from the request body.
     */
    @Test
    void deserializesAssessmentCreationRequest() throws Exception {
        var mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        var json = """
                {
                  "plannedDate": "2026-06-10",
                  "managers": [16, 42]
                }
                """;

        var dto = mapper.readValue(json, CreateAssessmentDto.class);

        assertEquals(LocalDate.of(2026, 6, 10), dto.getPlannedDate());
        assertEquals(List.of(16, 42), dto.getManagers());
    }
}
