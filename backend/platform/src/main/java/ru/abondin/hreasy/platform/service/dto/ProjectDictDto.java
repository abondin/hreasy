package ru.abondin.hreasy.platform.service.dto;

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
public class ProjectDictDto extends SimpleDictDto {
    @Nullable
    private String externalId;
    private List<ProjectWorkstreamDto> workstreams = List.of();
    @Nullable
    private Integer baId;
}
