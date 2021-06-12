package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;

@Data
public class EmployeeWithAllDetailsDto extends EmployeeAllFields {
    private Integer id;
    private boolean hasAvatar;
}
