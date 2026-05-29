package ru.abondin.hreasy.notifyms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
        deliveryRepo.claimDue(now, props.getWorker().getBatchSize())
                .flatMap(this::processDelivery)
                .doOnError(e -> log.error("Notification delivery worker failed", e))
                .subscribe();
    }

    private Mono<NotificationDeliveryEntry> processDelivery(NotificationDeliveryEntry delivery) {
        return notificationRepo.findById(delivery.getNotificationId())
                .flatMap(notification -> send(delivery, notification));
    }

    private Mono<NotificationDeliveryEntry> send(NotificationDeliveryEntry delivery,
                                                 ru.abondin.hreasy.notifyms.repo.NotificationEntry notification) {
        if (NotificationChannel.yandex_messenger.name().equals(delivery.getChannel())) {
            return yandexProvider.send(notification, delivery)
                    .flatMap(result -> applyResult(delivery, result));
        }
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
            return deliveryRepo.save(delivery);
        }

        delivery.setErrorCount(delivery.getErrorCount() + 1);
        delivery.setLastErrorCode(result.errorCode());
        delivery.setLastErrorMessage(result.errorMessage());

        if (!result.retryable()) {
            delivery.setStatus(DeliveryStatus.failed_permanent.name());
            delivery.setNextAttemptAt(null);
            return deliveryRepo.save(delivery);
        }

        if (delivery.getErrorCount() >= delivery.getMaxAttempts()) {
            delivery.setStatus(DeliveryStatus.retry_exhausted.name());
            delivery.setNextAttemptAt(null);
            return deliveryRepo.save(delivery);
        }

        var nextAttempt = now.plus(retryDelay(delivery.getErrorCount()));
        delivery.setStatus(DeliveryStatus.retry_scheduled.name());
        delivery.setDueAt(nextAttempt);
        delivery.setNextAttemptAt(nextAttempt);
        return deliveryRepo.save(delivery);
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
