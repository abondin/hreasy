package ru.abondin.hreasy.platform.service.support.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class NewSupportRequestDto {

    public static int SOURCE_TYPE_TELEGRAM = 1;

    @NotNull
    private String group;
    @Nullable
    private String category;
    @NotNull
    private String message;
}
