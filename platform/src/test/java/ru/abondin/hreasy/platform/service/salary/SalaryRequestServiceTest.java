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
import ru.abondin.hreasy.platform.service.salary.dto.*;

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
    private SalaryRequestAdminService salaryAdminRequestService;

    @Autowired
    private SalaryRequestRepo repo;


    @Autowired
    private DatabaseClient db;

    @BeforeEach
    protected void beforeEach() {
        initEmployeesDataAndLogin();
        cleanSalaryAndClosedPeriodAndNotificationsTables().block(MONO_DEFAULT_TIMEOUT);
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
                                && dto.getReq().getComment().equals(report.getComment())
                                && dto.getReq().getIncreaseStartPeriod().equals(report.getIncreaseStartPeriod())
                                && dto.getType().equals(report.getType())
                                && dto.getCreatedBy().getId() == ctx.getEmployeeInfo().getEmployeeId()
                                && dto.getEmployee().getId() == jensonId
                                && dto.getImpl() == null
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
    public void testReportClosedPeriod() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.Salary_Manager_Salary_Gold).block(MONO_DEFAULT_TIMEOUT);

        salaryAdminRequestService.closeSalaryRequestPeriod(ctx, 202305, "testReportClosedPeriod").block(MONO_DEFAULT_TIMEOUT);

        var ba = testData.ba_RND();

        ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var report = defaultRequest(ctx, jensonId, ba);
        report.setIncreaseStartPeriod(202305);
        StepVerifier
                .create(salaryRequestService.report(ctx, report)
                        .flatMap(r -> repo.findById(r))
                )
                .expectErrorMatches(e -> e instanceof BusinessError && ((BusinessError) e).getCode().equals("errors.salary_request.period_closed"))
                .verify();
    }

    @Test
    public void testReject() {
        var ctx = auth(TestEmployees.Salary_Manager_Salary_Gold).block(MONO_DEFAULT_TIMEOUT);
        var requestId = reportDefaultRequest();
        var rejectBody = SalaryRequestRejectBody.builder()
                .reason("Not planned increase")
                .comment("Very bad employee")
                .build();
        StepVerifier
                .create(salaryAdminRequestService.reject(ctx, requestId, rejectBody)
                        .flatMap(updatedId -> {
                            Assertions.assertEquals(requestId, updatedId, "Invalid ID after update");
                            return salaryRequestService.get(ctx, updatedId);
                        })
                ).expectNextMatches(dto -> dto.getImpl()!=null
                        && dto.getImpl().getImplementedAt() != null
                        && dto.getImpl().getImplementedBy().getId() == ctx.getEmployeeInfo().getEmployeeId()
                        && dto.getImpl().getComment().equals("Very bad employee")
                        && dto.getImpl().getReason().equals("Not planned increase")
                        && dto.getImpl().getState() == (short)-1
                ).verifyComplete();
    }


    @Test
    public void testMarkAsImplemented() {
        var ctx = auth(TestEmployees.Salary_Manager_Salary_Gold).block(MONO_DEFAULT_TIMEOUT);
        var requestId = reportDefaultRequest();
        var implBody = SalaryRequestImplementBody.builder()
                .salaryIncrease(BigDecimal.valueOf(1100))
                .increaseStartPeriod(202308)
                .newPosition(testData.position_JavaDeveloper())
                .reason("Planned increase")
                .comment("Very good employee")
                .build();
        StepVerifier
                .create(salaryAdminRequestService.markAsImplemented(ctx, requestId, implBody)
                        .flatMap(updatedId -> {
                            Assertions.assertEquals(requestId, updatedId, "Invalid ID after update");
                            return salaryRequestService.get(ctx, updatedId);
                        })
                ).expectNextMatches(dto ->
                        dto.getImpl() != null
                                && dto.getImpl().getImplementedAt() != null
                                && dto.getImpl().getImplementedBy().getId() == ctx.getEmployeeInfo().getEmployeeId()
                                && dto.getImpl().getComment().equals("Very good employee")
                                && dto.getImpl().getReason().equals("Planned increase")
                                && dto.getImpl().getState() == (short)1
                                && dto.getImpl().getNewPosition().getId() == testData.position_JavaDeveloper()
                                && dto.getImpl().getSalaryIncrease().compareTo(BigDecimal.valueOf(1100)) == 0
                                && dto.getImpl().getIncreaseStartPeriod().equals(202308)

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
    private Mono<Void> cleanSalaryAndClosedPeriodAndNotificationsTables() {
        return db.sql("delete from sal.salary_request").then()
                .then(db.sql("delete from sal.salary_request_approval").then())
                .then(db.sql("delete from history.history where " +
                        "entity_type='" + SALARY_REQUEST.getType() + "'" +
                        " or entity_type='" + SALARY_REQUEST_APPROVAL.getType() + "'").then())
                .then(db.sql("delete from notify.notification where " +
                        "category='" + NotificationPersistService.NotificationCategory.SALARY_REQUEST.getCategory() + "'").then())
                .then(db.sql("delete from sal.salary_request_closed_period").then());
    }

    private SalaryRequestReportBody defaultRequest(AuthContext ctx, int employeeId, int ba) {
        return SalaryRequestReportBody.builder()
                .employeeId(employeeId)
                .type(SalaryRequestType.SALARY_INCREASE.getValue())
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
