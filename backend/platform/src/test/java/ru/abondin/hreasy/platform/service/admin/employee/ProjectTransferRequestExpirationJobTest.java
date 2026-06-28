package ru.abondin.hreasy.platform.service.admin.employee;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.repo.employee.projecttransfer.ProjectTransferRequestEntry;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.TestFixedDataTimeConfig;

import java.time.OffsetDateTime;
import java.util.List;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Import(TestFixedDataTimeConfig.class)
class ProjectTransferRequestExpirationJobTest extends BaseServiceTest {

    @Autowired
    private ProjectTransferRequestExpirationJob expirationJob;

    @Autowired
    private DateTimeService dateTimeService;

    @Autowired
    private DatabaseClient db;

    @BeforeEach
    void beforeEach() {
        ((TestFixedDataTimeConfig.TestFixedDataTimeService) dateTimeService)
                .init(OffsetDateTime.parse("2026-06-22T12:00:00+03:00"));
        initEmployeesDataAndLogin();
        db.sql("delete from notify.notification where category = 'project_transfer'")
                .then()
                .then(db.sql("delete from empl.project_transfer_request")
                        .then())
                .block(MONO_DEFAULT_TIMEOUT);
    }

    /**
     * Test goal: verifies that the expiration job closes only old pending project transfer requests.
     * <p>Precondition: one old pending, one fresh pending, and one old approved transfer request exist.
     * <p>Action: run the project transfer request expiration job directly.
     * <p>Verification: only the old pending request becomes expired, gets the fixed updated timestamp, and notifies creator/approver with expiration context.
     */
    @Test
    @DisplayName("Expiration job expires only old pending project transfer requests")
    void expireOldPendingRequestsExpiresOnlyOldPendingRequests() {
        var now = dateTimeService.now();
        var oldPending = insertTransferRequest(
                testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob),
                ProjectTransferRequestEntry.STATE_PENDING,
                now.minusDays(15));
        var freshPending = insertTransferRequest(
                testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis),
                ProjectTransferRequestEntry.STATE_PENDING,
                now.minusDays(13));
        var oldApproved = insertTransferRequest(
                testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob),
                ProjectTransferRequestEntry.STATE_APPROVED,
                now.minusDays(15));

        expirationJob.expireOldPendingRequests();

        StepVerifier.create(Mono.zip(
                        transferRequestState(oldPending),
                        transferRequestState(freshPending),
                        transferRequestState(oldApproved),
                        projectTransferNotifications(oldPending)))
                .assertNext(states -> {
                    Assertions.assertEquals(ProjectTransferRequestEntry.STATE_EXPIRED, states.getT1().state());
                    Assertions.assertEquals(now.toInstant(), states.getT1().updatedAt().toInstant());
                    Assertions.assertNull(states.getT1().updatedBy());

                    Assertions.assertEquals(ProjectTransferRequestEntry.STATE_PENDING, states.getT2().state());
                    Assertions.assertNull(states.getT2().updatedAt());

                    Assertions.assertEquals(ProjectTransferRequestEntry.STATE_APPROVED, states.getT3().state());
                    Assertions.assertNull(states.getT3().updatedAt());

                    Assertions.assertEquals(2, states.getT4().size());
                    Assertions.assertTrue(hasNotification(states.getT4(),
                            testData.employees.get(TestEmployees.FMS_Manager_Jawad_Mcghee),
                            "project_transfer.request_expired"));
                    Assertions.assertTrue(hasNotification(states.getT4(),
                            testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May),
                            "project_transfer.request_expired"));
                    Assertions.assertTrue(states.getT4().stream().allMatch(notification -> notification.expiresAt() != null));
                })
                .verifyComplete();
    }

    private Integer insertTransferRequest(int employeeId, short state, OffsetDateTime createdAt) {
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
                            :createdAt,
                            :createdBy
                        )
                        returning id
                        """)
                .bind("employeeId", employeeId)
                .bind("fromProjectId", testData.project_M1_Billing())
                .bind("toProjectId", testData.project_M1_FMS())
                .bind("approverEmployeeId", testData.employees.get(TestEmployees.Billing_Manager_Maxwell_May))
                .bind("state", state)
                .bind("createdAt", createdAt)
                .bind("createdBy", testData.employees.get(TestEmployees.FMS_Manager_Jawad_Mcghee))
                .map(row -> row.get("id", Integer.class))
                .one()
                .block(MONO_DEFAULT_TIMEOUT);
    }

    private Mono<ProjectTransferRequestState> transferRequestState(int requestId) {
        return db.sql("""
                        select state, updated_at, updated_by
                        from empl.project_transfer_request
                        where id = :requestId
                        """)
                .bind("requestId", requestId)
                .map(row -> new ProjectTransferRequestState(
                        row.get("state", Number.class).shortValue(),
                        row.get("updated_at", OffsetDateTime.class),
                        row.get("updated_by", Integer.class)))
                .one();
    }

    private Mono<List<ProjectTransferNotification>> projectTransferNotifications(int requestId) {
        return db.sql("""
                        select employee, context ->> 'eventType' as event_type, context ->> 'expiresAt' as expires_at
                        from notify.notification
                        where category = 'project_transfer'
                          and context ->> 'projectTransferRequestId' = :requestId
                        """)
                .bind("requestId", Integer.toString(requestId))
                .map(row -> new ProjectTransferNotification(
                        row.get("employee", Integer.class),
                        row.get("event_type", String.class),
                        row.get("expires_at", String.class)))
                .all()
                .collectList();
    }

    private boolean hasNotification(List<ProjectTransferNotification> notifications,
                                    int employeeId,
                                    String eventType) {
        return notifications.stream()
                .anyMatch(notification -> notification.employeeId() == employeeId
                        && eventType.equals(notification.eventType()));
    }

    private record ProjectTransferRequestState(Short state,
                                               OffsetDateTime updatedAt,
                                               Integer updatedBy) {
    }

    private record ProjectTransferNotification(Integer employeeId, String eventType, String expiresAt) {
    }
}
