package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class CreateOrUpdateEmployeeBody extends EmployeeAllFields {
    private Integer departmentId;
    private Integer currentProjectId;
    private String currentProjectRole;
    private Integer positionId;
    private Integer organizationId;
    private Integer officeLocationId;
    private String officeWorkplace;
    private String extErpId;

    @Nullable
    private Integer importProcessId;

    @Override
    public String toString() {
        return "CreateOrUpdateEmployeeBody(email=" + getEmail() + ")";
    }
}
