package ru.abondin.hreasy.platform.tg.dto;


import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class TelegramConfirmationRequest {
    private final OffsetDateTime createdAt;
    private final String confirmationCode;
    private final String telegramAccount;
    private final int employeeId;
}
