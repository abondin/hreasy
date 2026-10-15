# Salary Request Notifications

Salary request creators receive inbox notifications when their requests are implemented or rejected. External delivery is best effort through Notify MS. The [notification catalog](notification_catalog.md) lists all implemented business events.

## User Story

As an employee who created a salary increase or bonus request, I want to receive a notification when the request is
implemented or rejected.

## Recipient access

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

Messages describe the request status without including salary amounts.

Message content:

- implemented: salary request or bonus request for employee `<employeeDisplayName>` was implemented for period `<period>`;
- rejected: salary request or bonus request for employee `<employeeDisplayName>` was rejected; include reject reason if present.

Periods are formatted by `MapperBase.formatPeriod`.

## Context Payload

Context fields:

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

## Deduplication

The inbox dedupe key must identify a saved implementation-state change, not just the salary request. Include
`implementedAt` in the key:

- `salary_request.implemented:<salaryRequestId>:<creatorEmployeeId>:<implementedAt>`;
- `salary_request.rejected:<salaryRequestId>:<creatorEmployeeId>:<implementedAt>`.

Reason: implementation can be reset and then saved again for the same request. The second saved implementation is a new
business notification and must not reuse the first inbox `client_uuid`.

## Implementation

- Rejection with rescheduling still sends `salary_request.rejected` for the original request.
- The context includes the rescheduled period, but not the new request ID.
- The notification follows the Platform notification architecture:
  event record, `BusinessNotificationHandler`, `NotificationPlan`, Platform inbox persistence, and best-effort
  `notify-ms` delivery.
