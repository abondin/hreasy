package ru.abondin.hreasy.platform.service.admin.dict.dto;

import lombok.Data;

/**
 * Office location
 */
@Data
public class CreateOrUpdateOfficeLocationBody {
    private String name;
    private Integer officeId;
    private String description;
    private String mapName;
    private boolean archived = false;

}
