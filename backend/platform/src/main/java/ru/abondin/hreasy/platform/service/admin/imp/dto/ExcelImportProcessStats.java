package ru.abondin.hreasy.platform.service.admin.imp.dto;

/**
 * Uses in {@link ExcelImportProcessingResult}
 * @param processedRows
 * @param errors
 * @param newItems
 * @param updatedItems
 */
public record ExcelImportProcessStats(int processedRows, int errors, int newItems, int updatedItems) {

}
