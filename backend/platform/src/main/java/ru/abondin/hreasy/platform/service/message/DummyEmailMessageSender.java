package ru.abondin.hreasy.platform.service.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.service.message.dto.HrEasyEmailMessage;

import jakarta.annotation.PostConstruct;

/**
 * Send email using smtp
 */
@ConditionalOnExpression("T(org.springframework.util.StringUtils).isEmpty('${spring.mail.host:}')")
@Service
@RequiredArgsConstructor
@Slf4j
public class DummyEmailMessageSender implements EmailMessageSender {

    @PostConstruct
    protected void postConstruct() {
        log.warn("---------------------- spring.mail.host not specified. Dummy Email Sender enabled");
    }

    @Override
    public Mono<String> sendMessage(HrEasyEmailMessage message) {
        log.info("Send message to dummy channel {}:\n{}", message, message.getBody());
        return Mono.just(message.getClientUuid());
    }
}
