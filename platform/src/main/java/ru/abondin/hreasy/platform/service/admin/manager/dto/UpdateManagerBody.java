package ru.abondin.hreasy.platform.service.admin.manager.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * Update manager link
 */
@Data
@ToString(of = {"responsibilityObjectType", "responsibilityObjectId"})
public class UpdateManagerBody {
    private String responsibilityObjectType;
    private int responsibilityObjectId;
    private String responsibilityType;
    @Nullable
    private String comment;
}
