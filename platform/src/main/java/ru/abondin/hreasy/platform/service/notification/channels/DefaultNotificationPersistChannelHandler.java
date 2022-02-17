package ru.abondin.hreasy.platform.service.notification.channels;

import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.notification.NotificationEntry;
import ru.abondin.hreasy.platform.repo.notification.NotificationRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.util.ArrayList;

/**
 * Save notification in database to show in UI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultNotificationPersistChannelHandler implements NotificationChannelHandler.NotificationPersistChannelHandler {

    public static final int DEFAULT_PERSIST_PARTITION_SIZE = 1000;

    private final NotificationRepo repo;
    private final DateTimeService dateTimeService;

    @Override
    public Flux<NotificationChannelHandler.NotificationPersistHandleResult> handleNotification(NewNotificationDto newNotificationDto, Route context) {
        log.info("Persist notification {} to the database by {}", newNotificationDto, context);
        var entries = new ArrayList<NotificationEntry>();
        for (var employeeId : context.getDestinationEmployeeIds()) {
            var entry = new NotificationEntry();
            entry.setCreatedAt(dateTimeService.now());
            entry.setCreatedBy(context.getSourceEmployeeId());
            entry.setClientUuid(newNotificationDto.getClientUuid());
            entry.setCategory(newNotificationDto.getCategory());
            entry.setContext(Json.of(newNotificationDto.getContext()));
            entry.setDeliveryChannels(newNotificationDto.getDeliveryChannels());
            entry.setTitle(newNotificationDto.getTitle());
            entry.setEmployee(employeeId);
            entry.setMarkdownText(newNotificationDto.getMarkdownText());
            entries.add(entry);
        }


        return repo.saveAll(entries).map(e -> NotificationPersistHandleResult.builder()
                .employeeId(e.getEmployee())
                .notificationId(e.getId())
                .handledAt(dateTimeService.now())
                .notificationClientId(e.getClientUuid())
                .build());
    }
}
