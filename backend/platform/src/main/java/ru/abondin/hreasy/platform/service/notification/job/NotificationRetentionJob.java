package ru.abondin.hreasy.platform.service.notification.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.config.BackgroundTasksProps;
import ru.abondin.hreasy.platform.repo.notification.NotificationRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;

import java.time.Duration;

/**
 * Deletes old employee inbox notifications according to configured retention.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRetentionJob {

    private static final Duration MAX_WAIT = Duration.ofMinutes(5);

    private final BackgroundTasksProps props;
    private final DateTimeService dateTimeService;
    private final NotificationRepo notificationRepo;

    /**
     * Runs notification retention cleanup for the platform UI inbox.
     */
    @Scheduled(cron = "${hreasy.background.notification-retention.cron:0 30 3 * * *}")
    public void deleteOldNotifications() {
        var retention = props.getNotificationRetention();
        if (!retention.isEnabled()) {
            log.info("Notification retention job is disabled");
            return;
        }

        var cutoff = dateTimeService.now().minus(retention.getMaxAge());
        try {
            notificationRepo.deleteCreatedBefore(cutoff)
                    .count()
                    .doOnNext(deleted -> log.info("Deleted old platform notifications count={}, cutoff={}",
                            deleted,
                            cutoff))
                    .block(MAX_WAIT);
        } catch (Exception ex) {
            log.error("Platform notification retention job failed cutoff={}", cutoff, ex);
        }
    }
}
