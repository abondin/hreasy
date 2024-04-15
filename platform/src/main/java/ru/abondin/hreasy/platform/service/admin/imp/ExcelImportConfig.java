package ru.abondin.hreasy.platform.service.admin.imp;

/**
 * Base class for excel import configuration
 */
public interface ExcelImportConfig {

    /**
     * 1 - first sheet
     */
    int getSheetNumber();

    /**
     * First row with actual data (not header)
     */
    int getTableStartRow();
}
