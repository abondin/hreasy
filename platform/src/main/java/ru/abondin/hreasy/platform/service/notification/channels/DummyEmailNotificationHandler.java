package ru.abondin.hreasy.platform.service.notification.channels;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.time.OffsetDateTime;
import java.util.List;

@ConditionalOnExpression("T(org.springframework.util.StringUtils).isEmpty('${spring.mail.host:}')")
@Service
@Slf4j
public class DummyEmailNotificationHandler implements NotificationEmailChannelHandler {
    @Override
    public Flux<NotificationEmailHandleResult> handleNotification(NewNotificationDto newNotificationDto, List<String> emails) {
        return Flux.fromIterable(emails).map(e -> NotificationEmailHandleResult.builder()
                .email(e)
                .notificationClientId(newNotificationDto.getClientUuid())
                .handledAt(OffsetDateTime.now())
                .build()
        ).doOnNext((result) -> {
            log.info("Dummy email sent. Result: {}", result);
        });
    }
}
