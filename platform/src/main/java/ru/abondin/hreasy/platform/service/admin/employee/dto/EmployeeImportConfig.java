package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;

/**
 * Configuration to parse EXCEL file with employees
 */
@Data
public class EmployeeImportConfig {

    /**
     * First sheet uses by default
     */
    private int sheetIndex = 0;

    /**
     * First row with actual employee data (not header)
     */
    private int tableStartRow = 8;

    private String nameCell = "A";
    private String ageCell = "B";
}
