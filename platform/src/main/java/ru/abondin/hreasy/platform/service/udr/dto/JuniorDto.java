package ru.abondin.hreasy.platform.service.udr.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.dto.ValueWithStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
public class JuniorDto {
    private int id;
    @NotNull
    private SimpleDictDto juniorEmpl;
    private LocalDate juniorDateOfEmployment;
    private ValueWithStatus<Long> juniorInCompanyMonths;
    private ValueWithStatus<Long> monthsWithoutReport;
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

    public JuniorReportDto getLatestReport() {
        return reports.stream().sorted(Comparator.comparing(JuniorReportDto::getCreatedAt).reversed()).findFirst().orElse(null);
    }

    public record Graduation(OffsetDateTime graduatedAt, SimpleDictDto graduatedBy, String comment) {
    }

}
