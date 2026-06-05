package ru.abondin.hreasy.notifyms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.notifyms.config.NotificationProperties;
import ru.abondin.hreasy.notifyms.provider.ProviderSendResult;
import ru.abondin.hreasy.notifyms.provider.YandexMessengerProvider;
import ru.abondin.hreasy.notifyms.repo.NotificationDeliveryEntry;
import ru.abondin.hreasy.notifyms.repo.NotificationDeliveryRepo;
import ru.abondin.hreasy.notifyms.repo.NotificationRepo;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "hreasy.notifications.worker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DeliveryWorker {
    private final NotificationProperties props;
    private final NotificationDeliveryRepo deliveryRepo;
    private final NotificationRepo notificationRepo;
    private final YandexMessengerProvider yandexProvider;

    @Scheduled(fixedDelayString = "${hreasy.notifications.worker.fixed-delay:10s}")
    public void run() {
        var now = OffsetDateTime.now();
        try {
            deliveryRepo.claimDue(now, props.getWorker().getBatchSize())
                    .collectList()
                    .flatMapMany(deliveries -> {
                        if (!deliveries.isEmpty()) {
                            log.info("Claimed notification deliveries count={}, batchSize={}",
                                    deliveries.size(),
                                    props.getWorker().getBatchSize());
                        }
                        return Flux.fromIterable(deliveries);
                    })
                    .flatMap(this::processDelivery)
                    .then()
                    .block();
        } catch (Exception e) {
            log.error("Notification delivery worker failed", e);
        }
    }

    private Mono<NotificationDeliveryEntry> processDelivery(NotificationDeliveryEntry delivery) {
        return notificationRepo.findById(delivery.getNotificationId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Notification not found for delivery id={}, notificationId={}",
                            delivery.getId(),
                            delivery.getNotificationId());
                    delivery.setStatus(DeliveryStatus.failed_permanent.name());
                    delivery.setLastErrorCode("notification_not_found");
                    delivery.setLastErrorMessage("Notification row not found");
                    delivery.setUpdatedAt(OffsetDateTime.now());
                    return deliveryRepo.save(delivery).then(Mono.empty());
                }))
                .flatMap(notification -> send(delivery, notification));
    }

    private Mono<NotificationDeliveryEntry> send(NotificationDeliveryEntry delivery,
                                                 ru.abondin.hreasy.notifyms.repo.NotificationEntry notification) {
        log.info("Send notification delivery id={}, notificationId={}, eventType={}, channel={}, attempt={}/{}",
                delivery.getId(),
                notification.getId(),
                notification.getEventType(),
                delivery.getChannel(),
                delivery.getAttemptCount() + 1,
                delivery.getMaxAttempts());
        if (NotificationChannel.yandex_messenger.name().equals(delivery.getChannel())) {
            return yandexProvider.send(notification, delivery)
                    .flatMap(result -> applyResult(delivery, result));
        }
        log.warn("Unsupported notification delivery channel id={}, notificationId={}, channel={}",
                delivery.getId(),
                delivery.getNotificationId(),
                delivery.getChannel());
        return applyResult(delivery, ProviderSendResult.permanent(null, "unsupported_channel", "Unsupported channel: " + delivery.getChannel()));
    }

    private Mono<NotificationDeliveryEntry> applyResult(NotificationDeliveryEntry delivery, ProviderSendResult result) {
        var now = OffsetDateTime.now();
        delivery.setAttemptCount(delivery.getAttemptCount() + 1);
        delivery.setLastAttemptAt(now);
        delivery.setProviderStatusCode(result.statusCode());
        delivery.setUpdatedAt(now);

        if (result.success()) {
            delivery.setStatus(DeliveryStatus.sent.name());
            delivery.setLastSuccessAt(now);
            delivery.setExternalMessageId(result.externalMessageId());
            delivery.setLastErrorCode(null);
            delivery.setLastErrorMessage(null);
            delivery.setNextAttemptAt(null);
            return deliveryRepo.save(delivery)
                    .doOnNext(saved -> log.info("Notification delivery sent id={}, notificationId={}, channel={}, externalMessageId={}",
                            saved.getId(),
                            saved.getNotificationId(),
                            saved.getChannel(),
                            saved.getExternalMessageId()));
        }

        delivery.setErrorCount(delivery.getErrorCount() + 1);
        delivery.setLastErrorCode(result.errorCode());
        delivery.setLastErrorMessage(result.errorMessage());

        if (!result.retryable()) {
            delivery.setStatus(DeliveryStatus.failed_permanent.name());
            delivery.setNextAttemptAt(null);
            return deliveryRepo.save(delivery)
                    .doOnNext(saved -> log.warn("Notification delivery failed permanently id={}, notificationId={}, channel={}, statusCode={}, errorCode={}",
                            saved.getId(),
                            saved.getNotificationId(),
                            saved.getChannel(),
                            saved.getProviderStatusCode(),
                            saved.getLastErrorCode()));
        }

        if (delivery.getErrorCount() >= delivery.getMaxAttempts()) {
            delivery.setStatus(DeliveryStatus.retry_exhausted.name());
            delivery.setNextAttemptAt(null);
            return deliveryRepo.save(delivery)
                    .doOnNext(saved -> log.warn("Notification delivery retry exhausted id={}, notificationId={}, channel={}, errorCount={}, errorCode={}",
                            saved.getId(),
                            saved.getNotificationId(),
                            saved.getChannel(),
                            saved.getErrorCount(),
                            saved.getLastErrorCode()));
        }

        var nextAttempt = now.plus(retryDelay(delivery.getErrorCount()));
        delivery.setStatus(DeliveryStatus.retry_scheduled.name());
        delivery.setDueAt(nextAttempt);
        delivery.setNextAttemptAt(nextAttempt);
        return deliveryRepo.save(delivery)
                .doOnNext(saved -> log.warn("Notification delivery retry scheduled id={}, notificationId={}, channel={}, errorCount={}, nextAttemptAt={}, errorCode={}",
                        saved.getId(),
                        saved.getNotificationId(),
                        saved.getChannel(),
                        saved.getErrorCount(),
                        saved.getNextAttemptAt(),
                        saved.getLastErrorCode()));
    }

    private java.time.Duration retryDelay(int errorCount) {
        return switch (errorCount) {
            case 1 -> java.time.Duration.ofMinutes(1);
            case 2 -> java.time.Duration.ofMinutes(5);
            case 3 -> java.time.Duration.ofMinutes(15);
            default -> java.time.Duration.ofHours(1);
        };
    }
}
