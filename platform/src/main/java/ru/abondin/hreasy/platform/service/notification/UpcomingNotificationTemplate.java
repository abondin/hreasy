package ru.abondin.hreasy.platform.service.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.repo.vacation.VacationEntry;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.util.Arrays;
import java.util.UUID;

import static ru.abondin.hreasy.platform.service.notification.sender.NotificationSender.NOTIFICATION_DELIVERY_CHANNEL_EMAIL;
import static ru.abondin.hreasy.platform.service.notification.sender.NotificationSender.NOTIFICATION_DELIVERY_CHANNEL_PERSIST;

/**
 * Fill all required fields for upcoming vacation notification
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UpcomingNotificationTemplate {

    public static final String UPCOMING_NOTIFICATION_CATEGORY = "upcoming-notification";

    private final I18Helper i18n;
    private final ObjectMapper objectMapper;

    public NewNotificationDto create(VacationEntry vacationEntry) {
        var context = new UpcomingNotificationContext();
        var notification = new NewNotificationDto();
        notification.setTitle(i18n.localize("notification.template.upcoming-vacation.title"));
        notification.setMarkdownText("### Upcoming vacation soon. Do not forgot to buy swimming trunks");
        notification.setCategory(UPCOMING_NOTIFICATION_CATEGORY);
        notification.setClientUuid(UUID.randomUUID().toString());
        notification.setDeliveryChannels(Arrays.asList(NOTIFICATION_DELIVERY_CHANNEL_PERSIST, NOTIFICATION_DELIVERY_CHANNEL_EMAIL));
        try {
            notification.setContext(objectMapper.writeValueAsString(context));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize UpcomingNotificationContext", e);
        }
        return notification;
    }


    @Data
    public static class UpcomingNotificationContext {
        private int vacationId;
    }

}
