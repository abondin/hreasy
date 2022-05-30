package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for admin_employees_template.xlsx XLSX template
 */
@Data
public class EmployeeExportDto {
    private String displayName;
    private LocalDate birthday;
    private String sex;
    @NotNull
    private String email;
    private String phone;
    private String skype;
    private String telegram;
    private LocalDate dateOfEmployment;
    private String level;
    private String department;
    private String currentProject;
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
