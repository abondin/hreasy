package ru.abondin.hreasy.platform.service.dict.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

@Data
public class DictOfficeDto {
    private int id;
    private String name;
    private String description;
    private String address;
    private String mapName;
    private boolean archived = false;

}
