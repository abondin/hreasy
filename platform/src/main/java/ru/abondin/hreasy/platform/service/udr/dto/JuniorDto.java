package ru.abondin.hreasy.platform.service.udr.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class JuniorDto {
    private SimpleDictDto junior;
    private SimpleDictDto mentor;
    private String role;
    private SimpleDictDto budgetingAccount;
    private OffsetDateTime createdAt;
    private SimpleDictDto createdBY;
    private OffsetDateTime graduatedAt;
    private SimpleDictDto graduatedBY;
}
