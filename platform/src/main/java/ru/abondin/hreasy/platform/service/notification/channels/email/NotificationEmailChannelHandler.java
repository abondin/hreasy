package ru.abondin.hreasy.platform.service.notification.channels.email;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationHandleResult;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationRoute;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.time.OffsetDateTime;

public interface NotificationEmailChannelHandler {

    String MAIL_CATEGORY_UPCOMING_VACATION="upcoming_vacation";

    @Getter
    @ToString
    class NotificationEmailHandleResult extends NotificationHandleResult {
        private String email;

        @Builder
        public NotificationEmailHandleResult(String email, String notificationClientId, OffsetDateTime handledAt) {
            super(notificationClientId, handledAt, 1);
            this.email = email;
        }
    }

    /**
     * @param newNotificationDto
     */
    Flux<NotificationEmailHandleResult> handleNotification(NewNotificationDto newNotificationDto, NotificationRoute route);
}
