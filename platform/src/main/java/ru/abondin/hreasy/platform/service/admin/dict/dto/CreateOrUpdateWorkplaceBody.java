package ru.abondin.hreasy.platform.service.admin.dict.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dict.dto.DictOfficeWorkplaceDto;

/**
 * Office location
 */
@Data
public class CreateOrUpdateWorkplaceBody {
    private String name;
    private String description;
    /**
     * @see DictOfficeWorkplaceDto#getType()
     */
    private short type;

    private Integer mapX;
    private Integer mapY;
    private boolean archived = false;

}
