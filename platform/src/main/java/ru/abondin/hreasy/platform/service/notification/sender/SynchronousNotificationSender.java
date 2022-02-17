package ru.abondin.hreasy.platform.service.notification.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationChannelHandler;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Send notification one by one to channels.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SynchronousNotificationSender implements NotificationSender {

    @Autowired(required = false)
    private NotificationChannelHandler.NotificationPersistChannelHandler persistChannelHandler;

    @Autowired(required = false)
    private NotificationChannelHandler.NotificationEmailChannelHandler emailChannelHandler;

    private final List<NotificationChannelHandler> handlers = new ArrayList<>();

    @PostConstruct
    private void postConstruct() {
        this.handlers.addAll(Stream.of(persistChannelHandler, emailChannelHandler)
                .filter(h -> h != null).collect(Collectors.toList()));
    }

    @Override
    public Flux<NotificationChannelHandler.NotificationHandleResult> send(NewNotificationDto newNotificationDto, NotificationChannelHandler.Route route) {
        Flux<NotificationChannelHandler.NotificationHandleResult> job = null;
        for (var h : handlers.stream().sorted(Comparator.comparing(NotificationChannelHandler::channelId)).collect(Collectors.toList())) {
            if (!newNotificationDto.getDeliveryChannels().contains(h.channelId())) {
                continue;
            }
            var j = h.handleNotification(newNotificationDto, route);
            if (job == null) {
                job = j;
            } else {
                job = job.concatWith(j);
            }
        }
        return job.doOnNext(result -> {
            log.info("Notification handled {}", result);
        });
    }
}
