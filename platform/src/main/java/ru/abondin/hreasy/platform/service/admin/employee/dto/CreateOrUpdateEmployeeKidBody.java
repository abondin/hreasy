package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateOrUpdateEmployeeKidBody {
    private String displayName;
    private LocalDate birthday;
}
