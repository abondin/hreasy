package ru.abondin.hreasy.platform.service.notification.sender;

import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationHandleResult;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationRoute;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

/**
 * Send notifications to different channels
 *
 * <ul>
 *     <li>0 - persist to database. Show in web UI</li>
 *     <li>1 - email</li>
 * </ul>
 * <p>
 */
public interface NotificationSender {

    int NOTIFICATION_DELIVERY_CHANNEL_PERSIST=0;
    int NOTIFICATION_DELIVERY_CHANNEL_EMAIL=1;

    /**
     * @param newNotificationDto
     * @param route
     * @return
     */
    Flux<NotificationHandleResult> send(NewNotificationDto newNotificationDto, NotificationRoute route);

}
