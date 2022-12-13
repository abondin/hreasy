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
    private int tableStartRow = 11;

    /**
     * 14 fields supported at the moment
     */
    private Short displayNameCell = 2;
    private Short externalErpId = 8;
    private Short emailCell = 17;
    private Short phoneCell = 33;
    private Short departmentCell = 34;
    private Short positionCell = 35;
    private Short dateOfEmploymentCell = 36;
    private Short dateOfDismissalCell = 37;
    private Short birthdayCell = 38;
    private Short sexCell = 39;
    private Short documentSeriesCell = 40;
    private Short documentNumberCell = 41;
    private Short documentIssuedDateCell = 42;
    private Short documentIssuedByCell = 43;
    private Short registrationAddressCell = 44;
}
