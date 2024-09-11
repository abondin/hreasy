package ru.abondin.hreasy.platform.repo.dict.office_map;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("dict.office_location_map")
@Data
public class OfficeLocationMapEntry {
    @Id
    private Integer id;
    private String mapSvg;
    private String description;
    private OffsetDateTime createdAt;
    private Integer createdBy;
    private OffsetDateTime deletedAt;
    private Integer deletedBy;
}
