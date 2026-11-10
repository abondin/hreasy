package ru.abondin.hreasy.platform.service.admin.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class CreateOrUpdateEmployeeBody extends EmployeeAllFields {
    @Override
    @NotNull
    public List<@NotBlank @Email String> getAdditionalEmails() {
        return super.getAdditionalEmails();
    }

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
