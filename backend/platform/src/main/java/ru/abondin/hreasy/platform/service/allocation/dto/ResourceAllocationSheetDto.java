package ru.abondin.hreasy.platform.service.allocation.dto;

import java.util.List;

public record ResourceAllocationSheetDto(int period,
                                         List<EmployeeDto> employees,
                                         List<ProjectDto> projects,
                                         List<AllocationDto> allocations) {
    public record EmployeeDto(Integer id, String displayName,
                              Integer departmentId, String departmentName,
                              Integer currentProjectId, String currentProjectName) {
    }

    public record ProjectDto(Integer id, String name,
                             Integer departmentId, String departmentName,
                             Integer baId, String baName,
                             boolean active, boolean editable) {
    }

    public record AllocationDto(Integer employeeId, Integer projectId,
                                int percent, Integer revisionId) {
    }
}
