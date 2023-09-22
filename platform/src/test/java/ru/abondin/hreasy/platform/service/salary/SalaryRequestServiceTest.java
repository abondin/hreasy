package ru.abondin.hreasy.platform.service.salary;

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
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestRepo;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.notification.NotificationPersistService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportBody;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportStat;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportType;

import java.math.BigDecimal;
import java.time.Duration;

import static ru.abondin.hreasy.platform.service.HistoryDomainService.HistoryEntityType.SALARY_REQUEST;
import static ru.abondin.hreasy.platform.service.HistoryDomainService.HistoryEntityType.SALARY_REQUEST_APPROVAL;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class SalaryRequestServiceTest extends BaseServiceTest {

    private final static Duration MONO_DEFAULT_TIMEOUT = Duration.ofSeconds(3);

    @Autowired
    private SalaryRequestService salaryRequestService;

    @Autowired
    private SalaryRequestRepo repo;


    @Autowired
    private DatabaseClient db;

    @BeforeEach
    protected void beforeEach() {
        initEmployeesDataAndLogin();
        cleanSalaryAndNotificationsTables().block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testReportRequest() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var ba = testData.ba_RND();
        var report = defaultRequest(ctx, jensonId, ba);
        StepVerifier
                .create(salaryRequestService.report(ctx, report)
                        .flatMap(r -> salaryRequestService.get(auth, r))
                )
                .expectNextMatches((dto) ->
                        dto.getBudgetBusinessAccount().getId() == ba
                                && dto.getComment().equals(report.getComment())
                                && dto.getType().equals(report.getType())
                                && dto.getStat().equals(SalaryRequestReportStat.CREATED.getValue())
                                && dto.getCreatedBy().getId() == ctx.getEmployeeInfo().getEmployeeId()
                                && dto.getEmployee().getId() == jensonId
                                && dto.getIncreaseStartPeriod().equals(report.getIncreaseStartPeriod())
                ).verifyComplete();
    }

    @Test
    public void testReportNoPermissionRequest() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        var ba = testData.ba_RND();
        var report = defaultRequest(ctx, jensonId, ba);
        StepVerifier
                .create(salaryRequestService.report(ctx, report)
                        .flatMap(r -> repo.findById(r))
                )
                .expectError(AccessDeniedException.class).verify();
    }

    @Test
    public void testMoveInProgress() {
        var ctx = auth(TestEmployees.Salary_Manager_Salary_Gold).block(MONO_DEFAULT_TIMEOUT);
        var requestId = reportDefaultRequest();
        StepVerifier
                .create(salaryRequestService.moveToInProgress(ctx, requestId)
                        .flatMap(updatedId -> {
                            Assertions.assertEquals(requestId, updatedId, "Invalid ID after update");
                            return repo.findById(updatedId);
                        })
                ).expectNextMatches(entry ->
                        entry.getInprogressAt() != null && entry.getInprogressBy().equals(ctx.getEmployeeInfo().getEmployeeId())
                ).verifyComplete();
    }

    @Test
    public void testMarkAsImplementedNotInProgress() {
        var ctx = auth(TestEmployees.Salary_Manager_Salary_Gold).block(MONO_DEFAULT_TIMEOUT);
        var requestId = reportDefaultRequest();
        StepVerifier
                .create(salaryRequestService.markAsImplemented(ctx, requestId))
                .expectErrorMatches(e -> e instanceof BusinessError && ((BusinessError) e).getCode().equals("errors.salary_request.not_in_inprogress"))
                .verify();

    }

    @Test
    public void testMarkAsImplemented() {
        var ctx = auth(TestEmployees.Salary_Manager_Salary_Gold).block(MONO_DEFAULT_TIMEOUT);
        var requestId = reportDefaultRequest();
        salaryRequestService.moveToInProgress(ctx, requestId).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(salaryRequestService.markAsImplemented(ctx, requestId)
                        .flatMap(updatedId -> {
                            Assertions.assertEquals(requestId, updatedId, "Invalid ID after update");
                            return repo.findById(updatedId);
                        })
                ).expectNextMatches(entry ->
                        entry.getImplementedAt() != null && entry.getImplementedBy().equals(ctx.getEmployeeInfo().getEmployeeId())
                ).verifyComplete();

    }

    @Test
    public void testGetNotMyRequest() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctxJawad = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var ba = testData.ba_RND();
        var requestReportBody = defaultRequest(ctxJawad, jensonId, ba);
        var requestId = salaryRequestService.report(ctxJawad, requestReportBody).block(MONO_DEFAULT_TIMEOUT);
        salaryRequestService.get(ctxJawad, requestId).block(MONO_DEFAULT_TIMEOUT);

        var ctxKyran = auth(TestEmployees.Multiprojet_Manager_Kyran_Neville).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier.create(salaryRequestService.get(ctxKyran, requestId))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    public void testFindInBa() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctxMaxwell = auth(TestEmployees.Billing_Manager_Maxwell_May).block(MONO_DEFAULT_TIMEOUT);
        var billingBa = testData.ba_Billing();
        var requestReportBody = defaultRequest(ctxMaxwell, jensonId, billingBa);
        var requestId = salaryRequestService.report(ctxMaxwell, requestReportBody).block(MONO_DEFAULT_TIMEOUT);

        var ctxKyran = auth(TestEmployees.Multiprojet_Manager_Kyran_Neville).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier.create(salaryRequestService.get(ctxKyran, requestId))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    /**
     * Do not clean all database, but delete only
     * <ul>
     *     <li>salary request</li>
     *     <li>salary request approval</li>
     *     <li>history messages ('salary_request', 'salary_request_approval')</li>
     *     <li>notifications messages ('salary_request')</li>
     * </ul>
     * ,
     *
     * @return
     */
    private Mono<Void> cleanSalaryAndNotificationsTables() {
        return db.sql("delete from sal.salary_request").then()
                .then(db.sql("delete from sal.salary_request_approval").then())
                .then(db.sql("delete from history.history where " +
                        "entity_type='" + SALARY_REQUEST.getType() + "'" +
                        " or entity_type='" + SALARY_REQUEST_APPROVAL.getType() + "'").then())
                .then(db.sql("delete from notify.notification where " +
                        "category='" + NotificationPersistService.NotificationCategory.SALARY_REQUEST.getCategory() + "'").then());
    }

    private SalaryRequestReportBody defaultRequest(AuthContext ctx, int employeeId, int ba) {
        return SalaryRequestReportBody.builder()
                .employeeId(employeeId)
                .type(SalaryRequestReportType.SALARY_INCREASE.getValue())
                .assessmentId(null)
                .comment("My First Report")
                .budgetExpectedFundingUntil(null)
                .budgetBusinessAccount(ba)
                .reason("Just increase salary")
                .salaryIncrease(BigDecimal.valueOf(1000))
                .increaseStartPeriod(202308)
                .build();
    }

    private int reportDefaultRequest() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var ba = testData.ba_RND();
        var report = defaultRequest(ctx, jensonId, ba);
        return salaryRequestService.report(ctx, report).block(MONO_DEFAULT_TIMEOUT);
    }
}
