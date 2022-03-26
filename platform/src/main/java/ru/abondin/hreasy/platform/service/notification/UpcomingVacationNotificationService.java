package ru.abondin.hreasy.platform.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.config.BackgroundTasksProps;
import ru.abondin.hreasy.platform.repo.notification.UpcomingVacationNotificationLogEntry;
import ru.abondin.hreasy.platform.repo.notification.UpcomingVacationNotificationLogRepo;
import ru.abondin.hreasy.platform.repo.vacation.VacationEntry;
import ru.abondin.hreasy.platform.repo.vacation.VacationRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationHandleResult;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationRoute;
import ru.abondin.hreasy.platform.service.notification.sender.NotificationSender;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handle
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpcomingVacationNotificationService {
    private final DateTimeService dateTimeService;
    private final UpcomingVacationNotificationLogRepo logRepo;
    private final VacationRepo vacationRepo;
    private final BackgroundTasksProps props;
    private final NotificationSender notificationSender;
    private final UpcomingNotificationTemplate template;

    @Transactional
    public Flux<NotificationHandleResult> notifyUpcomingVacations() {
        var now = dateTimeService.now();
        var nowDate = now.toLocalDate();
        log.info("Notify Upcoming Vacations");
        // 1. Load batch of vacations
        return vacationRepo.findActiveStartedBeetwen(nowDate, nowDate.plusDays(props.getUpcomingVacation().getStartTimeThresholdDays()))
                .buffer(props.getDefaultBufferSize())
                // 2. Filter all already sent notifications for the event
                .flatMap(vacations -> logRepo.vacationsIn(vacations.stream().map(VacationEntry::getId).collect(Collectors.toList())).collectList()
                        .map(alreadySent -> vacations.stream().filter(v -> !alreadySent.contains(v.getId())).collect(Collectors.toList())))
                // 3. Mark vacations as sent in database
                .flatMap(vacations -> markVacationsAsPersisted(vacations, now)
                        // 4. Send notification
                        .thenMany(sendNotifications(vacations, now))
                );
    }

    private Flux<UpcomingVacationNotificationLogEntry> markVacationsAsPersisted(List<VacationEntry> vacations, OffsetDateTime now) {
        return logRepo.saveAll(vacations.stream().map(v -> {
            var logEntry = new UpcomingVacationNotificationLogEntry();
            logEntry.setVacation(v.getId());
            logEntry.setCreatedAt(now);
            logEntry.setEmployee(v.getEmployee());
            return logEntry;
        }).collect(Collectors.toList()));
    }

    private Flux<NotificationHandleResult> sendNotifications(List<VacationEntry> vacations, OffsetDateTime now) {
        return Flux.fromStream(vacations.stream())
                .flatMap(v -> {
                            var notification = template.create(v);
                            return notificationSender.send(notification,
                                    NotificationRoute.fromSystem(Arrays.asList(v.getEmployee())));
                        }
                );
    }

}
