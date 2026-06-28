package ru.abondin.hreasy.platform.service.overtime;

import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeApprovalDecisionEntry;
import ru.abondin.hreasy.platform.service.notification.BusinessNotificationEvent;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeApprovalDecisionDto;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeReportDto;

/**
 * Event emitted after an overtime report approval or decline decision has been saved.
 *
 * @param auth action initiator
 * @param employeeId overtime report employee
 * @param reportId overtime report id
 * @param period overtime report period
 * @param decisionId overtime decision id
 * @param decision overtime approval decision
 * @param approverEmployeeId employee who made the decision
 * @param comment decision comment
 */
public record OvertimeDecisionNotificationEvent(
        AuthContext auth,
        int employeeId,
        Integer reportId,
        int period,
        Integer decisionId,
        OvertimeApprovalDecisionDto.ApprovalDecision decision,
        int approverEmployeeId,
        String comment
) implements BusinessNotificationEvent {
    public static OvertimeDecisionNotificationEvent from(AuthContext auth,
                                                         int employeeId,
                                                         OvertimeReportDto report,
                                                         OvertimeApprovalDecisionEntry decision) {
        return new OvertimeDecisionNotificationEvent(
                auth,
                employeeId,
                report.getId(),
                report.getPeriod(),
                decision.getId(),
                decision.getDecision(),
                decision.getApprover(),
                decision.getComment());
    }
}
