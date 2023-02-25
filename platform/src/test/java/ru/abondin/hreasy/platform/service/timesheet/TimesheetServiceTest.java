package ru.abondin.hreasy.platform.service.timesheet;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
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
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordRepo;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.ts.TimesheetService;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetAggregatedFilter;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetReportBody;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class TimesheetServiceTest extends BaseServiceTest {

    private final static Duration MONO_DEFAULT_TIMEOUT = Duration.ofSeconds(3);

    private final LocalDate defaultDate = LocalDate.of(2023, Month.FEBRUARY.getValue(), 02);

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private TimesheetRecordRepo repo;


    @Autowired
    private DatabaseClient db;

    @BeforeEach
    protected void beforeEach() {
        initEmployeesDataAndLogin();
        cleanTimesheetTables().block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testReportMyTimesheet() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        var date = LocalDate.of(2023, Month.FEBRUARY.getValue(), 02);
        var ba = testData.ba_RND();
        var hoursPlanned = (short) 8;
        var hoursSpent = (short) 4;
        var report = TimesheetReportBody.builder()
                .businessAccount(ba)
                .date(date)
                .hoursPlanned(hoursPlanned)
                .hoursSpent(hoursSpent)
                .project(null).build();
        StepVerifier
                .create(timesheetService.report(ctx, jensonId, report)
                        .flatMap(repo::findById)
                )
                .expectNextMatches((entry) ->
                        entry.getBusinessAccount() == ba
                                && entry.getProject() == null
                                && entry.getDate().equals(date)
                                && entry.getEmployee().equals(jensonId)
                                && entry.getHoursPlanned() == hoursPlanned
                                && entry.getHoursSpent() == hoursSpent
                                && entry.getCreatedBy() == jensonId)
                .verifyComplete();
    }

    @Test
    public void testReportByProjectManager() {
        var employeeId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var date = LocalDate.of(2023, Month.FEBRUARY.getValue(), 02);
        var ba = testData.ba_RND();
        var project = testData.project_M1_FMS();
        var hours = (short) 8;
        var report = TimesheetReportBody.builder()
                .businessAccount(ba)
                .date(date)
                .hoursPlanned(hours)
                .project(project).build();
        StepVerifier
                .create(timesheetService.report(ctx, employeeId, report)
                        .flatMap(repo::findById)
                )
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void testReportByProjectManagerFromAnotherProject() {
        var employeeId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.Billing_Manager_Maxwell_May).block(MONO_DEFAULT_TIMEOUT);
        var date = defaultDate;
        var ba = testData.ba_RND();
        var project = testData.project_M1_FMS();
        var hours = (short) 8;
        var report = TimesheetReportBody.builder()
                .businessAccount(ba)
                .date(date)
                .hoursSpent(hours)
                .project(project).build();
        StepVerifier
                .create(timesheetService.report(ctx, employeeId, report)
                        .flatMap(repo::findById)
                )
                .expectError(AccessDeniedException.class);
    }


    @Test
    public void testGetSummary() {
        var employeeId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        generateDefaultReports(ctx).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier.create(timesheetService.timesheetSummary(ctx, TimesheetAggregatedFilter
                .builder().from(defaultDate).to(defaultDate.plusDays(1)).build())).expectNextCount(2).verifyComplete();
    }

    @Test
    public void testGetSummaryNoAccess() {
        var employeeId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        generateDefaultReports(ctx).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier.create(timesheetService.timesheetSummary(ctx, TimesheetAggregatedFilter
                .builder().from(defaultDate).to(defaultDate).build())).expectError(AccessDeniedException.class);
        ;
    }

    @Test
    public void testGetSummaryFilter() {
        var employeeId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        generateDefaultReports(ctx).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier.create(timesheetService.timesheetSummary(ctx, TimesheetAggregatedFilter
                .builder().from(defaultDate).to(defaultDate).build())).expectNextCount(1).verifyComplete();
    }

    @Test
    public void testDeleteReport() {
        var employeeId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var reports = generateDefaultReports(ctx).block(MONO_DEFAULT_TIMEOUT);
        Assertions.assertEquals(2, timesheetService.timesheetSummary(ctx, TimesheetAggregatedFilter
                .builder().from(defaultDate).to(defaultDate.plusDays(1)).build()).collectList().block(MONO_DEFAULT_TIMEOUT).size());
        timesheetService.delete(ctx, employeeId, reports.get("1:2023-02-02:6")).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier.create(timesheetService.timesheetSummary(ctx, TimesheetAggregatedFilter
                .builder().from(defaultDate).to(defaultDate.plusDays(1)).build())).expectNextCount(1).verifyComplete();
    }

    @Test
    public void testDeleteNotMyReport() {
        var ctx = auth(TestEmployees.Admin_Shaan_Pitts).block(MONO_DEFAULT_TIMEOUT);
        var employeeId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        generateDefaultReports(ctx).block(MONO_DEFAULT_TIMEOUT);

        var employee2Id = testData.employees.get(TestEmployees.Integrator_Jonas_Martin);
        var employee2Ts = timesheetService.report(ctx, employee2Id, TimesheetReportBody.builder()
                .businessAccount(testData.ba_Billing())
                .hoursSpent((short) 8)
                .project(testData.project_M1_ERP_Integration())
                .date(defaultDate)
                .build()).block(MONO_DEFAULT_TIMEOUT);
        Assertions.assertEquals(3, timesheetService.timesheetSummary(ctx, TimesheetAggregatedFilter
                .builder().from(defaultDate).to(defaultDate.plusDays(1)).build()).collectList().block(MONO_DEFAULT_TIMEOUT).size());

        StepVerifier.create(timesheetService.delete(ctx, employeeId, employee2Ts))
                .expectError(BusinessError.class).verify(MONO_DEFAULT_TIMEOUT);
        StepVerifier.create(timesheetService.timesheetSummary(ctx, TimesheetAggregatedFilter
                .builder().from(defaultDate).to(defaultDate.plusDays(1)).build())).expectNextCount(3).verifyComplete();
    }

    /**
     * Do not clean all database, but delete only timesheet tables
     *
     * @return
     */
    private Mono<Void> cleanTimesheetTables() {
        return db.sql("delete from ts.timesheet_record").then();
    }

    private Mono<Map<String, Integer>> generateDefaultReports(AuthContext ctx) {
        var employeeId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var result = new HashMap<String, Integer>();
        return timesheetService.report(ctx, employeeId, TimesheetReportBody.builder()
                        .businessAccount(testData.ba_RND())
                        .hoursSpent((short) 6)
                        .project(testData.project_M1_FMS())
                        .date(defaultDate)
                        .build()).doOnNext(id -> result.put("1:2023-02-02:6", id))
                .flatMap(r -> timesheetService.report(ctx, employeeId, TimesheetReportBody.builder()
                        .businessAccount(testData.ba_RND())
                        .hoursSpent((short) 2)
                        .project(testData.project_M1_FMS())
                        .date(defaultDate.plusDays(1))
                        .build()).doOnNext(id -> result.put("1:2023-02-03:2", id)))
                .map(s -> result);
    }
}
