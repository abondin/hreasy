package ru.abondin.hreasy.platform.repo.dict;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

@Table("dict.position_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictPositionLogEntry {
    @Id
    private Integer id;
    private Integer positionId;

    private String name;
    @Nullable
    private OffsetDateTime createdAt;

    @Nullable
    private Integer createdBy;

    private boolean archived = false;

}
