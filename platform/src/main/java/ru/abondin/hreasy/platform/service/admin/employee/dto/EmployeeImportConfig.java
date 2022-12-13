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

    private EmployeeImportConfigColumns columns = new EmployeeImportConfigColumns();

    @Data
    public static class EmployeeImportConfigColumns {

        /**
         * 14 fields supported at the moment
         */
        private String displayName = "B";
        private String externalErpId = "H";
        private String email = "Q";
        private String phone = "AG";
        private String department = "AH";
        private String position = "AI";
        private String dateOfEmployment = "AJ";
        private String dateOfDismissal = "AK";
        private String birthday = "AL";
        private String sex = "AM";
        private String documentSeries = "AN";
        private String documentNumberCell = "AO";
        private String documentIssuedDateCell = "AP";
        private String documentIssuedByCell = "AQ";
        private String registrationAddressCell = "AR";
    }

}
