package ru.abondin.hreasy.platform.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.notification.NotificationEntry;
import ru.abondin.hreasy.platform.repo.notification.NotificationRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.notification.dto.NotificationDto;
import ru.abondin.hreasy.platform.service.notification.dto.NotificationMapper;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepo repo;
    private final NotificationMapper mapper;
    private final DateTimeService dateTimeService;

    /**
     * @param auth
     * @return
     */
    public Flux<NotificationDto> myNotifications(AuthContext auth) {
        return repo.findByEmployee(auth.getEmployeeInfo().getEmployeeId()).map(mapper::reportToDto);
    }

    public Mono<Integer> acknowledge(AuthContext auth, int notificationId) {
        log.info("Acknowledge notification {} by {}", notificationId, auth.getUsername());
        var now = dateTimeService.now();
        return doUpdate(auth, notificationId, (notificationEntry) -> {
            notificationEntry.setAcknowledgedAt(now);
            notificationEntry.setAcknowledgedBy(auth.getEmployeeInfo().getEmployeeId());
        });
    }

    public Mono<Integer> archive(AuthContext auth, int notificationId) {
        log.info("Archive notification {} by {}", notificationId, auth.getUsername());
        var now = dateTimeService.now();
        return doUpdate(auth, notificationId, (notificationEntry) -> {
            notificationEntry.setArchivedAt(now);
            notificationEntry.setArchivedBy(auth.getEmployeeInfo().getEmployeeId());
        });
    }


    private Mono<Integer> doUpdate(AuthContext auth, int notificationId, Consumer<NotificationEntry> consumer) {
        return repo.findById(notificationId)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(notificationId))))
                .flatMap(notificationEntry -> {
                    if (notificationEntry.getEmployee() != auth.getEmployeeInfo().getEmployeeId()) {
                        return Mono.error(new BusinessError("errors.entity.invalid.parent",
                                "Notification:" + notificationId,
                                "Employee:" + auth.getEmail()));
                    }
                    consumer.accept(notificationEntry);
                    return repo.save(notificationEntry).map(NotificationEntry::getId);
                });
    }

}
