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
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestRepo;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.notification.NotificationPersistService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestImplementBody;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestRejectBody;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportBody;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestType;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestApprovalDto;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestApproveBody;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestDeclineBody;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Locale;

import static ru.abondin.hreasy.platform.TestEmployees.*;
import static ru.abondin.hreasy.platform.service.HistoryDomainService.HistoryEntityType.SALARY_REQUEST;
import static ru.abondin.hreasy.platform.service.HistoryDomainService.HistoryEntityType.SALARY_REQUEST_APPROVAL;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
class SalaryRequestServiceTest extends BaseServiceTest {

    private final static Duration MONO_DEFAULT_TIMEOUT = Duration.ofSeconds(3);

    @Autowired
    private SalaryRequestService salaryRequestService;


    @Autowired
    private AdminSalaryRequestService salaryAdminRequestService;

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
    void testReportRequest() {
        var jensonId = testData.employees.get(FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var ba = testData.ba_RND();
        var report = defaultRequest(jensonId, ba);
        StepVerifier
                .create(salaryRequestService.report(ctx, report)
                        .flatMap(r -> salaryRequestService.get(auth, r))
                )
                .expectNextMatches(dto -> {
                            Assertions.assertEquals(ba, dto.getBudgetBusinessAccount().getId(), "Budgeting ba");
                            Assertions.assertEquals(report.getComment(), dto.getReq().getComment(), "Request comment");
                            Assertions.assertEquals(report.getIncreaseStartPeriod(), dto.getReq().getIncreaseStartPeriod(), "Request start period");
                            Assertions.assertEquals(report.getType(), dto.getType(), "Type");
                            Assertions.assertEquals(ctx.getEmployeeInfo().getEmployeeId(), dto.getCreatedBy().getId(), "Created by");
                            Assertions.assertNull(dto.getImpl(), "Impl must be null");
                            Assertions.assertEquals(jensonId, dto.getEmployee().getId(), "Employee");
                            Assertions.assertEquals(testData.project_M1_FMS(), dto.getEmployeeInfo().getCurrentProject().getId(), "Current Project");
                            Assertions.assertEquals(testData.ba_RND(), dto.getEmployeeInfo().getBa().getId(), "Employee BA");
                            Assertions.assertEquals(testData.position_AutomationQA(), dto.getEmployeeInfo().getPosition().getId(), "Employee Position");
                            return true;
                        }
                ).verifyComplete();
    }

    @Test
    void testReportNoPermissionRequest() {
        var jensonId = testData.employees.get(FMS_Empl_Jenson_Curtis);
        var ctx = auth(FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        var ba = testData.ba_RND();
        var report = defaultRequest(jensonId, ba);
        StepVerifier
                .create(salaryRequestService.report(ctx, report)
                        .flatMap(r -> repo.findById(r))
                )
                .expectError(AccessDeniedException.class).verify();
    }

    @Test
    void testReportClosedPeriod() {
        var jensonId = testData.employees.get(FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.Salary_Manager_Salary_Gold).block(MONO_DEFAULT_TIMEOUT);

        salaryAdminRequestService.closeSalaryRequestPeriod(ctx, 202305, "testReportClosedPeriod").block(MONO_DEFAULT_TIMEOUT);

        var ba = testData.ba_RND();

        ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var report = defaultRequest(jensonId, ba);
        report.setIncreaseStartPeriod(202305);
        StepVerifier
                .create(salaryRequestService.report(ctx, report)
                        .flatMap(r -> repo.findById(r))
                )
                .expectErrorMatches(e -> e instanceof BusinessError && ((BusinessError) e).getCode().equals("errors.salary_request.period_closed"))
                .verify();
    }

    @Test
    void testReject() {
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
                ).expectNextMatches(dto -> dto.getImpl() != null
                        && dto.getImpl().getImplementedAt() != null
                        && dto.getImpl().getImplementedBy().getId() == ctx.getEmployeeInfo().getEmployeeId()
                        && dto.getImpl().getComment().equals("Very bad employee")
                        && dto.getImpl().getRejectReason().equals("Not planned increase")
                        && dto.getImpl().getState() == (short) 2
                ).verifyComplete();
    }


    @Test
    void testMarkAsImplemented() {
        var ctx = auth(TestEmployees.Salary_Manager_Salary_Gold).block(MONO_DEFAULT_TIMEOUT);
        var requestId = reportDefaultRequest();
        var implBody = SalaryRequestImplementBody.builder()
                .increaseAmount(BigDecimal.valueOf(1100))
                .increaseStartPeriod(202308)
                .newPosition(testData.position_JavaDeveloper())
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
                                && dto.getImpl().getState() == (short) 1
                                && dto.getImpl().getNewPosition().getId() == testData.position_JavaDeveloper()
                                && dto.getImpl().getIncreaseAmount().compareTo(BigDecimal.valueOf(1100)) == 0
                                && dto.getImpl().getIncreaseStartPeriod().equals(202308)

                ).verifyComplete();

    }


    @Test
    void testGetNotMyRequest() {
        var jensonId = testData.employees.get(FMS_Empl_Jenson_Curtis);
        var ctxJawad = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var ba = testData.ba_RND();
        var requestReportBody = defaultRequest(jensonId, ba);
        var requestId = salaryRequestService.report(ctxJawad, requestReportBody).block(MONO_DEFAULT_TIMEOUT);
        salaryRequestService.get(ctxJawad, requestId).block(MONO_DEFAULT_TIMEOUT);

        var ctxKyran = auth(Multiprojet_Manager_Kyran_Neville).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier.create(salaryRequestService.get(ctxKyran, requestId))
                .expectError(AccessDeniedException.class)
                .verify();
    }


