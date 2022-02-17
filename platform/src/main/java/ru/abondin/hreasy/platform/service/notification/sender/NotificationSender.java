package ru.abondin.hreasy.platform.service.notification.sender;

import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationChannelHandler;
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

    /**
     * @param newNotificationDto
     * @param route
     * @return
     */
    Flux<NotificationChannelHandler.NotificationHandleResult> send(NewNotificationDto newNotificationDto, NotificationChannelHandler.Route route);

}
