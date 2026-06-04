package ru.abondin.hreasy.platform.service.overtime.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;

/**
 * Approval of overtime report of employee
 */
@Data
public class OvertimeApprovalDecisionDto {

    public enum ApprovalDecision {
        APPROVED,
        DECLINED;
    }

    private Integer id;

    /**
     * Approver employeeId
     */
    @NonNull
    private int approver;

    /**
     * Approver displayName
     */
    @NonNull
    private String approverDisplayName;

    /**
     * Approver decision
     */
    @NonNull
    private ApprovalDecision decision = ApprovalDecision.APPROVED;

    @NonNull
    private OffsetDateTime decisionTime;

    /**
     * Shows that decision was canceled
     */
    @Nullable
    private OffsetDateTime cancelDecisionTime;

    @Nullable
    private String comment;

    /**
     * true if any overtime items were added after approval decision time
     */
    private boolean outdated = false;
}
