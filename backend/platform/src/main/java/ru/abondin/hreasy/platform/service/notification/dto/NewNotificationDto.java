package ru.abondin.hreasy.platform.service.notification.dto;

import lombok.Data;

import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

/**
 * Create new notification
 */
@Data
@NoArgsConstructor
public class NewNotificationDto {
    @NonNull
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
