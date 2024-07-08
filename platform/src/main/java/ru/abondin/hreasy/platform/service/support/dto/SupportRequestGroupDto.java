
package ru.abondin.hreasy.platform.service.support.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class SupportRequestGroupDto {
    @NotNull
    private SupportGroupConfiguration configuration;
    @NotNull
    private String key;
    @NotNull
    private String displayName;
    @Nullable
    private String description;
}
