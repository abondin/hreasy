package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@ToString(of = {"email"})
public class CreateOrUpdateEmployeeBody extends EmployeeAllFields {
    private Integer departmentId;
    private Integer currentProjectId;
    private String currentProjectRole;
    private Integer positionId;
    private Integer organizationId;
    private Integer officeLocationId;
    private String extErpId;

    @Nullable
    private Integer importProcessId;
}
