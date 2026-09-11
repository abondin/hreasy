package ru.abondin.hreasy.platform.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Simple dict for project
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(description = "Project dictionary entry; includes active workstreams only.")
public class ProjectDictDto extends SimpleDictDto {
    @Nullable
    @Schema(description = "Optional project identifier in an external system; unique across projects when supplied.", example = "project-alpha", nullable = true)
    private String externalId;
    @Schema(description = "Active workstreams belonging to this project. Deleted workstreams referenced by allocations are available in allocation analytics.")
    private List<ProjectWorkstreamDto> workstreams = List.of();
    @Nullable
    @Schema(description = "HR Easy business account identifier.", nullable = true)
    private Integer baId;
}
