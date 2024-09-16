package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;


@Table("dict.office_location")
@Data
@NoArgsConstructor
public class DictOfficeLocationEntry {
    @Id
    private Integer id;

    @NonNull
    private String name;

    @Nullable
    private String description;

    @Nullable
    private Integer officeId;

    private boolean archived = false;

    @Nullable
    private OffsetDateTime updatedAt;

    @Nullable
    private Integer updatedBy;

}
