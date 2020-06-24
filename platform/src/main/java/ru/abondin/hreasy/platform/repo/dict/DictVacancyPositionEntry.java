package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("dict_vacancy_position")
@Data
public class DictVacancyPositionEntry {
    @Id
    private Integer id;
    private String name;
    private String description;
}
