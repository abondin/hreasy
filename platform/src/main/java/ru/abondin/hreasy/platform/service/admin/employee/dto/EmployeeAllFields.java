package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;

import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import java.time.LocalDate;

/**
 * Plain mapping of all employee fields (except id)
 */
@Data
@NoArgsConstructor
public class EmployeeAllFields {
    private String displayName;
    private LocalDate birthday;
    private String sex;
    @NonNull
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
