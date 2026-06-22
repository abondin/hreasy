package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.config.BackgroundTasksProps;
import ru.abondin.hreasy.platform.repo.employee.projecttransfer.ProjectTransferRequestRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.notification.NotificationOrchestrator;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectTransferRequestExpirationJob {

    private static final Duration MAX_WAIT = Duration.ofMinutes(5);

    private final BackgroundTasksProps props;
    private final DateTimeService dateTimeService;
    private final ProjectTransferRequestRepo projectTransferRequestRepo;
    private final NotificationOrchestrator notificationOrchestrator;

    @Scheduled(cron = "${hreasy.background.project-transfer-request-expiration.cron:0 0 2 * * *}")
    public void expireOldPendingRequests() {
        var expiration = props.getProjectTransferRequestExpiration();
        if (!expiration.isEnabled()) {
            log.info("Project transfer request expiration job is disabled");
            return;
        }

        var now = dateTimeService.now();
        var createdBefore = now.minus(expiration.getMaxAge());
        try {
            // Process: expiration is a background cleanup of pending requests by created_at + configured max age.
            // The job is not a strict consistency gate; transfer/update commands still rely on the current DB state.
            projectTransferRequestRepo.expirePendingCreatedBefore(createdBefore, now)
                    .flatMap(request -> notificationOrchestrator.publishBestEffort(
                                    ProjectTransferRequestNotificationEvent.expired(request))
                            .thenReturn(request))
                    .count()
                    .doOnNext(expired -> log.info("Expired project transfer requests count={}, createdBefore={}",
                            expired,
                            createdBefore))
                    .block(MAX_WAIT);
        } catch (Exception ex) {
            log.error("Project transfer request expiration job failed createdBefore={}", createdBefore, ex);
        }
    }
}
