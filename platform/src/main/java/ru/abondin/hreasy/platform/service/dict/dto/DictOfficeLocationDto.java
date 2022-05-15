package ru.abondin.hreasy.platform.service.dict.dto;

import lombok.Data;

@Data
public class DictOfficeLocationDto {
    private int id;
    private String name;
    private String description;
    private String office;
    private boolean archived = false;

}
