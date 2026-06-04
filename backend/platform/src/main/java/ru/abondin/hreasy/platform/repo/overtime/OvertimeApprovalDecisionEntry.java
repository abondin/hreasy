package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeApprovalDecisionDto;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;

/**
 * Approval of overtime report of employee
 */
@Data
@NoArgsConstructor
@Table("ovt.overtime_approval_decision")
public class OvertimeApprovalDecisionEntry {

    @Id
    private Integer id;

    /**
     * Link to overtime report
     */
    @NonNull
    private int reportId;

    /**
     * Current logged in user
     */
    @NonNull
    private int approver;

    /**
     * Approver decision
     */
    @NonNull
    private OvertimeApprovalDecisionDto.ApprovalDecision decision = OvertimeApprovalDecisionDto.ApprovalDecision.APPROVED;

    @NonNull
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
