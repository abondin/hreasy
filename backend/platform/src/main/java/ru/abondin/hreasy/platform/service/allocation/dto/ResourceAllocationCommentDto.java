package ru.abondin.hreasy.platform.service.allocation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

/** Internal API contracts for allocation-cell comment threads. */
public record ResourceAllocationCommentDto(
        Integer id,
        String text,
        AuthorDto author,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        boolean mine) {

    public record AuthorDto(Integer id, String displayName) {
    }

    public record CreateBody(
            @NotNull Integer period,
            @NotNull Integer employeeId,
            @NotNull Integer projectId,
            Integer workstreamId,
            @NotBlank @Size(max = 4000) String text) {
    }

    public record UpdateBody(@NotBlank @Size(max = 4000) String text) {
    }

    public record SummaryDto(int year, List<RowSummaryDto> rows) {
    }

    public record RowSummaryDto(
            EmployeeDto employee,
            ProjectDto project,
            WorkstreamDto workstream,
            List<CellSummaryDto> cells) {
    }

    public record EmployeeDto(
            Integer id,
            String displayName,
            String email,
            Integer departmentId,
            String departmentName,
            Integer currentProjectId,
            String currentProjectName,
            String currentProjectRole) {
    }

    public record ProjectDto(
            Integer id,
            String name,
            Integer departmentId,
            String departmentName,
            Integer baId,
            String baName) {
    }

    public record WorkstreamDto(Integer id, String displayName) {
    }

    public record CellSummaryDto(int period, int commentCount) {
    }
}
