package ru.abondin.hreasy.notifyms.repo;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("notification")
public class NotificationEntry {
    @Id
    private Long id;
    private String eventType;
    private String recipientType;
    private String recipientLogin;
    private String recipientChatId;
    private Integer employeeId;
    private String dedupeKey;
    private String priority;
    private String locale;
    private String title;
    private String body;
    private Json data;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
