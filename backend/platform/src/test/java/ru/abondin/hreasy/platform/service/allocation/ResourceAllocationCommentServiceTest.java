package ru.abondin.hreasy.platform.service.allocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationCommentDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.abondin.hreasy.platform.service.allocation.ResourceAllocationSecurityValidator.READ_PERMISSION;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = PostgreSQLTestContainerContextInitializer.class)
class ResourceAllocationCommentServiceTest extends BaseServiceTest {

    @Autowired
    private ResourceAllocationCommentService service;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        initEmployeesDataAndLogin();
        databaseClient.sql("delete from alloc.resource_allocation_comment")
                .fetch().rowsUpdated().block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    void persistsListsUpdatesProtectsAndDeletesCommentsOnTheTestDatabase() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var projectId = testData.project_M1_Billing();
        var period = 202600;
        var cell = new ResourceAllocationCommentDto.CreateBody(
                period, employeeId, projectId, null, "  Needs review  ");

        var created = service.create(cell, auth).block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(created);
        assertEquals("Needs review", created.text());
        assertTrue(created.mine());

        var comments = service.getComments(period, employeeId, projectId, null, auth)
                .block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(comments);
        assertEquals(List.of(created.id()), comments.stream().map(ResourceAllocationCommentDto::id).toList());

        var summary = service.getSummary(2026, auth).block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(summary);
        assertEquals(1, summary.rows().getFirst().cells().getFirst().commentCount());

        var otherReader = new AuthContext("reader", "reader@example.test", List.of(READ_PERMISSION),
                new AuthContext.EmployeeInfo(
                        testData.employees.get(TestEmployees.FMS_Empl_Ammara_Knott),
                        null, null, List.of(), List.of(), List.of(projectId), null, null));
        StepVerifier.create(service.update(created.id(),
                        new ResourceAllocationCommentDto.UpdateBody("Changed"), otherReader))
                .expectError(AccessDeniedException.class)
                .verify(MONO_DEFAULT_TIMEOUT);

        var updated = service.update(created.id(), new ResourceAllocationCommentDto.UpdateBody(" Changed "), auth)
                .block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(updated);
        assertEquals("Changed", updated.text());
        assertNotNull(updated.updatedAt());

        service.delete(created.id(), auth).block(MONO_DEFAULT_TIMEOUT);
        var afterDelete = service.getComments(period, employeeId, projectId, null, auth)
                .block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(afterDelete);
        assertTrue(afterDelete.isEmpty());
        assertFalse(databaseClient.sql("select id from alloc.resource_allocation_comment")
                .map((row, _) -> row.get("id", Integer.class)).all().hasElements().block(MONO_DEFAULT_TIMEOUT));
    }
}
