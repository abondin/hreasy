package ru.abondin.hreasy.platform.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.abondin.hreasy.platform.service.skills.dto.SkillDto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Employee information, available to any authenticated user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Basic employee details; project role and skills depend on the acting user permissions.")
public class EmployeeDto {
    @Schema(description = "HR Easy employee identifier.", example = "101")
    private Integer id;
    @Schema(description = "Employee display name.", example = "Alex Morgan")
    private String displayName;
    @Schema(description = "Birthday without year in dd.MM format.", example = "15.04", nullable = true)
    private String birthday;
    @Schema(description = "Sex value stored in the employee profile.", nullable = true)
    private String sex;
    @Schema(description = "Employee department.", nullable = true)
    private SimpleDictDto department;
    @Schema(description = "Current project; its role is filtered by the acting user permissions.", nullable = true)
    private CurrentProjectDictDto currentProject;
    @Schema(description = "Employee position.", nullable = true)
    private SimpleDictDto position;
    @Schema(description = "Office location assigned to the employee.", nullable = true)
    private OfficeLocationDictDto officeLocation;
    @Schema(description = "Workplace label within the office.", nullable = true)
    private String officeWorkplace;
    @Schema(description = "Business account associated with the employee current project.", nullable = true)
    private SimpleDictDto ba;
    @Schema(description = "Employee email; no external ERP identifier is included in this DTO.", example = "alex.morgan@example.test", nullable = true)
    private String email;
    @Schema(description = "Telegram account from the employee profile.", nullable = true)
    private String telegram;
    @Schema(description = "Time when the Telegram account was confirmed.", nullable = true)
    private OffsetDateTime telegramConfirmedAt;
    @Schema(description = "Whether an employee avatar file exists.")
    private boolean hasAvatar;
    @Schema(description = "Skills visible to the acting user.")
    private List<SkillDto> skills = new ArrayList<>();
}
