package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;


@Table("dict.organization_log")
@Data
public class DictOrganizationLogEntry {
    @Id
    private Integer id;

    private Integer organizationId;

    @NonNull
    private String name;

    @Nullable
    private String description;

    private boolean archived = false;

    @Nullable
    private OffsetDateTime createdAt;

    @Nullable
    private Integer createdBy;


}
