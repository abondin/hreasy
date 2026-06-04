package ru.abondin.hreasy.notifyms.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNotificationRequest {
    @NotBlank
    private String eventType;
    @NotNull
    @Valid
    private Recipient recipient;
    private String priority = "normal";
    @NotBlank
    private String dedupeKey;
    private String locale = "ru";
    private String title;
    @NotBlank
    private String body;
    private String data;

    @Data
    public static class Recipient {
        @NotBlank
        private String type;
        private String login;
        private String chatId;
        private Integer employeeId;
    }
}
