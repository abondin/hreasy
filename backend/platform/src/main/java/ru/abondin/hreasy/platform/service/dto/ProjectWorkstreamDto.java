package ru.abondin.hreasy.platform.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * Project workstream exposed to web and external consumers.
 */
@Schema(description = "Project dictionaries include all non-deleted workstreams, including archived ones. Allocation analytics also includes referenced deleted workstreams.")
public record ProjectWorkstreamDto(
        @Schema(description = "HR Easy workstream identifier.", example = "401") Integer id,
        @Schema(description = "Optional external identifier, unique among non-deleted workstreams within its project.",
                example = "delivery", nullable = true) String externalId,
        @Schema(description = "Workstream display name.", example = "Delivery") String displayName,
        @Schema(description = "Workstream description.", nullable = true) String description,
        @Schema(description = "Archive time; null when not archived.", nullable = true) OffsetDateTime archivedAt) {
}
