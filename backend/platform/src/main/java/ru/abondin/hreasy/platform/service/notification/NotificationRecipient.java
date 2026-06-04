package ru.abondin.hreasy.platform.service.notification;

/**
 * Normalized recipient used by platform UI notifications and the delivery microservice.
 *
 * @param type recipient type accepted by notify-ms, for example user or chat
 * @param login user login for private messenger delivery
 * @param chatId chat identifier for group messenger delivery
 * @param employeeId HR Easy employee id used for UI inbox persistence
 */
public record NotificationRecipient(
        String type,
        String login,
        String chatId,
        Integer employeeId
) {
    /**
     * Creates a private user recipient.
     *
     * @param login corporate login used by external messenger delivery
     * @param employeeId HR Easy employee id used by the UI inbox
     * @return user recipient
     */
    public static NotificationRecipient user(String login, Integer employeeId) {
        return new NotificationRecipient("user", login, null, employeeId);
    }
}
