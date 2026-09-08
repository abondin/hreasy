package ru.abondin.hreasy.platform.service.allocation.dto;

import java.time.LocalDate;
import java.util.List;

import ru.abondin.hreasy.platform.service.dto.ProjectWorkstreamDto;

public record ResourceAllocationAnalyticsDto(int year,
                                              List<EmployeeDto> employees,
                                              List<ProjectDto> projects,
                                              List<ProjectWorkstreamDto> workstreams,
                                              List<AllocationDto> allocations) {
    public record EmployeeDto(Integer id, String displayName,
                              Integer departmentId, String departmentName,
                              Integer currentProjectId, String currentProjectName,
                              String currentProjectRole, String email) {
    }

    public record ProjectDto(Integer id, String name,
                             Integer departmentId, String departmentName,
                             Integer baId, String baName,
                             LocalDate startDate, LocalDate endDate,
                             boolean active, boolean editable) {
    }

    public record AllocationDto(int period, Integer employeeId, Integer projectId, Integer workstreamId, int percent) {
    }
}
