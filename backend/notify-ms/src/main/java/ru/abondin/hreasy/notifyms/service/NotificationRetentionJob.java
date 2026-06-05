package ru.abondin.hreasy.notifyms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.abondin.hreasy.notifyms.config.NotificationProperties;
import ru.abondin.hreasy.notifyms.repo.NotificationDeliveryRepo;
import ru.abondin.hreasy.notifyms.repo.NotificationRepo;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Deletes old accepted notifications and delivery queue rows according to configured retention.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "hreasy.notifications.retention", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationRetentionJob {

    private static final Duration MAX_WAIT = Duration.ofMinutes(5);

    private final NotificationProperties props;
    private final NotificationDeliveryRepo deliveryRepo;
    private final NotificationRepo notificationRepo;

    /**
     * Runs retention cleanup for notify-ms normalized events and delivery queue rows.
     */
    @Scheduled(cron = "${hreasy.notifications.retention.cron:0 45 3 * * *}")
    public void deleteOldNotifications() {
        var cutoff = OffsetDateTime.now().minus(props.getRetention().getMaxAge());
        try {
            deliveryRepo.deleteForNotificationsCreatedBefore(cutoff)
                    .count()
                    .flatMap(deletedDeliveries -> notificationRepo.deleteCreatedBefore(cutoff)
                            .count()
                            .doOnNext(deletedNotifications -> log.info(
                                    "Deleted old notify-ms notifications count={}, deliveriesCount={}, cutoff={}",
                                    deletedNotifications,
                                    deletedDeliveries,
                                    cutoff)))
                    .block(MAX_WAIT);
        } catch (Exception ex) {
            log.error("Notify-ms notification retention job failed cutoff={}", cutoff, ex);
        }
    }
}
