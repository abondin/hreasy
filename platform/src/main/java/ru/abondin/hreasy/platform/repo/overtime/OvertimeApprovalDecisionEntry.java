package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeApprovalDecisionDto;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * Approval of overtime report of employee
 */
@Data
@Table("ovt.overtime_approval_decision")
public class OvertimeApprovalDecisionEntry {

    @Id
    private Integer id;

    /**
     * Link to overtime report
     */
    @NotNull
    private int reportId;

    /**
     * Current logged in user
     */
    @NotNull
    private int approver;

    /**
     * Approver decision
     */
    @NotNull
    private OvertimeApprovalDecisionDto.ApprovalDecision decision = OvertimeApprovalDecisionDto.ApprovalDecision.APPROVED;

    @NotNull
    private OffsetDateTime decisionTime;

    /**
     * Shows that decision was canceled
     */
    @Nullable
    private OffsetDateTime cancelDecisionTime;

    @Nullable
    private String comment;

    @Data
    public static class OvertimeApprovalDecisionWithEmployeeEntry extends OvertimeApprovalDecisionEntry {
        @Column("approver_display_name")
        private String approverDisplayName;
    }
}
