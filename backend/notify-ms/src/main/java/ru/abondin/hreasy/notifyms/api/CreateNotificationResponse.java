package ru.abondin.hreasy.notifyms.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateNotificationResponse {
    private Long notificationId;
    private String status;
}
