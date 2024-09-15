package ru.abondin.hreasy.platform.service.admin.dict.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.PositionOnMap;

/**
 * Office location
 */
@Data
public class CreateOrUpdateWorkplaceBody {
    private String name;
    private Integer officeLocationId;
    private String description;
    private PositionOnMap position;
    private boolean archived = false;

}
