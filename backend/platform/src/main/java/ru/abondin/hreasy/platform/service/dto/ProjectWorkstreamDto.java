package ru.abondin.hreasy.platform.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Project workstream exposed to web and external consumers.
 */
@Schema(description = "Project workstream. Project dictionaries contain active workstreams; allocation analytics also includes referenced deleted workstreams.")
public record ProjectWorkstreamDto(
        @Schema(description = "HR Easy workstream identifier.", example = "401") Integer id,
        @Schema(description = "Optional external identifier, unique among active workstreams within its project.",
                example = "delivery", nullable = true) String externalId,
        @Schema(description = "Workstream display name.", example = "Delivery") String displayName,
        @Schema(description = "Workstream description.", nullable = true) String description) {
}
