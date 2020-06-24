package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("project")
@Data
public class DictProjectEntry {
    @Id
    private Integer id;
    private String name;
}
