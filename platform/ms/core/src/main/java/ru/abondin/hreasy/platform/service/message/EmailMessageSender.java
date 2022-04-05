package ru.abondin.hreasy.platform.service.message;

import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.service.message.dto.HrEasyEmailMessage;

/**
 * Send message via email
 */
public interface EmailMessageSender {

    Mono<String> sendMessage(HrEasyEmailMessage message);

}
