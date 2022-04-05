package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.LocalDate;

@Data
public class EmployeeKidDto {
    private Integer id;
    private String displayName;
    private SimpleDictDto parent;
    private LocalDate birthday;
    private Integer age;
}
