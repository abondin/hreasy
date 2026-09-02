package ru.abondin.hreasy.platform.service.allocation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ResourceAllocationSaveBody(@NotEmpty List<@Valid Change> changes) {
    public record Change(@NotNull Integer employeeId,
                         @NotNull Integer projectId,
                         @Min(0) @Max(1000) int percent,
                         Integer expectedRevisionId) {
    }
}
