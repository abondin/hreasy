package ru.abondin.hreasy.platform.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationChannelHandler;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.time.OffsetDateTime;

@Primary
@Service
@Slf4j
public class DummyEmailNotificationHandler implements NotificationChannelHandler.NotificationEmailChannelHandler {
    @Override
    public Flux<NotificationEmailHandleResult> handleNotification(NewNotificationDto newNotificationDto, Route route) {
        return Flux.fromIterable(route.getDestinationEmployeeIds()).map(e -> NotificationEmailHandleResult.builder()
                .employeeId(e)
                .notificationClientId(newNotificationDto.getClientUuid())
                .handledAt(OffsetDateTime.now())
                .build()
        );
    }
}
