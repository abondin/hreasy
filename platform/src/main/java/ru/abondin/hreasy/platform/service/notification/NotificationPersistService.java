package ru.abondin.hreasy.platform.service.notification;

import io.r2dbc.postgresql.codec.Json;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.notification.NotificationEntry;
import ru.abondin.hreasy.platform.repo.notification.NotificationRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Save notification in database to show in UI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPersistService {

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public enum NotificationCategory {
        SALARY_REQUEST("salary_request"),
        UPCOMING_VACATION("upcoming_vacation");
        private final String category;
    }

    private final NotificationRepo repo;
    private final DateTimeService dateTimeService;

    public Flux<Integer> sendNotificationTo(NewNotificationDto newNotificationDto
            , List<Integer> employeesToNotify
            , @Nullable Integer initiatorEmployee) {
        log.info("Persist notification {} from {} to the database for {}"
                , newNotificationDto
                , initiatorEmployee == null ? "system" : initiatorEmployee
                , employeesToNotify);
        var entries = new ArrayList<NotificationEntry>();
        for (var employeeId : employeesToNotify) {
            var entry = new NotificationEntry();
            entry.setCreatedAt(dateTimeService.now());
            entry.setCreatedBy(initiatorEmployee);
            entry.setClientUuid(newNotificationDto.getClientUuid());
            entry.setCategory(newNotificationDto.getCategory());
            entry.setContext(Json.of(newNotificationDto.getContext()));
            entry.setTitle(newNotificationDto.getTitle());
            entry.setEmployee(employeeId);
            entry.setMarkdownText(newNotificationDto.getMarkdownText());
            entries.add(entry);
        }


        return repo.saveAll(entries).map(NotificationEntry::getId);
    }
}
