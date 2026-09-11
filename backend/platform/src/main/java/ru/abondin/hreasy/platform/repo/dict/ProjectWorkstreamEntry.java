package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

/**
 * Project workstream with audit fields and soft deletion.
 */
@Data
@Table("proj.project_workstream")
public class ProjectWorkstreamEntry {
    @Id
    private Integer id;
    private Integer projectId;
    private String externalId;
    private String displayName;
    private String description;
    private OffsetDateTime createdAt;
    private Integer createdBy;
    private OffsetDateTime updatedAt;
    private Integer updatedBy;
    private OffsetDateTime deletedAt;
    private Integer deletedBy;
}
