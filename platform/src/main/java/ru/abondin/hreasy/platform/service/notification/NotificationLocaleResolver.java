package ru.abondin.hreasy.platform.service.notification;

import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Resolves the locale used to render notification templates for a recipient.
 */
@Service
public class NotificationLocaleResolver {
    private static final Locale DEFAULT_NOTIFICATION_LOCALE = Locale.of("ru", "RU");

    /**
     * Resolves recipient locale.
     *
     * @param recipient notification recipient
     * @return locale for template rendering
     */
    public Locale resolve(NotificationRecipient recipient) {
        return DEFAULT_NOTIFICATION_LOCALE;
    }
}
