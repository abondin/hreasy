package ru.abondin.hreasy.platform.service.admin.dict.dto;

import lombok.Data;
import lombok.ToString;

/**
 *
 */
@Data
@ToString
public class CreateOrUpdateDepartmentBody {
    private String name;
    private boolean archived = false;
}
