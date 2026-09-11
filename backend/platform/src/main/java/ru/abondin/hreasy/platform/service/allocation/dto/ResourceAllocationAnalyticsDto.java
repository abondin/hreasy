package ru.abondin.hreasy.platform.service.allocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.abondin.hreasy.platform.service.dto.ProjectWorkstreamDto;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Employee-scoped sparse annual allocation snapshot: current employees of accessible projects or employees with a recorded allocation on an accessible project in the selected year. All annual allocations of these employees are included. Explicit zeros are included; missing cells mean no allocation. "
        + "No revision feed or closed-period states are included.")
public record ResourceAllocationAnalyticsDto(
        @Schema(description = "Calendar year.", example = "2026") int year,
        @Schema(description = "Visible employees with recorded annual allocations, including dismissed employees with qualifying recorded allocations.")
        List<EmployeeDto> employees,
        @Schema(description = "Projects referenced by visible allocation cells. Join /external/api/v1/projects by id for externalId.")
        List<ProjectDto> projects,
        @Schema(description = "Workstreams referenced by allocation cells, including soft-deleted workstreams.")
        List<ProjectWorkstreamDto> workstreams,
        @Schema(description = "Recorded monthly cells. Key: period, employeeId, projectId, workstreamId. "
                + "Replace the imported annual snapshot to account for deleted cells; do not only upsert returned rows.")
        List<AllocationDto> allocations) {

    @Schema(name = "ResourceAllocationEmployee", description = "Employee referenced by allocations; organizational fields describe the current employee profile.")
    public record EmployeeDto(
            @Schema(description = "HR Easy employee identifier.", example = "101") Integer id,
            @Schema(description = "Employee display name.", example = "Alex Morgan") String displayName,
            @Schema(description = "Current department identifier.", nullable = true) Integer departmentId,
            @Schema(description = "Current department name.", nullable = true) String departmentName,
            @Schema(description = "Current project identifier; not necessarily the allocated project.", nullable = true) Integer currentProjectId,
            @Schema(description = "Current project name.", nullable = true) String currentProjectName,
            @Schema(description = "Employee role on the current project.", nullable = true) String currentProjectRole,
            @Schema(description = "Employee email.", example = "alex.morgan@example.test", nullable = true) String email) {
    }

    @Schema(name = "ResourceAllocationProject", description = "Project referenced by allocations; metadata describes the current project configuration.")
    public record ProjectDto(
            @Schema(description = "HR Easy project identifier.", example = "301") Integer id,
            @Schema(description = "Project name.", example = "Example project") String name,
            @Schema(description = "Project department identifier.", nullable = true) Integer departmentId,
            @Schema(description = "Project department name.", nullable = true) String departmentName,
            @Schema(description = "Business account identifier.", nullable = true) Integer baId,
            @Schema(description = "Business account name.", nullable = true) String baName,
            @Schema(description = "Actual project start date.", nullable = true) LocalDate startDate,
            @Schema(description = "Actual project end date.", nullable = true) LocalDate endDate,
            @Schema(description = "Whether the project dates overlap the requested year.") boolean active,
            @Schema(description = "Whether the acting user may edit this project through the internal API; external API remains read-only.") boolean editable) {
    }

    @Schema(name = "ResourceAllocationCell", description = "Recorded monthly allocation for an employee and project/workstream dimension.")
    public record AllocationDto(
            @Schema(description = "Zero-based YYYYMM period: 202600 is January, 202608 is September, 202611 is December 2026.", example = "202608") int period,
            @Schema(description = "HR Easy employee identifier; joins employees.id.", example = "101") Integer employeeId,
            @Schema(description = "HR Easy project identifier; joins projects.id.", example = "301") Integer projectId,
            @Schema(description = "HR Easy workstream identifier; joins workstreams.id. Null means a separate project-level allocation, not a total over workstreams.", nullable = true) Integer workstreamId,
            @Schema(description = "Explicit allocation percentage. Zero is a recorded value; an absent allocation has no cell in the response.",
                    minimum = "0", maximum = "1000", example = "0") int percent) {
    }
}
