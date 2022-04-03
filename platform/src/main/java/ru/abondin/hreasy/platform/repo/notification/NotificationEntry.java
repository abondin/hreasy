package ru.abondin.hreasy.platform.repo.notification;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

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

    private OffsetDateTime acknowledgedAt;

    private Integer acknowledgedBy;

    private OffsetDateTime archivedAt;

    private Integer archivedBy;
}
