package ru.abondin.hreasy.platform.repo.ba;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Table("ba.ba_assignment_history")
public class BusinessAccountAssignmentHistoryEntry {
    @Id
    private Integer id;
    private int baAssignmentId;
    private int businessAccount;
    private Integer employee;
    private Integer project;
    private Integer parentAssignment;
    private Integer baPosition;
    private Float employmentRate;
    private Float baPositionRate;
    private String comment;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private boolean archived;
    private OffsetDateTime closedAt;
    private Integer closedBy;
    private String closedReason;
    private String closedComment;
    private OffsetDateTime updatedAt;
    private Integer updatedBy;
}
