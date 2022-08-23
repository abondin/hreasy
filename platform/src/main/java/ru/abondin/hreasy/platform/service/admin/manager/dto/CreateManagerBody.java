package ru.abondin.hreasy.platform.service.admin.manager.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;

/**
 * Create new manager link
 */
@Data
@ToString(of = {"employee", "responsibilityObjectType", "responsibilityObjectId"})
public class CreateManagerBody {
    private int employee;
    private String responsibilityObjectType;
    private int responsibilityObjectId;
    private String responsibilityType;
    @Nullable
    private String comment;
}
