package ru.abondin.hreasy.notifyms.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.notifyms.BasePostgresTest;
import ru.abondin.hreasy.notifyms.api.CreateNotificationRequest;
import ru.abondin.hreasy.notifyms.repo.NotificationDeliveryRepo;
import ru.abondin.hreasy.notifyms.repo.NotificationRepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationAcceptServiceTest extends BasePostgresTest {

    @Autowired
    private NotificationAcceptService service;
    @Autowired
    private NotificationRepo notificationRepo;
    @Autowired
    private NotificationDeliveryRepo deliveryRepo;

    /**
     * Test goal: verifies that accepting a notification persists event data and creates a Yandex delivery.
     * <p>Precondition: PostgreSQL schema is migrated and Yandex channel is enabled in test properties.
     * <p>Action: accept one notification request through {@link NotificationAcceptService}.
     * <p>Verification: notification row exists and exactly one Yandex delivery is queued with initial counters.
     */
    @Test
    void acceptCreatesNotificationAndYandexDelivery() {
        var request = request("assessment.assigned:456:123");

        StepVerifier.create(service.accept(request))
                .assertNext(id -> assertTrue(id > 0))
                .verifyComplete();

        StepVerifier.create(notificationRepo.findByDedupeKey(request.getDedupeKey()))
                .assertNext(saved -> {
                    assertEquals("assessment.assigned", saved.getEventType());
                    assertEquals("ivan.petrov@example.com", saved.getRecipientLogin());
                    assertTrue(saved.getBody().contains("self assessment"));
                })
                .verifyComplete();

        StepVerifier.create(deliveryRepo.findAll().collectList())
                .assertNext(deliveries -> {
                    assertEquals(1, deliveries.size());
                    var delivery = deliveries.getFirst();
                    assertEquals(NotificationChannel.yandex_messenger.name(), delivery.getChannel());
                    assertEquals(5, delivery.getMaxAttempts());
                    assertEquals(0, delivery.getAttemptCount());
                    assertEquals(0, delivery.getErrorCount());
                    assertEquals(request.getDedupeKey() + ":yandex_messenger", delivery.getProviderPayloadId());
                })
                .verifyComplete();
    }

    /**
     * Test goal: verifies deduplication by business key.
     * <p>Precondition: PostgreSQL schema is empty before the test.
     * <p>Action: accept the same notification request twice.
     * <p>Verification: both calls return the same notification id and only one notification/delivery pair exists.
     */
    @Test
    void acceptIsIdempotentByDedupeKey() {
        var request = request("assessment.assigned:456:123");

        var firstId = service.accept(request).block();
        var secondId = service.accept(request).block();

        assertNotNull(firstId);
        assertEquals(firstId, secondId);

        StepVerifier.create(notificationRepo.findAll().collectList())
                .assertNext(notifications -> assertEquals(1, notifications.size()))
                .verifyComplete();
        StepVerifier.create(deliveryRepo.findAll().collectList())
                .assertNext(deliveries -> assertEquals(1, deliveries.size()))
                .verifyComplete();
    }

    /**
     * Test goal: verifies that due delivery claiming is state-changing and non-repeatable.
     * <p>Precondition: one queued Yandex delivery exists.
     * <p>Action: claim due deliveries twice.
     * <p>Verification: the first claim returns the delivery as sending, the second claim returns no deliveries.
     */
    @Test
    void claimDueMarksDeliveryAsSending() {
        var request = request("assessment.assigned:456:123");
        service.accept(request).block();

        StepVerifier.create(deliveryRepo.claimDue(java.time.OffsetDateTime.now().plusMinutes(1), 10).collectList())
                .assertNext(deliveries -> {
                    assertEquals(1, deliveries.size());
                    assertEquals(DeliveryStatus.sending.name(), deliveries.getFirst().getStatus());
                })
                .verifyComplete();

        StepVerifier.create(deliveryRepo.claimDue(java.time.OffsetDateTime.now().plusMinutes(1), 10).collectList())
                .assertNext(deliveries -> assertTrue(deliveries.isEmpty()))
                .verifyComplete();
    }

    private CreateNotificationRequest request(String dedupeKey) {
        var recipient = new CreateNotificationRequest.Recipient();
        recipient.setType(RecipientType.user.name());
        recipient.setLogin("ivan.petrov@example.com");
        recipient.setEmployeeId(123);

        var request = new CreateNotificationRequest();
        request.setEventType("assessment.assigned");
        request.setRecipient(recipient);
        request.setPriority("normal");
        request.setDedupeKey(dedupeKey);
        request.setLocale("ru");
        request.setTitle("Assessment assigned");
        request.setBody("A self assessment form was assigned.");
        request.setData("{\"assessmentId\":456}");
        return request;
    }
}
