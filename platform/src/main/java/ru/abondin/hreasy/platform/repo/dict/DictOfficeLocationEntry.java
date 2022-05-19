package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;


@Table("dict.office_location")
@Data
public class DictOfficeLocationEntry {
    @Id
    private Integer id;

    @NotNull
    private String name;

    @Nullable
    private String description;

    @Nullable
    private String office;

    private boolean archived = false;

    @Nullable
    private OffsetDateTime updatedAt;

    @Nullable
    private Integer updatedBy;

}
