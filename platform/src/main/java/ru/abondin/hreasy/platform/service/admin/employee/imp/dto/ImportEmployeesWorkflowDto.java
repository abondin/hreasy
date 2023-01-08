package ru.abondin.hreasy.platform.service.admin.employee.imp.dto;

import lombok.Data;

import java.util.List;

/**
 * Import employee process split in several phases:
 * <ol>
 *     <li>Request from user incoming excel document and parsing configuration</li>
 *     <li>Process incoming excel, map dictionaries like departments and positions, load existing employees and make a diff</li>
 *     <li>Show new and updated employees to user and request an accept</li>
 *     <li>Create/Update employees in database</li>
 * </ol>
 */
@Data
public class ImportEmployeesWorkflowDto {
    private int id;
    private int newEmployeesCnt;
    private int updatedEmployeesCnt;
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
    private EmployeeImportConfig config;
    private List<ImportEmployeeExcelRowDto> importedRows;
    private ImportProcessStats importProcessStats;
    private String filename;
}
