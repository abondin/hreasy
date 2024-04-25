package ru.abondin.hreasy.platform.service.admin.imp;

import lombok.Data;

import java.util.List;

/**
 * Import process split in several phases:
 * <ol>
 *     <li>Request from user incoming excel document and parsing configuration</li>
 *     <li>Process incoming excel, map dictionaries like departments and positions, load existing data and make a diff</li>
 *     <li>Show new and updated items to user and request an accept</li>
 *     <li>Create/Update items in database</li>
 * </ol>
 */
@Data
public class ExcelImportWorkflowDto<C extends ExcelImportConfig, R extends ExcelImportRowDto> {
    private int id;
    private int newItemsCnt;
    private int updatedItemsCnt;
    /**
     * <ul>
     *     <li>0 - created</li>
     *     <li>1 - file uploaded</li>
     *     <li>2 - configuration set</li>
     *     <li>3 - changes applied</li>
     *     <li>-1 - aborted</>
     * </ul>
     */
    private int state = 0;
    private C config;
    private List<R> importedRows;
    private ExcelImportProcessStats importProcessStats;
    private String filename;
}
