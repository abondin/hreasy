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
import ru.abondin.hreasy.platform.service.notification.channels.NotificationChannelHandler;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;
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

    @Transactional
    public Flux<NotificationChannelHandler.NotificationHandleResult> notifyUpcomingVacations() {
        var now = dateTimeService.now();
        log.info("Notify Upcoming Vacations");
        // 1. Load batch of vacations
        return vacationRepo.findStartedSince(now.minus(props.getUpcomingVacation().getStartTimeThreshold()))
                .buffer(1000)
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

    private Flux<NotificationChannelHandler.NotificationHandleResult> sendNotifications(List<VacationEntry> vacations, OffsetDateTime now) {
        return Flux.fromStream(vacations.stream())
                .flatMap(v -> {
                            var notification = new NewNotificationDto();
                            return notificationSender.send(notification,
                                    NotificationChannelHandler.Route.fromSystem(Arrays.asList(v.getEmployee())));
                        }
                );
    }

}
