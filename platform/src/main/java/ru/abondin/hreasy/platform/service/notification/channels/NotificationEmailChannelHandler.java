package ru.abondin.hreasy.platform.service.notification.channels;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface NotificationEmailChannelHandler {


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
    Flux<NotificationEmailHandleResult> handleNotification(NewNotificationDto newNotificationDto, List<String> emails);
}
