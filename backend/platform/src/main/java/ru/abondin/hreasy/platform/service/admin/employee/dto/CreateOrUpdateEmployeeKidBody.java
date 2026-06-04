package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

@Data
public class CreateOrUpdateEmployeeKidBody {
    private String displayName;
    private LocalDate birthday;

    @Nullable
    private Integer importProcessId;
}
