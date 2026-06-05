package ru.abondin.hreasy.platform.service.admin.manager.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * Update manager link
 */
@Data
public class UpdateManagerBody {
    private String responsibilityType;
    @Nullable
    private String comment;
}
