package ru.abondin.hreasy.platform.service.allocation.dto;

import java.util.List;

import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto.EmployeeDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto.ProjectDto;

public record ResourceAllocationAnalyticsDto(int year,
                                              List<EmployeeDto> employees,
                                              List<ProjectDto> projects,
                                              List<AllocationDto> allocations) {
    public record AllocationDto(int period, Integer employeeId, Integer projectId, int percent) {
    }
}
