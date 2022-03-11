package ru.abondin.hreasy.platform.service.notification.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.config.BackgroundTasksProps;
import ru.abondin.hreasy.platform.service.DateTimeService;

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

    @Scheduled(fixedDelay = 10000)
    public void sendEmailForUpcomingVacations() {
        if (!props.getUpcomingVacation().isUpcomingVacationJobEnabled()) {
            log.info("Upcoming Vacation Notifications Job is disabled. Please use hreasy.background.upcomingVacation.upcomingVacationJobEnabled=true");
        }
        var startTime = dateTimeService.now();
        var jobId = UUID.randomUUID().toString();
        log.info("Start upcoming vacation notifications job {}: {}", jobId, startTime);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        var endTime = dateTimeService.now();
        log.info("Complete upcoming vacation notifications job {}: {}. Duration={}", jobId,
                endTime, Duration.between(startTime, endTime));
    }

}
