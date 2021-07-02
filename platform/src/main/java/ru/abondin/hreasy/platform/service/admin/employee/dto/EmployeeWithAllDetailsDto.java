package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;

@Data
public class EmployeeWithAllDetailsDto extends EmployeeAllFields {
    private Integer id;
    private String displayName;
    private String documentFull;

    private Integer departmentId;
    private Integer currentProjectId;
    private Integer positionId;
    private Integer officeLocationId;

}
