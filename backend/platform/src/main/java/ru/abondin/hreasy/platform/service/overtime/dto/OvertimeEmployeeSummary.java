package ru.abondin.hreasy.platform.service.overtime.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Monthly overtime summary for an employee; items are grouped by date and project, not workstream.")
public class OvertimeEmployeeSummary {

    /**
     * Overtimes for one employee, aggregated for given day and project
     */
    @Data
    @Schema(description = "Overtime total for one employee, date and project.")
    public static class OvertimeDaySummary {
        @Schema(description = "Overtime calendar date.", example = "2026-09-15")
        private LocalDate date;
        @Schema(description = "HR Easy project identifier.", example = "301")
        private int projectId;
        @Schema(description = "HR Easy overtime report identifier.")
        private int reportId;
        @Schema(description = "Total overtime hours for this employee, date and project, across all workstreams.", example = "4.5")
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

    @Schema(description = "HR Easy employee identifier.", example = "101")
    private int employeeId;
    @Schema(description = "HR Easy overtime report identifier.")
    private int reportId;

    @Schema(description = "Total overtime hours in the monthly report.")
    private float totalHours = 0.0f;
    @Nullable
    @Schema(description = "Latest creation timestamp among current, non-deleted overtime items.", nullable = true)
    private OffsetDateTime lastUpdate;
    @Nullable
    @Schema(description = "Latest non-canceled approval decision time.", nullable = true)
    private OffsetDateTime lastApprove;
    @Nullable
    @Schema(description = "Latest non-canceled decline decision time.", nullable = true)
    private OffsetDateTime lastDecline;

    @Schema(description = "Overtime totals grouped by date and project.")
    private List<OvertimeDaySummary> items = new ArrayList<>();

    @Schema(description = "Computed approval status: NO_DECISIONS when there are no applicable decisions; "
            + "DECLINED when a non-canceled decline exists; APPROVED_NO_DECLINED when approval is current; "
            + "APPROVED_OUTDATED when approval predates the latest item.", accessMode = Schema.AccessMode.READ_ONLY)
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
