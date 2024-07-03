package ru.abondin.hreasy.platform.service.support.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewSupportRequestDto {
    @NotNull
    private String group;
    @NotNull
    private String message;
}
