package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Plain mapping of all employee fields (except id)
 */
@Data
public class EmployeeAllFields {
    private String displayName;
    private LocalDate birthday;
    private String sex;
    @NotNull
    private String email;
    private String phone;
    private String skype;
    private String telegram;
    private LocalDate dateOfEmployment;
    private Integer levelId;
    private String workType;
    private String workDay;
    private String registrationAddress;
    private String documentSeries;
    private String documentNumber;
    private String documentIssuedBy;
    private LocalDate documentIssuedDate;
    private String foreignPassport;
    private String cityOfResidence;
    private String englishLevel;
    private String familyStatus;
    private String spouseName;
    private String children;
    private LocalDate dateOfDismissal;

}
