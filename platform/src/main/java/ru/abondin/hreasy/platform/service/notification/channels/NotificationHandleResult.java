package ru.abondin.hreasy.platform.service.notification.channels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public abstract class NotificationHandleResult {
    private final String notificationClientId;
    private final OffsetDateTime handledAt;
    private final int channelId;
}
