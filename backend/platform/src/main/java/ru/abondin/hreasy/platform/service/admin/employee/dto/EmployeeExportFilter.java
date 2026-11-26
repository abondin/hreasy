package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Filter employees before export
 */
@Data
@Builder
public class EmployeeExportFilter {
    private boolean includeFired;
    private boolean includeAdditional;
    private boolean includePassport;
    private boolean includeKids;
    @Builder.Default
    private List<Integer> departmentIds = List.of();
    @Builder.Default
    private List<Integer> baIds = List.of();
}
