package ru.abondin.hreasy.platform.service.admin.employee;

import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.repo.employee.projecttransfer.ProjectTransferRequestEntry;
import ru.abondin.hreasy.platform.service.notification.BusinessNotificationEvent;

import java.time.OffsetDateTime;

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
 * @param createdAt request creation timestamp
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
        OffsetDateTime createdAt,
        Integer actionEmployeeId,
        String comment
) implements BusinessNotificationEvent {
    public static ProjectTransferRequestNotificationEvent created(ProjectTransferRequestEntry request,
                                                                  Integer actionEmployeeId) {
        return from(request, Kind.CREATED, actionEmployeeId);
    }

    public static ProjectTransferRequestNotificationEvent closed(ProjectTransferRequestEntry request,
                                                                 short state,
                                                                 Integer actionEmployeeId) {
        return from(request, Kind.fromRequestState(state), actionEmployeeId);
    }

    public static ProjectTransferRequestNotificationEvent expired(ProjectTransferRequestEntry request) {
        return from(request, Kind.EXPIRED, null);
    }

    public static ProjectTransferRequestNotificationEvent from(ProjectTransferRequestEntry request,
                                                               Kind kind,
                                                               @Nullable Integer actionEmployeeId) {
        return new ProjectTransferRequestNotificationEvent(
                request.getId(),
                kind,
                request.getEmployeeId(),
                request.getFromProjectId(),
                request.getToProjectId(),
                request.getCreatedBy(),
                request.getApproverEmployeeId(),
                request.getCreatedAt(),
                actionEmployeeId,
                request.getDecisionComment());
    }

    public enum Kind {
        CREATED,
        APPROVED,
        REJECTED,
        CANCELED,
        EXPIRED;

        public static Kind fromRequestState(short state) {
            if (state == ProjectTransferRequestEntry.STATE_APPROVED) {
                return APPROVED;
            }
            if (state == ProjectTransferRequestEntry.STATE_REJECTED) {
                return REJECTED;
            }
            if (state == ProjectTransferRequestEntry.STATE_CANCELED) {
                return CANCELED;
            }
            throw new IllegalArgumentException("Unsupported project transfer request notification state: " + state);
        }
    }
}
