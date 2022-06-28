package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;
import lombok.ToString;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

@Data
@ToString(of = {"email"})
public class CreateOrUpdateEmployeeBody extends EmployeeAllFields {
    private Integer departmentId;
    private Integer currentProjectId;
    private String currentProjectRole;
    private Integer positionId;
    private Integer officeLocationId;

}
