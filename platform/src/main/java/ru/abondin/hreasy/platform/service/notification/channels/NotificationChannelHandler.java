package ru.abondin.hreasy.platform.service.notification.channels;

import lombok.*;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Channel to send notification to the employee
 * 0 - save to database
 * 1 - sent via email
 */
public interface NotificationChannelHandler<T extends NotificationChannelHandler.NotificationHandleResult> {

    @Getter
    @AllArgsConstructor
    abstract class NotificationHandleResult {
        private final int employeeId;
        private final String notificationClientId;
        private final OffsetDateTime handledAt;
        private final int channelId;
    }

    @Getter
    @ToString
    class NotificationPersistHandleResult extends NotificationHandleResult {
        private final int notificationId;

        @Builder
        public NotificationPersistHandleResult(int employeeId, String notificationClientId, OffsetDateTime handledAt, int notificationId) {
            super(employeeId, notificationClientId, handledAt, 0);
            this.notificationId = notificationId;
        }
    }

    @Getter
    @ToString
    class NotificationEmailHandleResult extends NotificationHandleResult {
        @Builder
        public NotificationEmailHandleResult(int employeeId, String notificationClientId, OffsetDateTime handledAt) {
            super(employeeId, notificationClientId, handledAt, 1);
        }
    }

    interface NotificationPersistChannelHandler extends NotificationChannelHandler<NotificationPersistHandleResult> {
        default int channelId() {
            return 0;
        }
    }

    interface NotificationEmailChannelHandler extends NotificationChannelHandler<NotificationEmailHandleResult> {
        default int channelId() {
            return 1;
        }
    }

    @Data
    class Route {
        private final Integer sourceEmployeeId;
        private final List<Integer> destinationEmployeeIds;

        public static Route fromSystem(List<Integer> destinationEmployeeIds) {
            return new Route(null, destinationEmployeeIds);
        }

        public static Route fromEmployee(int employeeId, List<Integer> destinationEmployeeIds) {
            return new Route(employeeId, destinationEmployeeIds);
        }

        private Route(Integer sourceEmployeeId, List<Integer> destinationEmployeeIds) {
            this.sourceEmployeeId = sourceEmployeeId;
            this.destinationEmployeeIds = destinationEmployeeIds;
        }

        @Override
        public String toString() {
            return "{from: " + (sourceEmployeeId == null ? "system" : "employee:" + sourceEmployeeId) + ", to:" + destinationEmployeeIds + "}";
        }
    }

    /**
     * @return channel id to filter channels in #no
     */
    int channelId();

    /**
     * @param newNotificationDto
     */
    Flux<T> handleNotification(NewNotificationDto newNotificationDto, Route route);

}
