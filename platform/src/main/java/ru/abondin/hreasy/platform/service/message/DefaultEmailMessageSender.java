package ru.abondin.hreasy.platform.service.message;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.service.message.dto.HrEasyEmailMessage;

/**
 * Send email using smtp
 */
@Service
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${spring.mail.host:}')")
@RequiredArgsConstructor
@Slf4j
public class DefaultEmailMessageSender implements EmailMessageSender {
    private final JavaMailSender emailSender;


    @Override
    public Mono<String> sendMessage(HrEasyEmailMessage message) {
        log.info("Send email message {}", message);
        return validateMessage(message)
                .then(buildMimeMessage(message))
                .flatMap(email -> Mono.defer(() -> {
                    emailSender.send(email);
                    return Mono.just(message.getClientUuid());
                }));
    }

    private Mono<Boolean> validateMessage(HrEasyEmailMessage message) {
        BusinessError error = null;
        if (StringUtils.isBlank(message.getClientUuid())) {
            error = new BusinessError("errors.validation.failed.null", "сlientUuid");
        }
        if (StringUtils.isBlank(message.getFrom())) {
            error = new BusinessError("errors.validation.failed.null", "from");
        }
        if (message.getTo() == null || !message.getTo().stream().filter(m -> !StringUtils.isBlank(m)).findFirst().isPresent()) {
            error = new BusinessError("errors.validation.failed.null", "to");
        }
        if (StringUtils.isBlank(message.getTitle())) {
            error = new BusinessError("errors.validation.failed.null", "title");
        }
        if (StringUtils.isBlank(message.getBody())) {
            error = new BusinessError("errors.validation.failed.null", "body");
        }
        if (error != null) {
            log.error("Email message validate fail {}, {}", error.getCode(), error.getLocalizationArgs());
            return Mono.error(error);
        }
        return Mono.just(true);
    }

    private Mono<MimeMessage> buildMimeMessage(HrEasyEmailMessage hrEasyEmailMessage) {
        try {
            var message = emailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            for (var address : hrEasyEmailMessage.getTo()) {
                helper.addTo(address);
            }
            for (var address : hrEasyEmailMessage.getCc()) {
                helper.addCc(address);
            }
            helper.setFrom(hrEasyEmailMessage.getFrom());
            helper.setSubject(hrEasyEmailMessage.getTitle());
            helper.setText(hrEasyEmailMessage.getBody(), true);
            for (var attachmentEntry : hrEasyEmailMessage.getAttachments().entrySet()) {
                helper.addAttachment(attachmentEntry.getKey(),
                        attachmentEntry.getValue());
            }
            return Mono.just(message);
        } catch (MessagingException ex) {
            log.error("Unable to build mime message", ex);
            return Mono.error(ex);
        }
    }
}
