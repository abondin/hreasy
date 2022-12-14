package ru.abondin.hreasy.platform.service.admin.employee.dto;

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
    private List<ImportEmployeeExcelDto> data;
}
