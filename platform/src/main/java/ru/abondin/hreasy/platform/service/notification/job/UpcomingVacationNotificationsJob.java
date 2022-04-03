package ru.abondin.hreasy.platform.service.notification.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.config.BackgroundTasksProps;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.notification.upcomingvacations.UpcomingVacationNotificationService;

import java.time.Duration;
import java.util.UUID;

/**
 * Check every 6 hours upcoming vacations (new or updated).
 * If vacations starts less than 3 weeks
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpcomingVacationNotificationsJob {

    private final BackgroundTasksProps props;
    private final DateTimeService dateTimeService;
    private final UpcomingVacationNotificationService service;

    private final Duration MAX_WAIT = Duration.ofMinutes(5);

    @Scheduled(fixedDelay = 3600000)
    public void sendEmailForUpcomingVacations() {
        if (!props.getUpcomingVacation().isJobEnabled()) {
            log.info("Upcoming Vacation Notifications Job is disabled. Please use hreasy.background.upcomingVacation.upcomingVacationJobEnabled=true");
            return;
        }
        var startTime = dateTimeService.now();
        var jobId = UUID.randomUUID().toString();
        log.info("Start upcoming vacation notifications job {}: {}", jobId, startTime);
        try {
            service.notifyUpcomingVacations()
                    .doOnError(e -> {
                        var endTime = dateTimeService.now();
                        log.error("Upcoming vacation notifications job failed {}: {}. Duration={}", jobId,
                                endTime, Duration.between(startTime, endTime));
                    })
                    .count().map(cnt -> {
                        var endTime = dateTimeService.now();
                        log.info("Complete upcoming vacation notifications job {}. Sent notifications: {}. Duration={}", jobId,
                                cnt, Duration.between(startTime, endTime));
                        return cnt;
                    }).block(MAX_WAIT);
        } catch (Exception ex) {
            log.error("Unexpected exception during upcoming vacation notifications job {}", jobId, ex);
        }
    }

}
