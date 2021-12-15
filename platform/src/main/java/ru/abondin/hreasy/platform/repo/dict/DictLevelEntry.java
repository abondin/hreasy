package ru.abondin.hreasy.platform.repo.dict;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("dict.dict_level")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictLevelEntry {
    @Id
    private Integer id;
    private String name;
    private Integer weight;
}
