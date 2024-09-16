package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * Simple dict for project
 */
@Data
@NoArgsConstructor
public class OfficeLocationDictDto extends SimpleDictDto { @Nullable
    private Integer officeId;
}
