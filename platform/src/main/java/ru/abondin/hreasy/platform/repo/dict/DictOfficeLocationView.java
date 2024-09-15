package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import org.springframework.lang.Nullable;


@Data
public class DictOfficeLocationView extends DictOfficeLocationEntry {
    @Nullable
    private String officeName;
}
