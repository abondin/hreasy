package ru.abondin.hreasy.platform.service.notification.channels;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.util.List;

/**
 * Send email using smtp
 */
@Service
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${spring.mail.host:}')")
@RequiredArgsConstructor
@Slf4j
public class DefaultNotificationEmailChannelHandler implements NotificationEmailChannelHandler {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.host}")
    private String emailHost;

    @Override
    public Flux<NotificationEmailHandleResult> handleNotification(NewNotificationDto newNotificationDto, List<String> emails) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
