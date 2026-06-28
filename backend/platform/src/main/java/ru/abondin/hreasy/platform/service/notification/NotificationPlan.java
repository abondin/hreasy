package ru.abondin.hreasy.platform.service.notification;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Channel-independent notification description produced by a business handler.
 */
@Builder
@Getter
public class NotificationPlan {
    private final String eventType;
    private final String category;
    private final String dedupeKey;
    private final NotificationRecipient recipient;
    private final Locale locale;
    private final String priority;
    private final String titleKey;
    @Singular
    private final List<Object> titleArgs;
    private final String bodyKey;
    @Singular
    private final List<Object> bodyArgs;
    private final String actionTitleKey;
    private final String actionUrl;
    @Singular("context")
    private final Map<String, Object> context;
    private final Integer initiatorEmployeeId;
}
