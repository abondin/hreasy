package ru.abondin.hreasy.platform.service.notification;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.notification.dto.NotificationDto;
import ru.abondin.hreasy.platform.service.overtime.OvertimeService;
import ru.abondin.hreasy.platform.service.overtime.dto.NewOvertimeItemDto;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeApprovalDecisionDto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.abondin.hreasy.platform.TestEmployees.FMS_Empl_Jenson_Curtis;
import static ru.abondin.hreasy.platform.TestEmployees.FMS_Manager_Jawad_Mcghee;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
public class OvertimeNotificationIntegrationTest extends BaseServiceTest {
    private static final String CATEGORY = "overtime";
    private static final String ITEM_CREATED_EVENT = "overtime.item_created";
    private static final String ITEM_DELETED_EVENT = "overtime.item_deleted";
    private static final String APPROVED_EVENT = "overtime.approved";
    private static final String DECLINED_EVENT = "overtime.declined";

    @Autowired
    private OvertimeService overtimeService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DatabaseClient db;

    @BeforeEach
    protected void beforeEach() {
        initEmployeesDataAndLogin();
        cleanOvertimeData().block(MONO_DEFAULT_TIMEOUT);
    }

    /**
     * Test goal: verifies that creating an overtime item creates a manager inbox notification.
     * <p>Precondition: an active FMS employee and FMS manager with overtime view permission exist in test data.
     * <p>Action: the employee adds one overtime item.
     * <p>Verification: the FMS manager inbox contains an overtime item-created notification with the report and item context.
     */
    @Test
    @DisplayName("Overtime item creation notifies project manager")
    public void overtimeItemCreationNotifiesProjectManager() {
        var employeeId = testData.employees.get(FMS_Empl_Jenson_Curtis);
        var employeeAuth = auth(FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        var managerAuth = auth(FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var period = 202601;
        var itemDate = LocalDate.of(2026, 1, 10);

        var report = overtimeService.addItem(employeeId, period,
                new NewOvertimeItemDto(itemDate, testData.project_M1_FMS(), 2, "notify manager"), employeeAuth)
                .block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(report);
        var itemId = report.getItems().getFirst().getId();

        StepVerifier.create(notificationService.myNotifications(managerAuth)
                        .filter(n -> CATEGORY.equals(n.getCategory()))
                        .filter(n -> ITEM_CREATED_EVENT.equals(context(n).optString("eventType")))
                        .filter(n -> itemId == context(n).optInt("overtimeItemId")))
                .assertNext(n -> {
                    var context = context(n);
                    assertTrue(n.getId() > 0);
                    assertEquals(employeeId, context.optInt("employeeId"));
                    assertEquals(report.getId(), context.optInt("overtimeReportId"));
                    assertEquals(period, context.optInt("period"));
                    assertEquals(itemDate.toString(), context.optString("itemDate"));
                    assertEquals(2, context.optDouble("hours"));
                    assertFalse(n.getMarkdownText().contains(Integer.toString(period)));
                    assertFalse(n.getMarkdownText().contains("202 601"));
                })
                .verifyComplete();
    }

    /**
     * Test goal: verifies that deleting an overtime item creates a manager inbox notification.
     * <p>Precondition: an active FMS employee has an overtime item visible to an FMS manager.
     * <p>Action: the employee deletes the overtime item.
     * <p>Verification: the FMS manager inbox contains an overtime item-deleted notification with the deleted item context.
     */
    @Test
    @DisplayName("Overtime item deletion notifies project manager")
    public void overtimeItemDeletionNotifiesProjectManager() {
        var employeeId = testData.employees.get(FMS_Empl_Jenson_Curtis);
        var employeeAuth = auth(FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        var managerAuth = auth(FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var period = 202604;
        var itemDate = LocalDate.of(2026, 4, 10);

        var report = overtimeService.addItem(employeeId, period,
                        new NewOvertimeItemDto(itemDate, testData.project_M1_FMS(), 5, "delete notify"),
                        employeeAuth)
                .block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(report);
        var itemId = report.getItems().getFirst().getId();

        overtimeService.deleteItem(employeeId, period, itemId, employeeAuth)
                .block(MONO_DEFAULT_TIMEOUT);

        StepVerifier.create(notificationService.myNotifications(managerAuth)
                        .filter(n -> CATEGORY.equals(n.getCategory()))
                        .filter(n -> ITEM_DELETED_EVENT.equals(context(n).optString("eventType")))
                        .filter(n -> itemId == context(n).optInt("overtimeItemId")))
                .assertNext(n -> {
                    var context = context(n);
                    assertTrue(n.getId() > 0);
                    assertEquals(employeeId, context.optInt("employeeId"));
                    assertEquals(report.getId(), context.optInt("overtimeReportId"));
                    assertEquals(period, context.optInt("period"));
                    assertEquals(itemDate.toString(), context.optString("itemDate"));
                    assertEquals(5, context.optDouble("hours"));
                    assertFalse(n.getMarkdownText().contains(Integer.toString(period)));
                    assertFalse(n.getMarkdownText().contains("202 604"));
                })
                .verifyComplete();
    }

    /**
     * Test goal: verifies that approving an overtime report creates an employee inbox notification.
     * <p>Precondition: an active FMS employee has an overtime report with one item.
     * <p>Action: the FMS manager approves the overtime report.
     * <p>Verification: the employee inbox contains an overtime-approved notification with the decision context.
     */
    @Test
    @DisplayName("Overtime approval notifies employee")
    public void overtimeApprovalNotifiesEmployee() {
        var employeeId = testData.employees.get(FMS_Empl_Jenson_Curtis);
        var employeeAuth = auth(FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        var managerAuth = auth(FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var period = 202602;

        var report = overtimeService.addItem(employeeId, period,
                        new NewOvertimeItemDto(LocalDate.of(2026, 2, 10), testData.project_M1_FMS(), 3, "approve notify"),
                        employeeAuth)
                .block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(report);

        overtimeService.approveReport(employeeId, period, OvertimeApprovalDecisionDto.ApprovalDecision.APPROVED,
                        null, null, managerAuth)
                .block(MONO_DEFAULT_TIMEOUT);

        StepVerifier.create(notificationService.myNotifications(employeeAuth)
                        .filter(n -> CATEGORY.equals(n.getCategory()))
                        .filter(n -> APPROVED_EVENT.equals(context(n).optString("eventType")))
                        .filter(n -> period == context(n).optInt("period")))
                .assertNext(n -> {
                    var context = context(n);
                    assertTrue(n.getId() > 0);
                    assertEquals(employeeId, context.optInt("employeeId"));
                    assertEquals(report.getId(), context.optInt("overtimeReportId"));
                    assertEquals(OvertimeApprovalDecisionDto.ApprovalDecision.APPROVED.name(), context.optString("decision"));
                    assertEquals(managerAuth.getEmployeeInfo().getEmployeeId(), context.optInt("approverEmployeeId"));
                    assertTrue(context.optInt("decisionId") > 0);
                    assertFalse(n.getMarkdownText().contains(Integer.toString(period)));
                    assertFalse(n.getMarkdownText().contains("202 602"));
                })
                .verifyComplete();
    }

    /**
     * Test goal: verifies that declining an overtime report creates an employee inbox notification.
     * <p>Precondition: an active FMS employee has an overtime report with one item.
     * <p>Action: the FMS manager declines the overtime report with a comment.
     * <p>Verification: the employee inbox contains an overtime-declined notification with the decision context.
     */
    @Test
    @DisplayName("Overtime decline notifies employee")
    public void overtimeDeclineNotifiesEmployee() {
        var employeeId = testData.employees.get(FMS_Empl_Jenson_Curtis);
        var employeeAuth = auth(FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        var managerAuth = auth(FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        var period = 202603;

        var report = overtimeService.addItem(employeeId, period,
                        new NewOvertimeItemDto(LocalDate.of(2026, 3, 10), testData.project_M1_FMS(), 4, "decline notify"),
                        employeeAuth)
                .block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(report);

        overtimeService.approveReport(employeeId, period, OvertimeApprovalDecisionDto.ApprovalDecision.DECLINED,
                        null, "Need details", managerAuth)
                .block(MONO_DEFAULT_TIMEOUT);

        StepVerifier.create(notificationService.myNotifications(employeeAuth)
                        .filter(n -> CATEGORY.equals(n.getCategory()))
                        .filter(n -> DECLINED_EVENT.equals(context(n).optString("eventType")))
                        .filter(n -> period == context(n).optInt("period")))
                .assertNext(n -> {
                    var context = context(n);
                    assertTrue(n.getId() > 0);
                    assertEquals(employeeId, context.optInt("employeeId"));
                    assertEquals(report.getId(), context.optInt("overtimeReportId"));
                    assertEquals(OvertimeApprovalDecisionDto.ApprovalDecision.DECLINED.name(), context.optString("decision"));
                    assertEquals(managerAuth.getEmployeeInfo().getEmployeeId(), context.optInt("approverEmployeeId"));
                    assertTrue(context.optInt("decisionId") > 0);
                })
                .verifyComplete();
    }

    private Mono<Void> cleanOvertimeData() {
        return db.sql("delete from notify.notification where category = 'overtime'").then()
                .then(db.sql("delete from ovt.overtime_item").then())
                .then(db.sql("delete from ovt.overtime_approval_decision").then())
                .then(db.sql("delete from ovt.overtime_period_history").then())
                .then(db.sql("delete from ovt.overtime_closed_period").then())
                .then(db.sql("delete from ovt.overtime_report").then());
    }

    private JSONObject context(NotificationDto notification) {
        try {
            return new JSONObject(notification.getContext());
        } catch (Exception ex) {
            throw new AssertionError("Notification context must be a valid JSON object", ex);
        }
    }
}
