package ru.abondin.hreasy.platform.repo.dict;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;


@Table("dict.office_workplace")
@Data
@NoArgsConstructor
public class DictOfficeWorkplaceEntry {
    @Id
    private Integer id;
    @NotNull
    private Integer officeLocationId;

    @NonNull
    private String name;

    @Nullable
    private String description;

    private Integer mapX;

    private Integer mapY;


    private boolean archived = false;

    @Nullable
    private OffsetDateTime createdAt;

    @Nullable
    private Integer createdBy;

}
