package ru.abondin.hreasy.platform.service.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Runs the common platform notification flow for business events.
 * <p>
 * Event-specific handlers define what should be sent. This orchestrator owns the common mechanics:
 * template rendering, UI inbox persistence, and best-effort delivery publication.
 */
@Service
public class NotificationOrchestrator {
    private final Map<Class<?>, BusinessNotificationHandler<?>> handlers;
    private final NotificationPersistService notificationPersistService;
    private final NotifyMsClient notifyMsClient;
    private final NotificationLocaleResolver localeResolver;
    private final ObjectMapper objectMapper;
    private final I18Helper i18n;

    public NotificationOrchestrator(List<BusinessNotificationHandler<?>> handlers,
                                    NotificationPersistService notificationPersistService,
                                    NotifyMsClient notifyMsClient,
                                    NotificationLocaleResolver localeResolver,
                                    ObjectMapper objectMapper,
                                    I18Helper i18n) {
        this.handlers = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(BusinessNotificationHandler::eventClass, Function.identity()));
        this.notificationPersistService = notificationPersistService;
        this.notifyMsClient = notifyMsClient;
        this.localeResolver = localeResolver;
        this.objectMapper = objectMapper;
        this.i18n = i18n;
    }

    /**
     * Publishes notifications produced by the handler registered for the event class.
     *
     * @param event business event that should produce one or more notifications
     * @return completion signal after UI persistence and best-effort external publication
     */
    public Mono<Void> publish(BusinessNotificationEvent event) {
        var handler = handler(event);
        return handler.build(event)
                .flatMap(this::publish)
                .then();
    }

    @SuppressWarnings("unchecked")
    private BusinessNotificationHandler<BusinessNotificationEvent> handler(BusinessNotificationEvent event) {
        var handler = handlers.get(event.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported notification event: " + event.getClass().getName());
        }
        return (BusinessNotificationHandler<BusinessNotificationEvent>) handler;
    }

    private Mono<Void> publish(NotificationPlan plan) {
        var locale = plan.getLocale() == null ? localeResolver.resolve(plan.getRecipient()) : plan.getLocale();
        var title = render(locale, plan.getTitleKey(), plan.getTitleArgs());
        var body = render(locale, plan.getBodyKey(), plan.getBodyArgs());
        var context = serializeContext(plan);

        return persistInbox(plan, title, body, context)
                .then(notifyMsClient.sendBestEffort(notifyMsRequest(plan, locale, title, body, context)));
    }

    private Mono<Void> persistInbox(NotificationPlan plan, String title, String body, String context) {
        var recipient = plan.getRecipient();
        if (recipient.employeeId() == null) {
            return Mono.empty();
        }
        var notification = new NewNotificationDto();
        notification.setClientUuid(deterministicUuid(plan.getDedupeKey()));
        notification.setCategory(plan.getCategory());
        notification.setTitle(title);
        notification.setMarkdownText(body);
        notification.setContext(context);
        return notificationPersistService.sendNotificationTo(notification, List.of(recipient.employeeId()),
                        plan.getInitiatorEmployeeId())
                .then();
    }

    private NotifyMsClient.CreateNotificationRequest notifyMsRequest(NotificationPlan plan,
                                                                     java.util.Locale locale,
                                                                     String title,
                                                                     String body,
                                                                     String context) {
        var recipient = plan.getRecipient();
        return new NotifyMsClient.CreateNotificationRequest(
                plan.getEventType(),
                new NotifyMsClient.Recipient(recipient.type(), recipient.login(), recipient.chatId(), recipient.employeeId()),
                plan.getPriority(),
                plan.getDedupeKey(),
                locale.toLanguageTag(),
                title,
                body,
                context
        );
    }

    private String render(java.util.Locale locale, String key, List<Object> args) {
        var localizedArgs = args.stream()
                .map(arg -> renderArg(locale, arg))
                .toArray();
        return i18n.localize(locale, key, localizedArgs);
    }

    private Object renderArg(java.util.Locale locale, Object arg) {
        if (arg instanceof LocalDate date) {
            return i18n.formatDate(locale, date);
        }
        return arg;
    }

    private String serializeContext(NotificationPlan plan) {
        try {
            return objectMapper.writeValueAsString(plan.getContext());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize notification context for " + plan.getEventType(), ex);
        }
    }

    private String deterministicUuid(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
