package ru.abondin.hreasy.platform.repo.notification;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Employee notification
 */
@Data
@Table("notify.notification")
public class NotificationEntry {
    @Id
    private int id;

    private int employee;
    
    private String clientUuid;

    private String category;

    private String title;

    private String markdownText;

    private Json context;

    private OffsetDateTime createdAt;

    private Integer createdBy;

    /**
     * 0 - database / web ui (show notification to the employee in ui)
     * 1 - email - send notification as an email
     */
    private List<Integer> deliveryChannels = new ArrayList<>();

    private OffsetDateTime acknowledgedAt;

    private Integer acknowledgedBy;

    private OffsetDateTime archivedAt;

    private Integer archivedBy;
}
