package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import org.springframework.lang.Nullable;


@Data
public class DictOfficeWorkplaceView extends DictOfficeWorkplaceEntry {
    @Nullable
    private String officeLocationName;

    @Nullable
    private Integer officeId;
    @Nullable
    private String officeName;

}
