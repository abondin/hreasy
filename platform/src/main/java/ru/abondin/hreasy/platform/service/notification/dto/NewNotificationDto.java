package ru.abondin.hreasy.platform.service.notification.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Register new notification in the system
 */
@Data
@ToString(of = {"clientUuid", "title", "deliveryChannels"})
public class NewNotificationDto {
    /**
     * Unique id of the notification for given employee
     */
    @NotNull
    private String clientUuid;
    /**
     * Show text to show notification title
     */
    @NotNull
    private String title;
    /**
     * Notification category
     */
    private String category;
    /**
     * Text in markdown
     */
    @NotNull
    private String markdownText;
    /**
     * JSON context to advanced frontend behavior
     */
    private String context;

    /**
     * 0 - database / web ui (show notification to the employee in ui)
     * 1 - email - send notification as an email
     */
    @NotNull
    private List<Integer> deliveryChannels = new ArrayList<>();

}
