package ru.abondin.hreasy.platform.service.dict.dto;

import lombok.Data;

@Data
public class DictOrganizationDto {
    private int id;
    private String name;
    private String description;
    private boolean archived = false;

}
