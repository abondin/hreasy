package ru.abondin.hreasy.platform.service.message.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "body")
public abstract class HrEasyMessage {
    private String clientUuid;
    private String title;
    private String body;
}
