package ru.abondin.hreasy.platform.service.allocation.dto;

import java.time.LocalDate;
import java.util.List;

import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto.ProjectDto;

public record ResourceAllocationProjectInputDto(int year,
                                                Integer selectedProjectId,
                                                List<MonthDto> months,
                                                List<EmployeeDto> employees,
                                                List<ProjectDto> projects,
                                                List<AllocationDto> allocations,
                                                List<OtherAllocationDto> otherAllocations,
                                                boolean canManagePeriods) {
    public record MonthDto(int period, boolean closed) {
    }

    public record EmployeeDto(Integer id, String displayName,
                              Integer currentProjectId, String currentProjectName,
                              LocalDate dateOfEmployment, LocalDate dateOfDismissal,
                              boolean dismissed) {
    }

    public record AllocationDto(int period, Integer employeeId, int percent, Integer revisionId) {
    }

    public record OtherAllocationDto(int period, Integer employeeId, int percent) {
    }
}
