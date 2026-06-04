package ru.abondin.hreasy.platform.service.admin.dict.dto;

import lombok.Data;
import lombok.ToString;

/**
 *
 */
@Data
@ToString
public class CreateOrUpdateOrganizationBody {
    private String name;
    private String description;
    private boolean archived = false;
}
