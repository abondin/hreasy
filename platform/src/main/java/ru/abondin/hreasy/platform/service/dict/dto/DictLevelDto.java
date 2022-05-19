package ru.abondin.hreasy.platform.service.dict.dto;

import lombok.Data;

@Data
public class DictLevelDto {
    private int id;
    private String name;
    private Integer weight;
    private boolean archived = false;

}
