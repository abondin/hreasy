package ru.abondin.hreasy.platform.service.admin.dict.dto;

import lombok.Data;

/**
 * Office
 */
@Data
public class CreateOrUpdateOfficeBody {
    private String name;
    private String address;
    private String description;
    private String mapName;
    private boolean archived = false;

}
