package ru.abondin.hreasy.platform.service.overtime.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated overtime information
 */
@Data
public class OvertimeEmployeeSummary {

    /**
     * Overtimes for one employee, aggregated for given day and project
     */
    @Data
    public static class OvertimeDaySummary {
        private LocalDate date;
        private int projectId;
        private int reportId;
        private float hours;
    }

    /**
     * Computed status of all approves decisions for given overtime report
     */
    public enum OvertimeApprovalCommonStatus {
        /**
         * No (not canceled) decisions at all
         */
        NO_DECISIONS,
        /**
         * Any not canceled declines found
         */
        DECLINED,
        /**
         * There is at least one not outdated, not canceled approve and no declines
         */
        APPROVED_NO_DECLINED,
        /**
         * There are some not canceled approves,
         * but all of them have been performed before last overtime item updated
         * (still only if no declines fond)
         */
        APPROVED_OUTDATED;
    }

    private int employeeId;
    private int reportId;

    private float totalHours = 0.0f;
    @Nullable
    private OffsetDateTime lastUpdate;
    @Nullable
    private OffsetDateTime lastApprove;
    @Nullable
    private OffsetDateTime lastDecline;

    private List<OvertimeDaySummary> items = new ArrayList<>();

    public OvertimeApprovalCommonStatus getCommonApprovalStatus() {
        if (lastUpdate != null) {
            if (lastDecline != null){
                return OvertimeApprovalCommonStatus.DECLINED;
            }
            if (lastApprove != null) {
                if (lastApprove.isBefore(lastUpdate)) {
                    return OvertimeApprovalCommonStatus.APPROVED_OUTDATED;
                } else {
                    return OvertimeApprovalCommonStatus.APPROVED_NO_DECLINED;
                }
            }
        }
        return OvertimeApprovalCommonStatus.NO_DECISIONS;
    }
}
