package ru.abondin.hreasy.platform.service.admin.dict.dto;

import lombok.Data;

/**
 * Office location
 */
@Data
public class CreateOrUpdateOfficeLocationBody {
    private String name;
    private String description;
    private boolean archived = false;

}
