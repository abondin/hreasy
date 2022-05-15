package ru.abondin.hreasy.platform.service.dict.dto;

import lombok.Data;

@Data
public class DictPositionDto {
    private int id;
    private String name;
    private String category;
    private boolean archived = false;

}
