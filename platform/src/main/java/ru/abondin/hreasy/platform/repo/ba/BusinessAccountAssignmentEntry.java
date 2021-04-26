package ru.abondin.hreasy.platform.repo.ba;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Links employees with business account position. Several different cases are allowed:
 * <ul>
 *     <li><code>businessAccount is not null and employee is not null</code> - Employee is 100% budgeted by ba position and spent full time. That means that different between account position rate and employee wages is a profit</li>
 *     <li><code>businessAccount is null and employee is not null</code> - Employee is spent full time without budgeting (Personnel reserve). That means that employee generates only costs at this moment</li>
 *     <li><code>businessAccount is not null and employee is null</code> - There is no assigned employee to BA account position. That means that account position generates only profit at this moment</li>
 *     <li><code>employmentRate < 1</code> - Part-time employment</li>
 *     <li><code>ba_positionRate < 1</code> - Partially budgeting</li>
 * </ul>
 *
 * Assignment can be closed if:
 * <ul>
 *     <li>Employee assigned/unassigned to/from BA position</li>
 *     <li>New BA position opened for unbudgeted employee</li>
 *     <li>BA position closed before endDate</li>
 * </ul>
 *
 */
@Data
public class BusinessAccountAssignmentEntry {
    private Integer id;
    private int businessAccount;
    private int employee;
    private int project;
    private Integer parentAssignment;
    private Integer baPosition;
    private Float employmentRate;
    private Float baPositionRate;
    private String comment;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private OffsetDateTime archivedAt;
    private Integer archivedBy;
    private OffsetDateTime closedAt;
    private Integer closedBy;
    private String closedReason;
    private String closedComment;
    private OffsetDateTime createdAt;
    private Integer createdBy;
}
