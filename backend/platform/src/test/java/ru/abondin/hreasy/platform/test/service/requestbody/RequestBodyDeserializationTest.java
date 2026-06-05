package ru.abondin.hreasy.platform.test.service.requestbody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestImplementBody;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestRejectBody;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportBody;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestUpdateBody;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetReportBody;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestBodyDeserializationTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Test goal: verifies that salary request bodies with Lombok builders can be decoded from JSON.
     * <p>Precondition: JSON contains payloads sent by the frontend for salary and bonus workflows.
     * <p>Action: deserialize JSON into salary request body DTOs with the platform Java time module.
     * <p>Verification: representative fields are populated from the request bodies.
     */
    @Test
    void deserializesSalaryRequestBodies() throws Exception {
        var report = mapper.readValue("""
                {
                  "employeeId": 1190,
                  "type": 1,
                  "budgetBusinessAccount": 604,
                  "budgetExpectedFundingUntil": "2026-07-01",
                  "increaseAmount": 15000,
                  "increaseStartPeriod": 202606,
                  "reason": "Performance review"
                }
                """, SalaryRequestReportBody.class);
        var update = mapper.readValue("""
                {
                  "budgetExpectedFundingUntil": "2026-08-01",
                  "plannedSalaryAmount": 250000,
                  "newPosition": 12,
                  "comment": "Updated"
                }
                """, SalaryRequestUpdateBody.class);
        var implement = mapper.readValue("""
                {
                  "increaseAmount": 10000,
                  "salaryAmount": 240000,
                  "increaseStartPeriod": 202607,
                  "comment": "Implemented"
                }
                """, SalaryRequestImplementBody.class);
        var reject = mapper.readValue("""
                {
                  "reason": "Budget is not approved",
                  "comment": "Try next quarter",
                  "rescheduleToNewPeriod": 202610
                }
                """, SalaryRequestRejectBody.class);

        assertEquals(1190, report.getEmployeeId());
        assertEquals(LocalDate.of(2026, 8, 1), update.getBudgetExpectedFundingUntil());
        assertEquals(new BigDecimal("10000"), implement.getIncreaseAmount());
        assertEquals(202610, reject.getRescheduleToNewPeriod());
    }

    /**
     * Test goal: verifies that the timesheet report body with a Lombok builder can be decoded from JSON.
     * <p>Precondition: JSON contains the payload sent by the frontend for reporting project hours.
     * <p>Action: deserialize JSON into {@link TimesheetReportBody} with the platform Java time module.
     * <p>Verification: project identifiers and day records are populated from the request body.
     */
    @Test
    void deserializesTimesheetReportBody() throws Exception {
        var body = mapper.readValue("""
                {
                  "businessAccount": 604,
                  "project": 1162,
                  "comment": "June work",
                  "hours": [
                    {"date": "2026-06-04", "hoursSpent": 8}
                  ]
                }
                """, TimesheetReportBody.class);

        assertEquals(604, body.getBusinessAccount());
        assertEquals(1162, body.getProject());
        assertEquals(1, body.getHours().size());
        assertEquals(LocalDate.of(2026, 6, 4), body.getHours().getFirst().date());
        assertEquals(8, body.getHours().getFirst().hoursSpent());
    }
}
