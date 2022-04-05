package ru.abondin.hreasy.platform.repo.assessment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Table("assmnt.assessment")
public class AssessmentEntry {
    @Id
    private Integer id;
    private Integer employee;
    private LocalDate plannedDate;
    private OffsetDateTime createdAt;
    private Integer createdBy;
    private OffsetDateTime completedAt;
    private Integer completedBy;
    private OffsetDateTime canceledAt;
    private Integer canceledBy;
}
