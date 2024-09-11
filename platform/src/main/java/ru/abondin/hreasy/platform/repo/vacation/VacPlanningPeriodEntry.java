package ru.abondin.hreasy.platform.repo.vacation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("vac.vac_planning_period")
@NoArgsConstructor
@AllArgsConstructor
public class VacPlanningPeriodEntry {
    @Id
    private Integer year;
    private OffsetDateTime openedAt;
    private Integer openedBy;
    private OffsetDateTime closedAt;
    private Integer closedBy;
    private String comment;
}
