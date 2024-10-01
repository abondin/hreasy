package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeWithAllDetailsDto extends EmployeeAllFields {
    private Integer id;
    private String displayName;
    private String documentFull;

    private Integer departmentId;
    private Integer currentProjectId;
    private Integer organizationId;
    private String currentProjectRole;
    private Integer positionId;
    private Integer officeLocationId;
    private Integer officeWorkplaceId;
    private Integer baId;

    private String extErpId;

    /**
     * If employee is not dismissed
     */
    private boolean active;

}
