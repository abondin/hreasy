package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.dict.dto.DictOfficeWorkplaceDto;

/**
 * Simple dict for workplace with link to office location
 */
@Data
@NoArgsConstructor
public class OfficeWorkplaceDictDto extends SimpleDictDto { @Nullable
    private Integer officeLocationId;
    /**
     * @see DictOfficeWorkplaceDto#getType()
     */
    private short type;
}
