package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class CurrentProjectTransferRequestDto {
    private Integer id;
    private Integer employeeId;
    private Integer fromProjectId;
    private String fromProjectName;
    private Integer toProjectId;
    private String toProjectName;
    private String requestedProjectRole;
    private Integer createdBy;
    private String createdByDisplayName;
    private Integer approverEmployeeId;
    private String approverDisplayName;
    private OffsetDateTime createdAt;
    private Boolean canMakeDecision;
}
