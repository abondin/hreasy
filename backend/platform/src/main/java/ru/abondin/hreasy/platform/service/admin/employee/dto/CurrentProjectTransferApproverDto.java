package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentProjectTransferApproverDto {
    private Integer employeeId;
    private String displayName;
    private String email;
    private String managerType;
}
