package ru.abondin.hreasy.platform.repo.ba;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Links employees with business account position. Several cases are allowed:
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
@Table("ba.ba_assignment")
public class BusinessAccountAssignmentEntry {
    @Id
    private Integer id;
    private Integer ancestorAssignment;
    private int businessAccount;
    private Integer employee;
    private Integer project;
    private Integer baPosition;
    //(18,2)
    private BigDecimal employmentRate;
    //(1,2)
    private BigDecimal employmentRateFactor=BigDecimal.ONE;
    //(18,2)
    private BigDecimal baPositionRate;
    //(1,2)
    private BigDecimal baPositionRateFactor=BigDecimal.ONE;
    private String comment;
    private LocalDate startDate;
    private LocalDate endDate;
    private OffsetDateTime closedAt;
    private Integer closedBy;
    /**
     * One of
     * <ul>
     *      <li><b>employee_dismissed</b> - Employee dismissed</li>
     *      <li><b>ba_position_closed</b> - BA Position closed, project closed, ba closed</li>
     *      <li><b>employee_position_updated</b> - Employee moved to another position or moved to "bench"</li>
     *      <li><b>employment_rate_updated</b> - Employee rate updated</li>
     *      <li><b>employment_rate_factor_updated</b> - Employee rate factor updated</li>
     *      <li><b>bapositition_rate_updated</b> - BA position rate updated</li>
     *      <li><b>bapositition_rate_factor_updated</b> - BA position rate factor updated</li>
     *  * </ul>
     */
    private String closedReason;
    private String closedComment;
    private OffsetDateTime createdAt;
    private Integer createdBy;
}
