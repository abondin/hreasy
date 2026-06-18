package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;

@Data
public class CurrentProjectTransferApproverDto {
    private Integer employeeId;
    private String displayName;
    private String email;
    private String managerType;
}
