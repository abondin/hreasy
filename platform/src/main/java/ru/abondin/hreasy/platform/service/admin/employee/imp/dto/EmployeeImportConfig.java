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
        private String email = "R";
        private String phone = "AI";
        private String department = "AJ";
        private String position = "AK";
        private String dateOfEmployment = "AL";
        private String dateOfDismissal = "AM";
        private String birthday = "AO";
        private String sex = "AP";
        private String documentSeries = "AR";
        private String documentNumber = "AS";
        private String documentIssuedDate = "AT";
        private String documentIssuedBy = "AU";
        private String registrationAddress = "AW";
    }

}
