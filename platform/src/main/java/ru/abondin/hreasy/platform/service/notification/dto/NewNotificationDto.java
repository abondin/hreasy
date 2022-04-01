package ru.abondin.hreasy.platform.service.notification.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Create new notification
 */
@Data
public class NewNotificationDto {
    @NotNull
    private String clientUuid;
    /**
     * Show text to show notification title
     */
    private String title;
    /**
     * Notification category
     */
    private String category;
    /**
     * Text in markdown
     */
    private String markdownText;
    /**
     * JSON context to advanced frontend behavior
     */
    private String context;
}
