package ru.abondin.hreasy.platform.service.udr.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class JuniorExportDto {
    private int id;
    @NotNull
    private String juniorEmpl;
    private Long juniorInCompanyMonths;
    private Long monthsWithoutReport;
    @Nullable
    private String mentor;
    @NotNull
    private String role;
    @Nullable
    private String currentProject;
    @Nullable
    private String budgetingAccount;
    private LocalDate graduatedAt;
    private String graduatedBy;
    private String graduatedComment;

    // For some reason in jxls ${r.reports[0].comment} doesn't work. But ${r.reports[0]} works fine.
    // This is way we have 4 different collections instead of List<Report> reports
    private List<Short> reportsProgress = new ArrayList<>();
    private List<LocalDate> reportsCreatedAt = new ArrayList<>();
    private List<String> reportsCreatedBy = new ArrayList<>();
    private List<String> reportsComment = new ArrayList<>();
}
