package ru.abondin.hreasy.platform.service.external.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** Public integration contracts, independent of web DTOs and database identifiers. */
public final class ExternalApiDto {
    private ExternalApiDto() {
    }

    @Schema(name = "ExternalReference", description = "Business reference. The name is a label; externalId is the integration key.")
    public record Reference(String name,
                            @Schema(nullable = true, description = "Null when no integration key has been configured.") String externalId) {
    }

    @Schema(name = "ExternalProjectReference")
    public record ProjectReference(String name, @Schema(nullable = true) String externalId,
                                   @Schema(nullable = true) Reference ba) {
    }

    @Schema(name = "ExternalEmployee", description = "Active employee profile. Email is returned exactly as stored and is the external key.")
    public record Employee(String email, String displayName, String department, String position,
                           @Schema(nullable = true) ProjectReference currentProject, boolean hasAvatar) {
    }

    @Schema(name = "ExternalProject", description = "Project and its active workstreams. Workstream keys are scoped to the project.")
    public record Project(String name, @Schema(nullable = true) String externalId,
                          @Schema(nullable = true) Reference ba, boolean active, List<Reference> workstreams) {
    }

    @Schema(name = "ExternalOvertime", description = "Monthly hours, including dismissed employees identified only by email.")
    public record Overtime(String employeeEmail, String period, float totalHours,
                           @Schema(nullable = true, description = "Latest creation time among remaining items, not a change cursor.") OffsetDateTime lastUpdate,
                           @Schema(nullable = true) OffsetDateTime lastApprove,
                           @Schema(nullable = true) OffsetDateTime lastDecline,
                           @Schema(allowableValues = {"NO_DECISIONS", "DECLINED", "APPROVED_NO_DECLINED", "APPROVED_OUTDATED"}) String commonApprovalStatus,
                           List<OvertimeItem> items) {
    }

    @Schema(name = "ExternalOvertimeItem", description = "Hours grouped by date, project and external workstream key.")
    public record OvertimeItem(LocalDate date, ProjectReference project,
                               @Schema(nullable = true, description = "Null means project-level hours.") Reference workstream,
                               float hours) {
    }

    @Schema(name = "ExternalAllocations", description = "Sparse annual snapshot. Replace a previously imported snapshot to remove deleted cells. No employee profiles are included.")
    public record Allocations(int year, List<Allocation> allocations) {
    }

    @Schema(name = "ExternalAllocation", description = "Key: employeeEmail, period, project.externalId, workstream.externalId when configured. Reused workstream keys are summed; explicit zeros are retained.")
    public record Allocation(String employeeEmail,
                             @Schema(example = "2026-09", description = "ISO calendar month, YYYY-MM.") String period,
                             ProjectReference project,
                             @Schema(nullable = true, description = "Null means a separate project-level allocation.") Reference workstream,
                             int percent) {
    }
}
