package ru.abondin.hreasy.platform.service.admin.imp.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportWorkflowEntry;

@RequiredArgsConstructor
@Getter
public enum ExcelImportWorkflowType {
    EMPLOYEE(ImportWorkflowEntry.WF_TYPE_EMPLOYEE, "employee_import"),
    KIDS(ImportWorkflowEntry.WF_TYPE_KIDS, "kids_import");
    private final short wfType;
    private final String defaultBaseDir;

    public static ExcelImportWorkflowType fromWfType(short wfType) {
        for (ExcelImportWorkflowType type : ExcelImportWorkflowType.values()) {
            if (type.getWfType() == wfType) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown workflow type: " + wfType);
    }
}
