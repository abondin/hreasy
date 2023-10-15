package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;

import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import java.time.LocalDate;

/**
 * DTO for admin_employees_template.xlsx XLSX template
 */
@Data
@NoArgsConstructor
public class EmployeeExportDto {
    private String displayName;
    private LocalDate birthday;
    private String sex;
    @NonNull
    private String email;
    private String phone;
    private String skype;
    private String telegram;
    private LocalDate dateOfEmployment;
    private String level;
    private String department;
    private String currentProject;
    private String currentProjectRole;
    private String ba;
    private String position;
    private String officeLocation;
    private String workType;
    private String workDay;
    private String registrationAddress;
    private String documentFull;
    private String foreignPassport;
    private String cityOfResidence;
    private String englishLevel;
    private String familyStatus;
    private String spouseName;
    private String children;
    private LocalDate dateOfDismissal;
}
