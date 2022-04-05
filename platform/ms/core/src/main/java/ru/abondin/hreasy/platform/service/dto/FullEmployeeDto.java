package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Full representation of Employee DTO for admin APIs.
 */
@Data
public class FullEmployeeDto extends EmployeeDto {
    private LocalDate dateOfEmployment;
    private SimpleDictDto position;
    private SimpleDictDto level;
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
