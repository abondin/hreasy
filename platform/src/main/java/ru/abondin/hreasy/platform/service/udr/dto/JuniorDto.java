package ru.abondin.hreasy.platform.service.udr.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class JuniorDto {
    @NotNull
    private SimpleDictDto junior;
    @Nullable
    private SimpleDictDto mentor;
    @NotNull
    private String role;
    @Nullable
    private CurrentProjectDictDto currentProject;
    @Nullable
    private SimpleDictDto budgetingAccount;
    @NotNull
    private OffsetDateTime createdAt;
    @NotNull
    private SimpleDictDto createdBy;
    @Nullable
    private Graduation graduation;
    private List<JuniorReportDto> reports = new ArrayList<>();

    public record Graduation(OffsetDateTime graduatedAt, SimpleDictDto graduatedBy, String comment) {
    }
}
