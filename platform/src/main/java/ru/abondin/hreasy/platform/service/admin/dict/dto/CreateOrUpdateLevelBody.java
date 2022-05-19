package ru.abondin.hreasy.platform.service.admin.dict.dto;

import lombok.Data;

/**
 *
 */
@Data
public class CreateOrUpdateLevelBody {
    private String name;
    private Integer weight;
    private boolean archived = false;
}
