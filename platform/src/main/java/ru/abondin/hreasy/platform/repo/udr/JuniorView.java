package ru.abondin.hreasy.platform.repo.udr;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;

@Data
public class JuniorView extends JuniorEntry {

    private String juniorDisplayName;
    private String mentorDisplayName;
    private String createdByDisplayName;
    private String graduatedByDisplayName;
    private String budgetingAccountName;

    private Json reports;
}
