package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;


@Table("dict.office")
@Data
@NoArgsConstructor
public class DictOfficeEntry {
    @Id
    private Integer id;

    @NonNull
    private String name;

    @Nullable
    private String address;

    @Nullable
    private String description;

    private String mapName;


    private boolean archived = false;

    @Nullable
    private OffsetDateTime createdAt;

    @Nullable
    private Integer createdBy;

}
