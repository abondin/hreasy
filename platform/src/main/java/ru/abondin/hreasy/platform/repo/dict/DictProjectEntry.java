package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("project")
@Data
public class DictProjectEntry {
    @Id
    private Integer id;
    private String name;
    private LocalDate endDate;
    private Integer departmentId;
}
