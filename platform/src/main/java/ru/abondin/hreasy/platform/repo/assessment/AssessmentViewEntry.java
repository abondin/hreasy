package ru.abondin.hreasy.platform.repo.assessment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class AssessmentViewEntry extends AssessmentEntry{
    private String createdByLastname;
    private String createdByFirstname;
    private String createdByPatronymicName;

    private String completedByLastname;
    private String completedByFirstname;
    private String completedByPatronymicName;

    private String canceledByLastname;
    private String canceledByFirstname;
    private String canceledByPatronymicName;
}
