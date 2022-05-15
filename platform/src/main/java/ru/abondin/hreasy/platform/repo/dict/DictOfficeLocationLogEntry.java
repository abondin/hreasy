package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;


@Table("dict.office_location_log")
@Data
public class DictOfficeLocationLogEntry {
    @Id
    private Integer id;

    private Integer officeLocationId;

    @NotNull
    private String name;

    @Nullable
    private String description;

    @Nullable
    private String office;

    @Nullable
    private OffsetDateTime createdAt;

    @Nullable
    private Integer createdBy;

    private boolean archived = false;

}
