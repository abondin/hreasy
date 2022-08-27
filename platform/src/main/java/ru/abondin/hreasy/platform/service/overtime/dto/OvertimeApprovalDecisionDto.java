package ru.abondin.hreasy.platform.service.overtime.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
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
    @NotNull
    private int approver;

    /**
     * Approver displayName
     */
    @NotNull
    private String approverDisplayName;

    /**
     * Approver decision
     */
    @NotNull
    private ApprovalDecision decision = ApprovalDecision.APPROVED;

    @NotNull
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