    @Test
    void testGetFromOtherBas() {
        var jensonId = testData.employees.get(FMS_Empl_Jenson_Curtis);
        var ctxMaxwell = auth(TestEmployees.Billing_Manager_Maxwell_May).block(MONO_DEFAULT_TIMEOUT);
        var billingBa = testData.ba_Billing();
        var requestReportBody = defaultRequest(jensonId, billingBa);
        var requestId = salaryRequestService.report(ctxMaxwell, requestReportBody).block(MONO_DEFAULT_TIMEOUT);

        var ctxKyran = auth(Multiprojet_Manager_Kyran_Neville).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier.create(salaryRequestService.get(ctxKyran, requestId))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void testFindInBa() {
        // report in RND
        var jensonId = testData.employees.get(FMS_Empl_Jenson_Curtis);
        var auth = auth(Multiprojet_Manager_Kyran_Neville).block(MONO_DEFAULT_TIMEOUT);
        salaryRequestService.report(auth, defaultRequest(jensonId, testData.ba_RND())).block(MONO_DEFAULT_TIMEOUT);


        // report in billing account
        salaryRequestService.report(auth,
                defaultRequest(testData.employees.get(Billing_Empl_Asiyah_Bob), testData.ba_Billing())
        ).block(MONO_DEFAULT_TIMEOUT);

        // Husnain has finance role and has access to billing business account
        auth = auth(Billing_BA_Head_Husnain_Patterson).block(MONO_DEFAULT_TIMEOUT);
        /**
         * Check that only request from RND account retrieved
         */
        StepVerifier.create(salaryRequestService.find(auth, 202308))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testDecline() {
        var ctx = auth(Billing_BA_Head_Husnain_Patterson).block(MONO_DEFAULT_TIMEOUT);
        var report = salaryRequestService.report(auth,
                defaultRequest(testData.employees.get(Billing_Empl_Asiyah_Bob), testData.ba_Billing())
        ).block(MONO_DEFAULT_TIMEOUT);

        var declineBody = new SalaryRequestDeclineBody();
        declineBody.setComment("Very bad employee");

        StepVerifier.create(salaryRequestService.decline(ctx, report, declineBody)
                .flatMapMany(updatedId -> salaryRequestService.findApprovals(ctx, report))
        ).expectNextMatches(dto ->
                dto.getCreatedBy().getId() == ctx.getEmployeeInfo().getEmployeeId()
                        && dto.getComment().equals("Very bad employee")
                        && dto.getState() == SalaryRequestApprovalDto.ApprovalActionTypes.DECLINE.getValue()
                        && dto.getCreatedBy().getName().toLowerCase(Locale.ROOT).contains("husnain")
        ).verifyComplete();
    }

    @Test
    void testApprove() {
        var ctx = auth(Billing_BA_Head_Husnain_Patterson).block(MONO_DEFAULT_TIMEOUT);
        var report = salaryRequestService.report(auth,
                defaultRequest(testData.employees.get(Billing_Empl_Asiyah_Bob), testData.ba_Billing())
        ).block(MONO_DEFAULT_TIMEOUT);
        var body = new SalaryRequestApproveBody();
        body.setComment("Very good employee");
        StepVerifier
                .create(salaryRequestService.approve(ctx, report, body))
                .expectNextCount(1)
                .verifyComplete();
        var approvals = salaryRequestService.get(auth, report).block(MONO_DEFAULT_TIMEOUT).getApprovals();
        Assertions.assertEquals(1, approvals.size());
        Assertions.assertEquals(ctx.getEmployeeInfo().getEmployeeId(), approvals.get(0).getCreatedBy().getId());
        Assertions.assertEquals("Very good employee", approvals.get(0).getComment());
        Assertions.assertEquals(SalaryRequestApprovalDto.ApprovalActionTypes.APPROVE.getValue(), approvals.get(0).getState());
    }




    /**
     * Do not clean all database, but delete only
     * <ul>
     *     <li>salary request approval</li>
     *     <li>salary request</li>
     *     <li>history messages ('salary_request', 'salary_request_approval')</li>
     *     <li>notifications messages ('salary_request')</li>
     * </ul>
     *
     * @return
     */
    private Mono<Void> cleanSalaryAndClosedPeriodAndNotificationsTables() {
        return db.sql("delete from sal.salary_request_approval").then()
                .then(db.sql("delete from sal.salary_request").then())
                .then(db.sql("delete from history.history where " +
                        "entity_type='" + SALARY_REQUEST.getType() + "'" +
                        " or entity_type='" + SALARY_REQUEST_APPROVAL.getType() + "'").then())
                .then(db.sql("delete from notify.notification where " +
                        "category='" + NotificationPersistService.NotificationCategory.SALARY_REQUEST.getCategory() + "'").then())
                .then(db.sql("delete from sal.salary_request_closed_period").then());
    }

    private SalaryRequestReportBody defaultRequest(int employeeId, int ba) {
        return SalaryRequestReportBody.builder()
                .employeeId(employeeId)
                .type(SalaryRequestType.SALARY_INCREASE.getValue())
                .assessmentId(null)
                .comment("My First Report")
                .budgetExpectedFundingUntil(null)
                .budgetBusinessAccount(ba)
                .reason("Just increase salary")
                .increaseAmount(BigDecimal.valueOf(1000))
                .increaseStartPeriod(202308)
                .build();
    }

    private int reportDefaultRequest() {
        var jensonId = testData.employees.get(FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var ba = testData.ba_RND();
        var report = defaultRequest(jensonId, ba);
        return salaryRequestService.report(ctx, report).block(MONO_DEFAULT_TIMEOUT);
    }


}
