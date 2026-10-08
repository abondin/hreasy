package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Value;

import java.time.LocalDate;

/** Child row linked to an employee included in the same Excel export. */
@Value
public class EmployeeKidExportDto {
    EmployeeExportDto parent;
    String displayName;
    LocalDate birthday;
    Integer age;
}
