package ru.abondin.hreasy.platform.service.admin.imp.dto;

import lombok.Data;

import java.util.List;

/**
 * Uses in {@link ru.abondin.hreasy.platform.service.admin.employee.imp.AdminEmployeeImportProcessor}
 * @param <R>
 */
@Data
public class ExcelImportProcessingResult<R extends ExcelImportRowDto> {
    private final List<R> rows;
    private final ExcelImportProcessStats stats;

    public ExcelImportProcessingResult(List<R> rows) {
        this.rows = rows;
        int processedRows = rows.size();
        int errors = 0;
        int newItems = 0;
        int updatedItems = 0;
        for (var row : rows) {
            if (row.getErrorCount() > 0) {
                errors += row.getErrorCount();
            }
            if (row.isNew()) {
                newItems++;
            } else if (row.getUpdatedCellsCount() > 0) {
                updatedItems++;
            }
        }
        this.stats = new ExcelImportProcessStats(processedRows, errors, newItems, updatedItems);
    }
}
