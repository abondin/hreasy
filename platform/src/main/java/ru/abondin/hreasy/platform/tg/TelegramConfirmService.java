package ru.abondin.hreasy.platform.tg;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeHistoryRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeAllFieldsMapper;
import ru.abondin.hreasy.platform.service.message.EmailMessageSender;
import ru.abondin.hreasy.platform.service.message.dto.HrEasyEmailMessage;
import ru.abondin.hreasy.platform.tg.dto.TelegramConfirmationRequest;

import java.time.Duration;
import java.time.OffsetDateTime;
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
public class TelegramConfirmService {
    private final EmployeeWithAllDetailsRepo employeeRepo;
    private final EmployeeHistoryRepo historyRepo;
    private final EmployeeAllFieldsMapper mapper;
    private final EmailMessageSender emailMessageSender;
    private final DateTimeService dateTimeService;
    private final HrEasyCommonProps props;
    private final I18Helper i18n;
    private final TemplateEngine templateEngine;
    private final Map<String, TelegramConfirmationRequest> generatedLinksCache = Collections.synchronizedMap(new HashMap<>());

    private static final String CONFIRMATION_URL = "/telegram/confirm";

    private static String getRequestKey(AuthContext auth) {
        return getRequestKey(auth.getEmployeeInfo().getEmployeeId(), auth.getEmployeeInfo().getTelegramAccount());
    }

    private static String getRequestKey(int employeeId, String telegramAccount) {
        return employeeId + ":" + telegramAccount;
    }


    /**
     * Must be executed from telegram bot
     *
     * @param auth
     * @return
     */
    public Mono<String> sendConfirmationLink(AuthContext auth) {
        log.info("New telegram account confirmation is requested by {}:{}", auth.getUsername(), auth.getEmployeeInfo().getTelegramAccount());
        if (auth.getEmployeeInfo().getLoggedInType() != AuthContext.LoginType.TELEGRAM_BOT_SERVICE.getValue()) {
            return Mono.error(new AccessDeniedException("Only telegram bot service can send confirmation link"));
        }
        var now = dateTimeService.now();
        var key = getRequestKey(auth);
        var confirmationRequest = TelegramConfirmationRequest
                .builder()
                .createdAt(now)
                .telegramAccount(auth.getEmployeeInfo().getTelegramAccount())
                .employeeId(auth.getEmployeeInfo().getEmployeeId())
                .confirmationCode(UUID.randomUUID().toString()).build();
        synchronized (generatedLinksCache) {
            var lastLinkGenerated = generatedLinksCache.get(key);
            if (lastLinkGenerated != null &&
                    Duration.between(lastLinkGenerated.getCreatedAt(), now)
                            .compareTo(props.getTelegramConfirmation().getLinkGenerationInterval()) < 0) {
                return Mono.error(new BusinessError("errors.telegram_confirmation.prev_link_active", String.valueOf(props.getTelegramConfirmation().getLinkGenerationInterval().toMinutes())));
            }
            log.info("Putting new telegram confirmation request {} - {}", key, confirmationRequest.getConfirmationCode());
            generatedLinksCache.put(key, confirmationRequest);
        }
        var mail = buildMail(auth, confirmationRequest);
        return emailMessageSender.sendMessage(mail);
    }


    @Transactional
    public Mono<Integer> confirm(int employeeId, String telegramAccount, String confirmationCode) {
        log.info("Complete telegram account {} for employee {} by the code {}", employeeId, telegramAccount, confirmationCode);
        var now = dateTimeService.now();
        synchronized (generatedLinksCache) {
            var expectedLink = generatedLinksCache.remove(getRequestKey(employeeId, telegramAccount));
            if (expectedLink == null || !expectedLink.getConfirmationCode().equals(confirmationCode)) {
                return Mono.error(new BusinessError("errors.telegram_confirmation.invalid_code"));
            }
            if (Duration.between(expectedLink.getCreatedAt(), now)
                    .compareTo(props.getTelegramConfirmation().getLinkedExpirationDuration()) > 0) {
                return Mono.error(new BusinessError("errors.telegram_confirmation.expired"));
            }
        }
        return employeeRepo.findById(employeeId)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(employeeId))))
                .flatMap(entry -> {
                    entry.setTelegramConfirmedAt(now);
                    return doUpdate(employeeId, now, entry, null);
                });
    }

    private HrEasyEmailMessage buildMail(AuthContext auth, TelegramConfirmationRequest confirmationRequest) {
        var mail = new HrEasyEmailMessage();
        mail.setTo(Arrays.asList(auth.getEmail()));
        mail.setFrom(props.getDefaultEmailFrom());
        mail.setTitle(i18n.localize("telegram_confirmation.email.title"));
        mail.setClientUuid(UUID.randomUUID().toString());
        var bodyContext = new Context();
        var linkUrl = UriComponentsBuilder.fromUri(props.getWebBaseUrl())
                .path(CONFIRMATION_URL)
                .pathSegment(String.valueOf(confirmationRequest.getEmployeeId())
                        , confirmationRequest.getTelegramAccount()
                        , confirmationRequest.getConfirmationCode())
                .encode()
                .build();
        bodyContext.setVariable("url", linkUrl.toUriString());
        bodyContext.setVariable("accountName", auth.getEmployeeInfo().getTelegramAccount());
        mail.setBody(templateEngine.process("telegramconfirmation.html", bodyContext));
        return mail;
    }


    private Mono<Integer> doUpdate(int currentEmployeeId, OffsetDateTime now, EmployeeWithAllDetailsEntry entry, @Nullable Integer importProcessId) {
        return employeeRepo.save(entry).flatMap(persisted -> {
            var history = mapper.history(persisted, currentEmployeeId, now);
            history.setImportProcess(importProcessId);
            return historyRepo.save(history).map(e -> persisted.getId());
        });
    }


}
