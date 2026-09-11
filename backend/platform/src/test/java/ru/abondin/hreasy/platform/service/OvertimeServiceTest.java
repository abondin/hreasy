package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.overtime.OvertimeService;
import ru.abondin.hreasy.platform.service.overtime.dto.NewOvertimeItemDto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class OvertimeServiceTest extends BaseServiceTest {

    private final static Duration MONO_DEFAULT_TIMEOUT = Duration.ofSeconds(3);


    @Autowired
    private OvertimeService overtimeService;


    @Autowired
    private DatabaseClient db;

    @BeforeEach
    protected void beforeEach() {
        initEmployeesDataAndLogin();
        cleanOvertimesTables().block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testGetOvertimeNoPermissions() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Ammara_Knott).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(overtimeService.getOrStub(jensonId, 202008, ctx))
                .expectError(AccessDeniedException.class).verify(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testGetOvertimeSummary() {
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        // Generate unique notes to filter new created item
        var uniqueNotes = "testGetOvertimeSummary" + UUID.randomUUID().toString();
        StepVerifier
                .create(
                        overtimeService.addItem(
                                ctx.getEmployeeInfo().getEmployeeId(),
                                202008,
                                new NewOvertimeItemDto(LocalDate.now(), testData.project_M1_Billing(), 1, uniqueNotes),
                                ctx).thenMany(overtimeService.getSummary(202008, ctx)))
                //TODO Validate the actual result
                .expectNextMatches(o -> true).verifyComplete();
    }

    @Test
    public void testGetOvertimeSummaryNoPermissions() {
        var ctx = auth(TestEmployees.FMS_Empl_Ammara_Knott).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(overtimeService.getSummary(202008, ctx))
                .expectError(AccessDeniedException.class).verify(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testReportMyOvertime() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(
                        overtimeService.addItem(
                                jensonId,
                                202008,
                                new NewOvertimeItemDto(LocalDate.now(), testData.project_M1_Billing(), 4, null),
                                ctx))
                .expectNextCount(1).verifyComplete();
    }

    @Test
    public void testReportOvertimeForProjectWorkstream() {
        var employeeId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        var summaryCtx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var date = LocalDate.now();
        var workstreamId = db.sql("""
                        insert into proj.project_workstream
                            (project_id, external_id, display_name, created_at, created_by)
                        values (:projectId, :externalId, :displayName, :createdAt, :createdBy)
                        returning id
                        """)
                .bind("projectId", testData.project_M1_Billing())
                .bind("externalId", "test-overtime-workstream-" + UUID.randomUUID())
                .bind("displayName", "Test Workstream")
                .bind("createdAt", OffsetDateTime.now())
                .bind("createdBy", employeeId)
                .map((row, _) -> row.get("id", Integer.class))
                .one().block(MONO_DEFAULT_TIMEOUT);

        StepVerifier.create(overtimeService.addItem(employeeId, 202008,
                        new NewOvertimeItemDto(date, testData.project_M1_Billing(), workstreamId, 4, null), ctx)
                .doOnNext(report -> assertEquals(workstreamId, report.getItems().getFirst().getWorkstreamId()))
                .then(overtimeService.addItem(employeeId, 202008,
                        new NewOvertimeItemDto(date, testData.project_M1_Billing(), 2, null), ctx))
                .then(db.sql("update proj.project_workstream set deleted_at = :deletedAt where id = :id")
                        .bind("deletedAt", OffsetDateTime.now())
                        .bind("id", workstreamId)
                        .then())
                .then(overtimeService.getOrStub(employeeId, 202008, ctx))
                .doOnNext(report -> assertEquals("Test Workstream", report.getItems().stream()
                        .filter(item -> workstreamId.equals(item.getWorkstreamId()))
                        .findFirst().orElseThrow().getWorkstreamDisplayName()))
                .thenMany(overtimeService.getSummary(202008, summaryCtx))
                .filter(summary -> summary.getEmployeeId() == employeeId))
                .assertNext(summary -> {
                    assertEquals(1, summary.getItems().size());
                    assertEquals(6, summary.getItems().getFirst().getHours());
                })
                .verifyComplete();
    }

    @Test
    public void testDeleteMyOvertime() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        var uuidComment = UUID.randomUUID().toString();
        StepVerifier
                .create(
                        overtimeService.addItem(
                                        jensonId,
                                        202008,
                                        new NewOvertimeItemDto(LocalDate.now(), testData.project_M1_Billing(), 7, uuidComment),
                                        ctx)
                                .flatMap(r -> {
                                    var itemId = r.getItems().stream().filter(i -> uuidComment.equals(i.getNotes())).findFirst().get().getId();
                                    return overtimeService.deleteItem(jensonId, 202008, itemId, ctx);
                                })
                ).expectNextCount(1).verifyComplete();
    }


    @Test
    public void testAddOvertimeItemForEmployeeFromAnotherProject() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.Billing_Manager_Maxwell_May).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(overtimeService.addItem(
                        jensonId,
                        202008,
                        new NewOvertimeItemDto(LocalDate.now(), testData.project_M1_Billing(), 2, "testAddOvertimeItemForEmployeeFromAnotherProject"),
                        ctx))
                .expectError(AccessDeniedException.class).verify(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testAddOvertimeOfMyProject() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(overtimeService.addItem(
                        jensonId,
                        202008,
                        new NewOvertimeItemDto(LocalDate.now(), testData.project_M1_Billing(), 6, "testAddOvertimeOfMyProject"),
                        ctx))
                .expectNextCount(1).verifyComplete();
    }

    /**
     * Do not clean all database, but delete only overtimes tables
     *
     * @return
     */
    private Mono<Void> cleanOvertimesTables() {
        return db.sql("delete from ovt.overtime_item").then()
                .then(db.sql("delete from ovt.overtime_item").then())
                .then(db.sql("delete from ovt.overtime_approval_decision").then())
                .then(db.sql("delete from ovt.overtime_period_history").then())
                .then(db.sql("delete from ovt.overtime_closed_period").then())
                .then(db.sql("delete from ovt.overtime_report").then());
    }
}
