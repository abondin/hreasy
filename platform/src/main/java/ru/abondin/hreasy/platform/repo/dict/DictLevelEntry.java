package ru.abondin.hreasy.platform.repo.dict;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

@Table("dict.level")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictLevelEntry {
    @Id
    private Integer id;
    private String name;
    private Integer weight;
    private boolean archived = false;
    @Nullable
    private OffsetDateTime updatedAt;

    @Nullable
    private Integer updatedBy;

}
