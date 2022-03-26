package ru.abondin.hreasy.platform.service.notification.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationEmailChannelHandler;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationHandleResult;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationPersistChannelHandler;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationRoute;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

/**
 * Send notification one by one to channels.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SynchronousNotificationSender implements NotificationSender {

    private final NotificationPersistChannelHandler persistChannelHandler;

    private final NotificationEmailChannelHandler emailChannelHandler;

    private final EmployeeRepo employeeRepo;

    @Override
    public Flux<NotificationHandleResult> send(NewNotificationDto newNotificationDto, NotificationRoute route) {
        Flux<NotificationHandleResult> job = Flux.empty();
        if (newNotificationDto.getDeliveryChannels().contains(NOTIFICATION_DELIVERY_CHANNEL_PERSIST)) {
            job = job.concatWith(persistChannelHandler.handleNotification(newNotificationDto, route));
        }
        if (newNotificationDto.getDeliveryChannels().contains(NOTIFICATION_DELIVERY_CHANNEL_EMAIL)) {
            job = job.concatWith(handleEmail(newNotificationDto, route));
        }
        return job.doOnNext(result -> {
            log.info("Notification handled {}", result);
        });
    }

    private Flux<NotificationEmailChannelHandler.NotificationEmailHandleResult> handleEmail(NewNotificationDto newNotificationDto, NotificationRoute route) {
        return employeeRepo.findAllById(route.getDestinationEmployeeIds())
                .filter(e -> Strings.isNotBlank(e.getEmail()))
                .map(EmployeeEntry::getEmail)
                .collectList()
                .flatMapMany(emails -> emailChannelHandler.handleNotification(newNotificationDto, emails));
    }


}
