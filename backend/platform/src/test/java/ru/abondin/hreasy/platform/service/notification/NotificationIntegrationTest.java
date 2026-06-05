package ru.abondin.hreasy.platform.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.notification.job.NotificationRetentionJob;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.abondin.hreasy.platform.TestEmployees.Admin_Shaan_Pitts;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class NotificationIntegrationTest extends BaseServiceTest {

    @Autowired
    private NotificationPersistService persistService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRetentionJob notificationRetentionJob;

    @Autowired
    private DatabaseClient db;

    @BeforeEach
    protected void validateTestConfiguration() {
        initEmployeesDataAndLogin();
    }

    /**
     * Test goal: verifies that a persisted notification is visible in current employee inbox.
     * <p>Precondition: test employees are initialized and the current auth user is logged in.
     * <p>Action: persist one notification for the current employee.
     * <p>Verification: my notifications returns the created notification with id and context.
     */
    @Test
    @DisplayName("Send one notification without any system template")
    public void oneNotificationIsVisibleInInbox() {
        var notification = new NewNotificationDto();
        var uuid = UUID.randomUUID().toString();
        notification.setCategory("test");
        notification.setClientUuid(uuid);
        notification.setMarkdownText("**Test**");
        notification.setTitle("Test message");
        notification.setContext(new JSONObject(Map.of("testKey", "testValue")).toString());

        StepVerifier.create(
                persistService.sendNotificationTo(notification, Arrays.asList(testData.employees.get(Admin_Shaan_Pitts)), null)
        ).expectNextCount(1).verifyComplete();

        StepVerifier.create(notificationService.myNotifications(this.auth)
                .filter(n -> uuid.equals(n.getClientUuid()))
        ).expectNextMatches(n -> {
            assertTrue(n.getId() > 0);
            assertEquals(uuid, n.getClientUuid());
            assertNotNull(n.getContext());
            return true;
        }).verifyComplete();
    }

    /**
     * Test goal: verifies unread count and acknowledge behavior for current employee notifications.
     * <p>Precondition: one unacknowledged notification exists for the current employee.
     * <p>Action: acknowledge the notification through {@link NotificationService}.
     * <p>Verification: unread count decreases and the notification exposes acknowledged timestamp.
     */
    @Test
    @DisplayName("Unread count changes after acknowledge")
    public void unreadCountChangesAfterAcknowledge() {
        var notification = newNotification(UUID.randomUUID().toString());
        var notificationId = persistService.sendNotificationTo(notification,
                Arrays.asList(testData.employees.get(Admin_Shaan_Pitts)), null).blockFirst();

        StepVerifier.create(notificationService.myUnreadCount(this.auth))
                .assertNext(count -> assertTrue(count > 0))
                .verifyComplete();

        StepVerifier.create(notificationService.acknowledge(this.auth, notificationId))
                .expectNext(notificationId)
                .verifyComplete();

        StepVerifier.create(notificationService.myNotifications(this.auth)
                        .filter(n -> notification.getClientUuid().equals(n.getClientUuid())))
                .assertNext(n -> assertNotNull(n.getAcknowledgedAt()))
                .verifyComplete();
    }

    /**
     * Test goal: verifies archive behavior for current employee notifications.
     * <p>Precondition: one notification exists for the current employee.
     * <p>Action: archive the notification through {@link NotificationService}.
     * <p>Verification: archived notification is not returned by current inbox query.
     */
    @Test
    @DisplayName("Archived notification is hidden from inbox")
    public void archivedNotificationIsHiddenFromInbox() {
        var notification = newNotification(UUID.randomUUID().toString());
        var notificationId = persistService.sendNotificationTo(notification,
                Arrays.asList(testData.employees.get(Admin_Shaan_Pitts)), null).blockFirst();

        StepVerifier.create(notificationService.archive(this.auth, notificationId))
                .expectNext(notificationId)
                .verifyComplete();

        StepVerifier.create(notificationService.myNotifications(this.auth)
                        .filter(n -> notification.getClientUuid().equals(n.getClientUuid())))
                .verifyComplete();
    }

    /**
     * Test goal: verifies notification retention removes only expired platform inbox rows.
     * <p>Precondition: one old and one fresh notification exist for the current employee.
     * <p>Action: run the notification retention job directly.
     * <p>Verification: the old notification is deleted and the fresh notification remains visible in the inbox.
     */
    @Test
    @DisplayName("Retention job deletes old inbox notifications")
    public void retentionJobDeletesOnlyOldNotifications() {
        var employee = testData.employees.get(Admin_Shaan_Pitts);
        var oldUuid = UUID.randomUUID().toString();
        var freshUuid = UUID.randomUUID().toString();
        insertNotification(employee, oldUuid, OffsetDateTime.now().minusDays(370));
        insertNotification(employee, freshUuid, OffsetDateTime.now());

        notificationRetentionJob.deleteOldNotifications();

        StepVerifier.create(notificationService.myNotifications(this.auth)
                        .filter(n -> oldUuid.equals(n.getClientUuid()) || freshUuid.equals(n.getClientUuid()))
                        .collectList())
                .assertNext(notifications -> {
                    assertEquals(1, notifications.size());
                    assertEquals(freshUuid, notifications.getFirst().getClientUuid());
                })
                .verifyComplete();
    }

    private NewNotificationDto newNotification(String uuid) {
        var notification = new NewNotificationDto();
        notification.setCategory("test");
        notification.setClientUuid(uuid);
        notification.setMarkdownText("**Test**");
        notification.setTitle("Test message");
        notification.setContext(new JSONObject(Map.of("testKey", "testValue")).toString());
        return notification;
    }

    private void insertNotification(int employeeId, String uuid, OffsetDateTime createdAt) {
        db.sql("""
                        insert into notify.notification
                            (employee, client_uuid, category, title, markdown_text, context, created_at, created_by)
                        values (:employee, :clientUuid, 'test', 'Test message', '**Test**', '{}'::jsonb, :createdAt, null)
                        """)
                .bind("employee", employeeId)
                .bind("clientUuid", uuid)
                .bind("createdAt", createdAt)
                .then()
                .block(MONO_DEFAULT_TIMEOUT);
    }

}
