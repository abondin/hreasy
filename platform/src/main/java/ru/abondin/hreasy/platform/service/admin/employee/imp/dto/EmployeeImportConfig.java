package ru.abondin.hreasy.platform.service.admin.employee.imp.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportConfig;

/**
 * Configuration to parse EXCEL file with employees
 */
@Data
public class EmployeeImportConfig implements ExcelImportConfig {


    private int sheetNumber = 1;


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
        private String phone = "AJ";
        private String department = "AK";
        private String position = "AL";
        private String dateOfEmployment = "AM";
        private String dateOfDismissal = "AN";
        private String birthday = "AP";
        private String sex = "AQ";
        private String documentSeries = "AS";
        private String documentNumber = "AT";
        private String documentIssuedDate = "AU";
        private String documentIssuedBy = "AV";
        private String registrationAddress = "AX";
        private String organization = "BF";
    }

}
