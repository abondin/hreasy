package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Filter employees before export
 */
@Data
@Builder
public class EmployeeExportFilter {
    private boolean includeFired = false;
}
