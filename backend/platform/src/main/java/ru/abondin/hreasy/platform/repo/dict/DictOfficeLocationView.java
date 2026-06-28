package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;


@Data
@EqualsAndHashCode(callSuper = false)
public class DictOfficeLocationView extends DictOfficeLocationEntry {
    @Nullable
    private String officeName;
}
