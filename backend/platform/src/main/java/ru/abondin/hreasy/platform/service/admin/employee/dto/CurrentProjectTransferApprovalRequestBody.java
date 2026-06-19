package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentProjectTransferApprovalRequestBody {
    private Integer fromProjectId;
    private Integer toProjectId;
    private String role;
    private Integer approverEmployeeId;
}
