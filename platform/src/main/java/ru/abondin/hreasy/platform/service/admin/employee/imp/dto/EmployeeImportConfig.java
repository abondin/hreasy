package ru.abondin.hreasy.platform.service.admin.employee.imp.dto;

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

    private EmployeeImportConfigColumns columns = new EmployeeImportConfigColumns();

    @Data
    public static class EmployeeImportConfigColumns {

        /**
         * 15 fields supported at the moment
         */
        private String displayName = "B";
        private String externalErpId = "H";
        private String email = "Q";
        private String phone = "AG";
        private String department = "AH";
        private String position = "AI";
        private String dateOfEmployment = "AJ";
        private String dateOfDismissal = "AK";
        private String birthday = "AM";
        private String sex = "AN";
        private String documentSeries = "AP";
        private String documentNumberCell = "AQ";
        private String documentIssuedDateCell = "AR";
        private String documentIssuedByCell = "AS";
        private String registrationAddressCell = "AU";
    }

}
