package ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportConfig;

/**
 * Configuration to parse EXCEL file with kids of employee
 */
@Data
public class EmployeeKidImportConfig implements ExcelImportConfig {


    private int sheetNumber = 1;


    private int tableStartRow = 8;

    private EmployeeKidImportConfigColumns columns = new EmployeeKidImportConfigColumns();

    @Data
    public static class EmployeeKidImportConfigColumns {

        /**
         * Only 3 fields is now supported
         */
        private String displayName = "G";
        private String birthday = "I";
        private String parentEmail = "K";
    }

}
