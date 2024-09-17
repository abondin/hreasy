package ru.abondin.hreasy.platform.service.dict.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

@Data
public class DictOfficeWorkplaceDto {
    private int id;
    private String name;
    private String description;
    private SimpleDictDto office;
    private SimpleDictDto officeLocation;
    private Integer mapX;
    private Integer mapY;
    private boolean archived = false;

}
