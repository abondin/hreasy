package ru.abondin.hreasy.platform.service.overtime.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** Monthly integration snapshot with separate totals for each project workstream. */
@Schema(description = "Monthly employee overtime summary with items grouped by date, project and workstream.")
public record ExternalOvertimeSummaryDto(
        int employeeId,
        int reportId,
        float totalHours,
        OffsetDateTime lastUpdate,
        OffsetDateTime lastApprove,
        OffsetDateTime lastDecline,
        OvertimeEmployeeSummary.OvertimeApprovalCommonStatus commonApprovalStatus,
        List<ItemDto> items) {

    @Schema(name = "ExternalOvertimeItemDto", description = "Hours for one date, project and optional workstream, including soft-deleted workstreams.")
    public record ItemDto(
            LocalDate date,
            int projectId,
            int reportId,
            @Schema(description = "HR Easy workstream ID; null for project-level overtime.", nullable = true)
            Integer workstreamId,
            @Schema(description = "Optional external integration key of the workstream.", nullable = true)
            String workstreamExternalId,
            @Schema(description = "Workstream name, including historical soft-deleted workstreams.", nullable = true)
            String workstreamDisplayName,
            float hours) {
    }
}
