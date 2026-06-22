package ru.abondin.hreasy.platform.service.admin.employee;

import ru.abondin.hreasy.platform.service.notification.BusinessNotificationEvent;

/**
 * Event emitted after a project transfer request state changes.
 *
 * @param requestId project transfer request id
 * @param kind notification kind
 * @param employeeId transferred employee
 * @param fromProjectId source project
 * @param toProjectId target project
 * @param createdByEmployeeId employee who created the request
 * @param approverEmployeeId assigned approver
 * @param actionEmployeeId employee who performed the action, or null for system actions
 * @param comment decision/cancel comment, if present
 */
public record ProjectTransferRequestNotificationEvent(
        Integer requestId,
        Kind kind,
        Integer employeeId,
        Integer fromProjectId,
        Integer toProjectId,
        Integer createdByEmployeeId,
        Integer approverEmployeeId,
        Integer actionEmployeeId,
        String comment
) implements BusinessNotificationEvent {
    public enum Kind {
        CREATED,
        APPROVED,
        REJECTED,
        CANCELED,
        EXPIRED
    }
}
