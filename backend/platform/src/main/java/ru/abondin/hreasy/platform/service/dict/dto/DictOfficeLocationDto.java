package ru.abondin.hreasy.platform.service.dict.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

@Data
public class DictOfficeLocationDto {
    private int id;
    private String name;
    private String description;
    private SimpleDictDto office;
    private boolean archived = false;
    private String mapName;
}
