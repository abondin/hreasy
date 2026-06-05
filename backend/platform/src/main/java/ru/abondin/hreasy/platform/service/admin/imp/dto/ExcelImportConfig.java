package ru.abondin.hreasy.platform.service.admin.imp.dto;

/**
 * Base class for excel import configuration
 * used in {@link ru.abondin.hreasy.platform.service.admin.imp.ExcelImporter}
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
