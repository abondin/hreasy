package ru.abondin.hreasy.platform.service.udr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class UdrEmployeeDto {
    @NotNull
    private EmployeeDto employee;
    private boolean active;
    private List<UdrCustomFieldValue> customFieldsValues = new ArrayList<>();
}
