package ru.abondin.hreasy.platform.repo.udr;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;

import java.time.LocalDate;

@Data
public class JuniorView extends JuniorEntry {

    private String juniorEmplDisplayName;
    private LocalDate juniorDateOfEmployment;
    private String mentorDisplayName;
    private String createdByDisplayName;
    private String graduatedByDisplayName;
    private String budgetingAccountName;

    private Integer currentProjectId;
    private String currentProjectName;
    private String currentProjectRole;

    private Json reports;
}
