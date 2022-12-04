package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;

/**
 * Configuration to parse EXCEL file with employees
 */
@Data
public class EmployeeImportConfig {

    /**
     * 1 - first sheet
     */
    private int sheetNumber = 1;

    /**
     * First row with actual employee data (not header)
     */
    private int tableStartRow = 8;

    private Short displayNameCell = 1;
    private Short emailCell = 2;
}
