package ru.abondin.hreasy.platform.repo.dict;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

@Table("dict.level_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictLevelLogEntry {
    @Id
    private Integer id;
    private Integer levelId;
    private String name;
    private Integer weight;
    @Nullable
    private OffsetDateTime createdAt;

    @Nullable
    private Integer createdBy;

    private boolean deleted;

}
