# Salary Request Notifications

This document captures the business decision for salary request notifications.

## User Story

As an employee who created a salary increase or bonus request, I want to receive a notification when the request is
implemented or rejected.

## Role Model Decision

The Platform backend role model is the source of truth.

Salary request visibility is controlled by `SalarySecurityValidator`:

- the employee who created the request can view that request;
- users with `admin_salary_request` can view and administer any request;
- users with `approve_salary_request` can view requests for accessible budgeting business accounts.

For this notification, the recipient must be the original request creator (`salary_request.created_by`), not the target
employee (`salary_request.employee_id`).

Reason: the target employee may not have permission to see the salary request. Sending the notification to the target
employee would bypass the backend salary request visibility model and could expose sensitive compensation workflow data.

## Events

| Event type | Trigger | Recipient | Inbox | External delivery |
|------------|---------|-----------|-------|-------------------|
| `salary_request.implemented` | `AdminSalaryRequestService.markAsImplemented` successfully saves implementation state | Request creator (`created_by`) | Yes | Best effort through `notify-ms` |
| `salary_request.rejected` | `AdminSalaryRequestService.reject` successfully saves rejected state | Request creator (`created_by`) | Yes | Best effort through `notify-ms` |

## Message Content

Messages should be status-oriented and must avoid salary amounts in the first implementation.

Recommended content:

- implemented: salary request or bonus request for employee `<employeeDisplayName>` was implemented for period `<period>`;
- rejected: salary request or bonus request for employee `<employeeDisplayName>` was rejected; include reject reason if present.

The implementation should use existing period formatting rules from `MapperBase.formatPeriod`.

## Context Payload

Recommended context fields:

| Field | Description |
|-------|-------------|
| `eventType` | Stable event type |
| `salaryRequestId` | Request id |
| `employeeId` | Target employee id from the request |
| `employeeDisplayName` | Target employee display name |
| `requestType` | Salary request type code |
| `requestPeriod` | Requested salary request period |
| `implementationPeriod` | Actual implementation period, if available |
| `implementationState` | `IMPLEMENTED` or `REJECTED` |
| `implementedByEmployeeId` | Employee who changed implementation state |
| `rescheduledToNewPeriod` | Optional period when rejected request was rescheduled |

## Open Implementation Notes

- Rejection with rescheduling still sends `salary_request.rejected` for the original request.
- If the notification later needs to link to the rescheduled request, `AdminSalaryRequestService.rescheduleIfRequired`
  should return the new request id instead of discarding it.
- The notification should follow the existing Platform notification architecture:
  event record, `BusinessNotificationHandler`, `NotificationPlan`, Platform inbox persistence, and best-effort
  `notify-ms` delivery.
