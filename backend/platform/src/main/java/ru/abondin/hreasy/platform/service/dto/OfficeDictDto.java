package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Office reference indicating whether it is a primary office of the organization. */
@Data
@EqualsAndHashCode(callSuper = true)
public class OfficeDictDto extends SimpleDictDto {
    private boolean primaryOffice;
}
