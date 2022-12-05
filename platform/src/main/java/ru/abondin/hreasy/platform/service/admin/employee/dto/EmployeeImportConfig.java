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
    private Short emailCell = 17;
    private Short phoneCell = 33;
    private Short departmentNameCell = 34;
    private Short positionNameCell = 35;
    private Short dateOfEmploymentCell = 36;
    private Short dateOfDismissalCell = 37;
    private Short birthdayCell = 39;
    private Short sexCell = 40;
    private Short documentSeriesCell = 42;
    private Short documentNumberCell = 43;
    private Short documentIssuedDateCell = 44;
    private Short documentIssuedByCell = 45;
    private Short registrationAddressCell = 47;
}
