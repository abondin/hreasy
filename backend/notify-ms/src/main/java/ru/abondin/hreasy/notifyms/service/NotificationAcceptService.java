package ru.abondin.hreasy.notifyms.service;

import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.notifyms.api.CreateNotificationRequest;
import ru.abondin.hreasy.notifyms.config.NotificationProperties;
import ru.abondin.hreasy.notifyms.repo.NotificationDeliveryEntry;
import ru.abondin.hreasy.notifyms.repo.NotificationDeliveryRepo;
import ru.abondin.hreasy.notifyms.repo.NotificationEntry;
import ru.abondin.hreasy.notifyms.repo.NotificationRepo;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationAcceptService {
    private final NotificationRepo notificationRepo;
    private final NotificationDeliveryRepo deliveryRepo;
    private final NotificationProperties props;
    private final BusinessHoursService businessHoursService;

    @Transactional
    public Mono<Long> accept(CreateNotificationRequest request) {
        return notificationRepo.findByDedupeKey(request.getDedupeKey())
                .doOnNext(existing -> log.info("Notification deduplicated id={}, dedupeKey={}",
                        existing.getId(),
                        request.getDedupeKey()))
                .map(NotificationEntry::getId)
                .switchIfEmpty(Mono.defer(() -> createNew(request)))
                .onErrorResume(DuplicateKeyException.class,
                        e -> {
                            log.info("Notification deduplicated after duplicate key dedupeKey={}", request.getDedupeKey());
                            return notificationRepo.findByDedupeKey(request.getDedupeKey()).map(NotificationEntry::getId);
                        });
    }

    private Mono<Long> createNew(CreateNotificationRequest request) {
        var now = OffsetDateTime.now();
        var entry = new NotificationEntry();
        entry.setEventType(request.getEventType());
        entry.setRecipientType(request.getRecipient().getType());
        entry.setRecipientLogin(request.getRecipient().getLogin());
        entry.setRecipientChatId(request.getRecipient().getChatId());
        entry.setEmployeeId(request.getRecipient().getEmployeeId());
        entry.setDedupeKey(request.getDedupeKey());
        entry.setPriority(StringUtils.hasText(request.getPriority()) ? request.getPriority() : "normal");
        entry.setLocale(request.getLocale());
        entry.setTitle(request.getTitle());
        entry.setBody(request.getBody());
        entry.setData(StringUtils.hasText(request.getData()) ? Json.of(request.getData()) : null);
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);

        return notificationRepo.save(entry)
                .doOnNext(saved -> log.info("Notification saved id={}, eventType={}, dedupeKey={}, recipientType={}, recipientLogin={}, employeeId={}",
                        saved.getId(),
                        saved.getEventType(),
                        saved.getDedupeKey(),
                        saved.getRecipientType(),
                        saved.getRecipientLogin(),
                        saved.getEmployeeId()))
                .flatMap(saved -> createDeliveries(saved, now)
                        .then(Mono.just(saved.getId())));
    }

    private Flux<NotificationDeliveryEntry> createDeliveries(NotificationEntry notification, OffsetDateTime now) {
        return Flux.concat(
                createYandexDelivery(notification, now),
                createEmailDelivery(notification, now)
        );
    }

    private Mono<NotificationDeliveryEntry> createYandexDelivery(NotificationEntry notification, OffsetDateTime now) {
        var channel = props.getChannels().getYandexMessenger();
        if (!channel.isEnabled()) {
            log.info("Skip disabled delivery channel notificationId={}, channel={}",
                    notification.getId(),
                    NotificationChannel.yandex_messenger.name());
            return Mono.empty();
        }
        var dueAt = businessHoursService.dueAt(channel.getMode(), now);
        var delivery = baseDelivery(notification, now);
        delivery.setChannel(NotificationChannel.yandex_messenger.name());
        delivery.setStatus(dueAt.isAfter(now) ? DeliveryStatus.deferred.name() : DeliveryStatus.queued.name());
        delivery.setDueAt(dueAt);
        delivery.setMaxAttempts(channel.getMaxAttempts());
        delivery.setProviderPayloadId(notification.getDedupeKey() + ":yandex_messenger");
        log.debug("Prepare Yandex Messenger delivery notificationId={}, mode={}, now={}, dueAt={}, status={}",
                notification.getId(),
                channel.getMode(),
                now,
                dueAt,
                delivery.getStatus());
        return deliveryRepo.save(delivery)
                .doOnNext(saved -> log.info("Notification delivery created id={}, notificationId={}, channel={}, status={}, dueAt={}, maxAttempts={}",
                        saved.getId(),
                        saved.getNotificationId(),
                        saved.getChannel(),
                        saved.getStatus(),
                        saved.getDueAt(),
                        saved.getMaxAttempts()));
    }

    private Mono<NotificationDeliveryEntry> createEmailDelivery(NotificationEntry notification, OffsetDateTime now) {
        var channel = props.getChannels().getEmail();
        if (!channel.isEnabled()) {
            log.info("Skip disabled delivery channel notificationId={}, channel=email", notification.getId());
            return Mono.empty();
        }
        return Mono.error(new IllegalStateException("Email digest channel is not implemented yet"));
    }

    private NotificationDeliveryEntry baseDelivery(NotificationEntry notification, OffsetDateTime now) {
        var delivery = new NotificationDeliveryEntry();
        delivery.setNotificationId(notification.getId());
        delivery.setAttemptCount(0);
        delivery.setErrorCount(0);
        delivery.setCreatedAt(now);
        delivery.setUpdatedAt(now);
        return delivery;
    }
}
