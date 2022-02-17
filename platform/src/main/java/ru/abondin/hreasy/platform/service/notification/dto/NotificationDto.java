package ru.abondin.hreasy.platform.service.notification.dto;

import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Show notification to employee
 */
@Data
public class NotificationDto {
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
    private OffsetDateTime createdAt;
    private OffsetDateTime acknowledgedAt;
    private OffsetDateTime archivedAt;
}
