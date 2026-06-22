package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import ru.abondin.hreasy.platform.api.employee.UpdateCurrentProjectBody;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.repo.employee.projecttransfer.ProjectTransferRequestEntry;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.CurrentProjectTransferApprovalRequestBody;
import ru.abondin.hreasy.platform.service.admin.employee.dto.CurrentProjectTransferDecisionBody;

import java.util.List;

import static ru.abondin.hreasy.platform.TestEmployees.FMS_Empl_Ammara_Knott;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class EmployeeServiceTest extends BaseServiceTest {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private AdminEmployeeService adminEmployeeService;
    @Autowired
    private DatabaseClient db;


    @BeforeEach
    protected void beforeEach() {
        initEmployeesDataAndLogin();
        cleanupProjectTransferTestsData().block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testFindNotFired() {
        /* We have 16 not fired employees in database*/
        StepVerifier
                .create(employeeService.findAll(auth, false))
                .expectNextCount(16)
                .verifyComplete();
    }

    @Test
    public void testFindAllEmployees() {
        /* We have 16 not fired employees +2 employees in database*/
        StepVerifier
                .create(employeeService.findAll(auth, true))
                .expectNextCount(18)
                .verifyComplete();
    }

    @Test
    public void testFindEmployeeReturnsBirthdayWithoutYear() {
        StepVerifier
                .create(employeeService.find(testData.employees.get(TestEmployees.Admin_Shaan_Pitts), auth))
                .expectNextMatches(employee -> "12.06".equals(employee.getBirthday()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Employee can't update own current project")
    public void testUpdateMyProject() {
        StepVerifier
                .create(auth(FMS_Empl_Ammara_Knott)
                        .flatMap(ctx -> adminEmployeeService.updateCurrentProject(
                                testData.employees.get(FMS_Empl_Ammara_Knott)
                                , testData.updateCurrentProjectBody("M1 Billing")
                                , ctx))
                        .doOnError(error -> {
                            log.error("-------- Unexpected error", error);
                        })
                )
                .expectError(AccessDeniedException.class)
                .verify(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    @DisplayName("Test update project for another employee")
    public void testUpdateProjectForAnotherEmployee() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(FMS_Empl_Ammara_Knott).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(adminEmployeeService.updateCurrentProject(
                        jensonId
                        , testData.updateCurrentProjectBody("M1 Policy Manager")
                        , ctx))
                .expectError(AccessDeniedException.class).verify(MONO_DEFAULT_TIMEOUT);
    }

    /**
     * Test goal: verifies the backend contract consumed by frontend when direct project transfer is denied.
     * <p>Precondition: Jawad manages the target FMS project, but does not manage Asiyah's current Billing project.
     * <p>Action: Jawad tries to move Asiyah from Billing to FMS through regular current project update.
     * <p>Verification: service returns BusinessError with transfer approval code and source/target project attributes.
     */
    @Test
    @DisplayName("Target project manager gets approval-required error instead of direct transfer")
    public void updateCurrentProjectReturnsApprovalRequiredForTargetProjectManager() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);

        StepVerifier
                .create(adminEmployeeService.updateCurrentProject(
                        employeeId,
                        testData.updateCurrentProjectBody("M1 FMS"),
                        ctx))
                .expectErrorSatisfies(error -> {
                    var businessError = Assertions.assertInstanceOf(BusinessError.class, error);
                    Assertions.assertEquals(AdminEmployeeService.CURRENT_PROJECT_TRANSFER_APPROVAL_REQUIRED,
                            businessError.getCode());
                    Assertions.assertEquals(Integer.toString(employeeId),
                            businessError.getAttrs().get("employeeId"));
                    Assertions.assertEquals(Integer.toString(testData.project_M1_Billing()),
                            businessError.getAttrs().get("fromProjectId"));
                    Assertions.assertEquals(Integer.toString(testData.project_M1_FMS()),
                            businessError.getAttrs().get("toProjectId"));
                })
                .verify(MONO_DEFAULT_TIMEOUT);
    }

    /**
     * Test goal: verifies that approval-required error is not used for invalid target project ids.
     * <p>Precondition: Jawad does not manage Asiyah's current Billing project and target project id does not exist.
     * <p>Action: Jawad tries to move Asiyah to a missing project through regular current project update.
     * <p>Verification: service keeps regular access denied error.
     */
    @Test
    @DisplayName("Missing target project does not start transfer approval flow")
    public void updateCurrentProjectKeepsAccessDeniedForMissingTargetProject() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);

        StepVerifier
                .create(adminEmployeeService.updateCurrentProject(
                        employeeId,
                        new UpdateCurrentProjectBody(-1, "Tester"),
                        ctx))
                .expectError(AccessDeniedException.class)
                .verify(MONO_DEFAULT_TIMEOUT);
    }

    /**
     * Test goal: verifies transfer approver candidates for the employee current project.
     * <p>Precondition: Jawad manages target FMS project and Asiyah is assigned to Billing.
     * <p>Action: Jawad requests approvers for Asiyah transfer from Billing to FMS.
     * <p>Verification: service returns active Billing project, BA and department managers in priority order.
     */
    @Test
    @DisplayName("Target project manager sees current project transfer approvers")
    public void findCurrentProjectTransferApproversReturnsCurrentProjectManagersInPriorityOrder() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);

        StepVerifier
                .create(adminEmployeeService.findCurrentProjectTransferApprovers(
                        ctx, employeeId, testData.project_M1_FMS()).map(approver -> approver.getDisplayName()).collectList())
                .expectNext(List.of("May Maxwell", "Neville Kyran", "Patterson Husnain", "Gough Percy"))
                .verifyComplete();
    }

    /**
     * Test goal: verifies that target project manager can create a pending transfer request instead of direct update.
     * <p>Precondition: Jawad manages target FMS project, Asiyah is assigned to Billing, and Maxwell is Billing manager.
     * <p>Action: Jawad creates a transfer request from Billing to FMS and then asks for the active request.
     * <p>Verification: service returns created request id and active request details for the same employee/project pair.
     */
    @Test
    @DisplayName("Target project manager creates pending current project transfer request")
    public void requestCurrentProjectTransferApprovalCreatesPendingRequest() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var jawad = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var body = transferRequestBody(
                testData.project_M1_Billing(),
                testData.project_M1_FMS(),
                testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May));

        StepVerifier
                .create(adminEmployeeService.requestCurrentProjectTransferApproval(jawad, employeeId, body)
                        .flatMap(_ -> adminEmployeeService.findActiveCurrentProjectTransferRequest(jawad, employeeId)))
                .assertNext(request -> {
                    Assertions.assertNotNull(request.getId());
                    Assertions.assertEquals(employeeId, request.getEmployeeId());
                    Assertions.assertEquals(testData.project_M1_Billing(), request.getFromProjectId());
                    Assertions.assertEquals("M1 Billing", request.getFromProjectName());
                    Assertions.assertEquals(testData.project_M1_FMS(), request.getToProjectId());
                    Assertions.assertEquals("M1 FMS", request.getToProjectName());
                    Assertions.assertEquals("Tester", request.getRequestedProjectRole());
                    Assertions.assertEquals(testData.employees.get(TestEmployees.FMS_Manager_Jawad_Mcghee),
                            request.getCreatedBy());
                    Assertions.assertEquals("Mcghee Jawad", request.getCreatedByDisplayName());
                    Assertions.assertEquals(testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May),
                            request.getApproverEmployeeId());
                    Assertions.assertEquals("May Maxwell", request.getApproverDisplayName());
                    Assertions.assertNotNull(request.getCreatedAt());
                })
                .verifyComplete();
    }

    /**
     * Test goal: verifies idempotent duplicate handling for pending transfer request creation.
     * <p>Precondition: Jawad already created a pending transfer request for Asiyah.
     * <p>Action: Jawad tries to create the same transfer request again.
     * <p>Verification: service returns the stable already-pending business error code.
     */
    @Test
    @DisplayName("Pending current project transfer request blocks duplicate request")
    public void requestCurrentProjectTransferApprovalRejectsPendingDuplicate() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var jawad = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var body = transferRequestBody(
                testData.project_M1_Billing(),
                testData.project_M1_FMS(),
                testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May));

        StepVerifier
                .create(adminEmployeeService.requestCurrentProjectTransferApproval(jawad, employeeId, body)
                        .then(adminEmployeeService.requestCurrentProjectTransferApproval(jawad, employeeId, body)))
                .expectErrorSatisfies(this::assertAlreadyPendingTransferRequestError)
                .verify(MONO_DEFAULT_TIMEOUT);
    }

    /**
     * Test goal: verifies that active transfer request blocks direct administrative project update.
     * <p>Precondition: Jawad created a pending transfer request for Asiyah and Shaan has global update permission.
     * <p>Action: Shaan tries to update Asiyah current project directly.
     * <p>Verification: service returns already-pending business error instead of applying a hidden override.
     */
    @Test
    @DisplayName("Pending current project transfer request blocks global direct update")
    public void updateCurrentProjectRejectsGlobalUpdateWhenTransferRequestIsPending() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var jawad = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var admin = auth(TestEmployees.Admin_Shaan_Pitts).block(MONO_DEFAULT_TIMEOUT);
        var body = transferRequestBody(
                testData.project_M1_Billing(),
                testData.project_M1_FMS(),
                testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May));

        StepVerifier
                .create(adminEmployeeService.requestCurrentProjectTransferApproval(jawad, employeeId, body)
                        .then(adminEmployeeService.updateCurrentProject(
                                employeeId,
                                testData.updateCurrentProjectBody("M1 FMS"),
                                admin)))
                .expectErrorSatisfies(this::assertAlreadyPendingTransferRequestError)
                .verify(MONO_DEFAULT_TIMEOUT);
    }

    /**
     * Test goal: verifies that inactive transfer requests do not block new approval flow.
     * <p>Precondition: Asiyah has rejected and expired transfer request rows but no pending request.
     * <p>Action: Jawad creates a new transfer request from Billing to FMS.
     * <p>Verification: service creates a new pending request successfully.
     */
    @Test
    @DisplayName("Rejected and expired transfer requests do not block new request")
    public void requestCurrentProjectTransferApprovalIgnoresInactiveRequests() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var jawad = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var approverId = testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May);
        var body = transferRequestBody(testData.project_M1_Billing(), testData.project_M1_FMS(), approverId);

        StepVerifier
                .create(insertTransferRequest(employeeId, ProjectTransferRequestEntry.STATE_REJECTED, approverId)
                        .then(insertTransferRequest(employeeId, ProjectTransferRequestEntry.STATE_EXPIRED, approverId))
                        .then(adminEmployeeService.requestCurrentProjectTransferApproval(jawad, employeeId, body))
                        .flatMap(_ -> adminEmployeeService.findActiveCurrentProjectTransferRequest(jawad, employeeId)))
                .assertNext(request -> {
                    Assertions.assertEquals(employeeId, request.getEmployeeId());
                    Assertions.assertEquals(testData.project_M1_Billing(), request.getFromProjectId());
                    Assertions.assertEquals(testData.project_M1_FMS(), request.getToProjectId());
                })
                .verifyComplete();
    }

    /**
     * Test goal: verifies that assigned approver applies pending current project transfer request.
     * <p>Precondition: Jawad created a pending transfer request for Asiyah and Maxwell is assigned approver.
     * <p>Action: Maxwell approves the request.
     * <p>Verification: request becomes approved and employee current project/role are updated.
     */
    @Test
    @DisplayName("Assigned approver approves current project transfer request")
    public void approveCurrentProjectTransferRequestAppliesProjectChange() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var jawad = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var maxwell = auth(TestEmployees.Billing_Manager_Maxwell_May).block(MONO_DEFAULT_TIMEOUT);
        var body = transferRequestBody(
                testData.project_M1_Billing(),
                testData.project_M1_FMS(),
                testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May));

        StepVerifier
                .create(adminEmployeeService.requestCurrentProjectTransferApproval(jawad, employeeId, body)
                        .flatMap(requestId -> adminEmployeeService.approveCurrentProjectTransferRequest(maxwell, requestId, decisionBody("ok"))
                                .then(Mono.zip(projectTransferRequestState(requestId), employeeCurrentProject(employeeId)))))
                .assertNext(result -> {
                    var requestState = result.getT1();
                    var currentProject = result.getT2();
                    Assertions.assertEquals(ProjectTransferRequestEntry.STATE_APPROVED, requestState.state());
                    Assertions.assertEquals(testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May),
                            requestState.updatedBy());
                    Assertions.assertNotNull(requestState.appliedEmployeeHistoryId());
                    Assertions.assertEquals("ok", requestState.decisionComment());
                    Assertions.assertEquals(testData.project_M1_FMS(), currentProject.projectId());
                    Assertions.assertEquals("Tester", currentProject.role());
                })
                .verifyComplete();
    }

    /**
     * Test goal: verifies that assigned approver can reject pending current project transfer request.
     * <p>Precondition: Jawad created a pending transfer request for Asiyah and Maxwell is assigned approver.
     * <p>Action: Maxwell rejects the request.
     * <p>Verification: request becomes rejected and employee remains on source project.
     */
    @Test
    @DisplayName("Assigned approver rejects current project transfer request")
    public void rejectCurrentProjectTransferRequestDoesNotApplyProjectChange() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var jawad = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var maxwell = auth(TestEmployees.Billing_Manager_Maxwell_May).block(MONO_DEFAULT_TIMEOUT);
        var body = transferRequestBody(
                testData.project_M1_Billing(),
                testData.project_M1_FMS(),
                testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May));

        StepVerifier
                .create(adminEmployeeService.requestCurrentProjectTransferApproval(jawad, employeeId, body)
                        .flatMap(requestId -> adminEmployeeService.rejectCurrentProjectTransferRequest(maxwell, requestId, decisionBody("no"))
                                .then(Mono.zip(projectTransferRequestState(requestId), employeeCurrentProject(employeeId)))))
                .assertNext(result -> {
                    var requestState = result.getT1();
                    var currentProject = result.getT2();
                    Assertions.assertEquals(ProjectTransferRequestEntry.STATE_REJECTED, requestState.state());
                    Assertions.assertEquals("no", requestState.decisionComment());
                    Assertions.assertNull(requestState.appliedEmployeeHistoryId());
                    Assertions.assertEquals(testData.project_M1_Billing(), currentProject.projectId());
                })
                .verifyComplete();
    }

    /**
     * Test goal: verifies that request creator can cancel pending current project transfer request.
     * <p>Precondition: Jawad created a pending transfer request for Asiyah.
     * <p>Action: Jawad cancels the request.
     * <p>Verification: request becomes canceled and employee remains on source project.
     */
    @Test
    @DisplayName("Request creator cancels current project transfer request")
    public void cancelCurrentProjectTransferRequestByCreatorClosesRequest() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var jawad = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var body = transferRequestBody(
                testData.project_M1_Billing(),
                testData.project_M1_FMS(),
                testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May));

        StepVerifier
                .create(adminEmployeeService.requestCurrentProjectTransferApproval(jawad, employeeId, body)
                        .flatMap(requestId -> adminEmployeeService.cancelCurrentProjectTransferRequest(jawad, requestId, decisionBody("cancel"))
                                .then(Mono.zip(projectTransferRequestState(requestId), employeeCurrentProject(employeeId)))))
                .assertNext(result -> {
                    var requestState = result.getT1();
                    var currentProject = result.getT2();
                    Assertions.assertEquals(ProjectTransferRequestEntry.STATE_CANCELED, requestState.state());
                    Assertions.assertEquals("cancel", requestState.decisionComment());
                    Assertions.assertNull(requestState.appliedEmployeeHistoryId());
                    Assertions.assertEquals(testData.project_M1_Billing(), currentProject.projectId());
                })
                .verifyComplete();
    }

    /**
     * Test goal: verifies that non-assigned employee cannot approve pending current project transfer request.
     * <p>Precondition: Jawad created a pending transfer request assigned to Maxwell.
     * <p>Action: Shaan tries to approve the request.
     * <p>Verification: service rejects the action with access denied.
     */
    @Test
    @DisplayName("Non-assigned employee cannot approve current project transfer request")
    public void approveCurrentProjectTransferRequestRejectsNonApprover() {
        var employeeId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var jawad = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var admin = auth(TestEmployees.Admin_Shaan_Pitts).block(MONO_DEFAULT_TIMEOUT);
        var body = transferRequestBody(
                testData.project_M1_Billing(),
                testData.project_M1_FMS(),
                testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May));

        StepVerifier
                .create(adminEmployeeService.requestCurrentProjectTransferApproval(jawad, employeeId, body)
                        .flatMap(requestId -> adminEmployeeService.approveCurrentProjectTransferRequest(admin, requestId, null)))
                .expectError(AccessDeniedException.class)
                .verify(MONO_DEFAULT_TIMEOUT);
    }

    private CurrentProjectTransferApprovalRequestBody transferRequestBody(int fromProjectId,
                                                                          int toProjectId,
                                                                          int approverEmployeeId) {
        var body = new CurrentProjectTransferApprovalRequestBody();
        body.setFromProjectId(fromProjectId);
        body.setToProjectId(toProjectId);
        body.setRole("Tester");
        body.setApproverEmployeeId(approverEmployeeId);
        return body;
    }

    private CurrentProjectTransferDecisionBody decisionBody(String comment) {
        var body = new CurrentProjectTransferDecisionBody();
        body.setComment(comment);
        return body;
    }

    private void assertAlreadyPendingTransferRequestError(Throwable error) {
        var businessError = Assertions.assertInstanceOf(BusinessError.class, error);
        Assertions.assertEquals(AdminEmployeeService.CURRENT_PROJECT_TRANSFER_REQUEST_ALREADY_PENDING,
                businessError.getCode());
    }

    private Mono<Void> insertTransferRequest(int employeeId, short state, int approverEmployeeId) {
        return db.sql("""
                        insert into empl.project_transfer_request (
                            employee_id,
                            from_project_id,
                            to_project_id,
                            requested_project_role,
                            approver_employee_id,
                            state,
                            created_at,
                            created_by
                        ) values (
                            :employeeId,
                            :fromProjectId,
                            :toProjectId,
                            'Tester',
                            :approverEmployeeId,
                            :state,
                            now(),
                            :createdBy
                        )
                        """)
                .bind("employeeId", employeeId)
                .bind("fromProjectId", testData.project_M1_Billing())
                .bind("toProjectId", testData.project_M1_FMS())
                .bind("approverEmployeeId", approverEmployeeId)
                .bind("state", state)
                .bind("createdBy", testData.employees.get(TestEmployees.FMS_Manager_Jawad_Mcghee))
                .then();
    }

    private Mono<ProjectTransferRequestState> projectTransferRequestState(int requestId) {
        return db.sql("""
                        select state, updated_by, decision_comment, applied_employee_history_id
                        from empl.project_transfer_request
                        where id = :requestId
                        """)
                .bind("requestId", requestId)
                .map(row -> new ProjectTransferRequestState(
                        row.get("state", Number.class).shortValue(),
                        row.get("updated_by", Integer.class),
                        row.get("decision_comment", String.class),
                        row.get("applied_employee_history_id", Integer.class)))
                .one();
    }

    private Mono<EmployeeCurrentProject> employeeCurrentProject(int employeeId) {
        return db.sql("""
                        select current_project, current_project_role
                        from empl.employee
                        where id = :employeeId
                        """)
                .bind("employeeId", employeeId)
                .map(row -> new EmployeeCurrentProject(
                        row.get("current_project", Integer.class),
                        row.get("current_project_role", String.class)))
                .one();
    }

    private record ProjectTransferRequestState(Short state,
                                               Integer updatedBy,
                                               String decisionComment,
                                               Integer appliedEmployeeHistoryId) {
    }

    private record EmployeeCurrentProject(Integer projectId, String role) {
    }

    private Mono<Void> cleanupProjectTransferTestsData() {
        return db.sql("delete from empl.project_transfer_request").then()
                .then(db.sql("delete from history.history where entity_type = 'project_transfer_request'").then())
                .then(db.sql("""
                                update empl.employee
                                set current_project = :projectId, current_project_role = null
                                where id = :employeeId
                                """)
                        .bind("projectId", testData.project_M1_Billing())
                        .bind("employeeId", testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob))
                        .then());
    }

}
