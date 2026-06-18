package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.api.employee.UpdateCurrentProjectBody;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;

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


    @BeforeEach
    protected void beforeEach() {
        initEmployeesDataAndLogin();
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

}
