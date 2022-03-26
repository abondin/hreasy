package ru.abondin.hreasy.platform.service.notification.channels;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.time.OffsetDateTime;

public interface NotificationPersistChannelHandler {
    @Getter
    @ToString
    class NotificationPersistHandleResult extends NotificationHandleResult {
        private final int notificationId;
        private final int employeeId;

        @Builder
        public NotificationPersistHandleResult(int employeeId, String notificationClientId, OffsetDateTime handledAt, int notificationId) {
            super(notificationClientId, handledAt, 0);
            this.employeeId = employeeId;
            this.notificationId = notificationId;
        }
    }

    /**
     * @param newNotificationDto
     */
    Flux<NotificationPersistHandleResult> handleNotification(NewNotificationDto newNotificationDto, NotificationRoute route);
}
