package ru.abondin.hreasy.notifyms.repo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("notification_delivery")
public class NotificationDeliveryEntry {
    @Id
    private Long id;
    private Long notificationId;
    private String channel;
    private String status;
    private OffsetDateTime dueAt;
    private String digestKey;
    private int attemptCount;
    private int errorCount;
    private int maxAttempts;
    private OffsetDateTime lastSuccessAt;
    private OffsetDateTime lastAttemptAt;
    private OffsetDateTime nextAttemptAt;
    private String externalMessageId;
    private String providerPayloadId;
    private String providerStatusCode;
    private String lastErrorCode;
    private String lastErrorMessage;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
