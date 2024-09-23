package ru.abondin.hreasy.platform.service.admin.dict.dto;

import lombok.Data;

/**
 * Office location
 */
@Data
public class CreateOrUpdateWorkplaceBody {
    private String name;
    private String description;
    private Integer mapX;
    private Integer mapY;
    private boolean archived = false;

}
