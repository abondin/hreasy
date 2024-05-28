package ru.abondin.hreasy.platform.tg;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;
import ru.abondin.hreasy.platform.service.message.EmailMessageSender;
import ru.abondin.hreasy.platform.service.message.dto.HrEasyEmailMessage;
import ru.abondin.hreasy.platform.tg.dto.TelegramConfirmationRequest;

import java.time.Duration;
import java.util.*;

/**
 * Service for generating and sending unique confirmation links for Telegram account verification.
 *
 * <p>This service handles the following tasks:
 * <ul>
 *   <li>Generating a unique UUID based on provided employeeId and telegramName.</li>
 *   <li>Forming a confirmation link containing the UUID.</li>
 *   <li>Sending the confirmation link to the user's email based on employeeId.</li>
 *   <li>Ensuring the confirmation link is valid for only 10 minutes.</li>
 *   <li>Enforcing a minimum interval of 3 minutes between link generation requests for the same employeeId and telegramName.</li>
 * </ul>
 */
@Service
@Slf4j
@AllArgsConstructor
public class TelegramBotConfirmService {
    private final AdminEmployeeService employeeService;
    private final EmailMessageSender emailMessageSender;
    private final DateTimeService dateTimeService;
    private final HrEasyCommonProps props;
    private final I18Helper i18n;
    private final TemplateEngine templateEngine;
    private final Map<String, TelegramConfirmationRequest> generatedLinksCache = Collections.synchronizedMap(new HashMap<>());

    private static final String CONFIRMATION_URL = "telegram/confirm/";

    private static String getRequestKey(AuthContext auth) {
        return auth.getUsername() + ":" + auth.getEmployeeInfo().getTelegramAccount();
    }


    public Mono<String> sendConfirmationLink(AuthContext auth) {
        log.info("New telegram account confirmation is requested by {}:{}", auth.getUsername(), auth.getEmployeeInfo().getTelegramAccount());
        var now = dateTimeService.now();
        var key = getRequestKey(auth);
        var confirmationCode = UUID.randomUUID().toString();
        synchronized (generatedLinksCache) {
            var lastLinkGenerated = generatedLinksCache.get(key);
            if (lastLinkGenerated != null &&
                    Duration.between(lastLinkGenerated.getCreatedAt(), now)
                            .compareTo(props.getTelegramConfirmation().getLinkGenerationInterval()) < 0) {
                return Mono.error(new BusinessError("errors.telegram_confirmation.prev_link_active", String.valueOf(props.getTelegramConfirmation().getLinkGenerationInterval().toMinutes())));
            }
            generatedLinksCache.put(key, TelegramConfirmationRequest.builder().createdAt(now).confirmationCode(confirmationCode).build());
        }
        var mail = buildMail(auth, confirmationCode);
        return emailMessageSender.sendMessage(mail);
    }


    public Mono<Integer> confirm(AuthContext auth, String confirmationCode) {
        synchronized (generatedLinksCache) {
            var expectedLink = generatedLinksCache.remove(getRequestKey(auth));
            if (expectedLink == null || !expectedLink.getConfirmationCode().equals(confirmationCode)) {
                return Mono.error(new BusinessError("errors.telegram_confirmation.invalid_code"));
            }
        }
        return employeeService.confirmTelegramBotAccount(auth, auth.getEmployeeInfo().getEmployeeId(), auth.getEmployeeInfo().getTelegramAccount());
    }

    private HrEasyEmailMessage buildMail(AuthContext auth, String key) {
        var mail = new HrEasyEmailMessage();
        mail.setTo(Arrays.asList(auth.getEmail()));
        mail.setFrom(props.getDefaultEmailFrom());
        mail.setTitle(i18n.localize("telegram_confirmation.email.title"));
        mail.setClientUuid(UUID.randomUUID().toString());
        var bodyContext = new Context();
        bodyContext.setVariable("url", props.getWebBaseUrl().resolve(CONFIRMATION_URL + key));
        bodyContext.setVariable("accountName", auth.getEmployeeInfo().getTelegramAccount());
        mail.setBody(templateEngine.process("telegramconfirmation.html", bodyContext));
        return mail;
    }


}
