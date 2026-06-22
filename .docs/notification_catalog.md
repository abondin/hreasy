# Notification Catalog

This document lists HR Easy business notifications, their trigger moments, recipients, and business meaning.

Technical delivery settings, channel configuration, API payloads, and provider behavior belong to the owning modules, primarily `platform` and `notify-ms`.

## Overtime Notifications

| Event type | Business trigger | Business recipients | User-visible inbox | Business purpose | Status |
|------------|------------------|---------------------|--------------------|------------------|--------|
| `overtime.item_created` | An employee adds an overtime item to a report | Active managers of the employee's current project, project business account, and project department who have `overtime_view` | Yes | Tell responsible managers that an employee has reported overtime requiring attention | Implemented |
| `overtime.item_deleted` | An employee deletes an overtime item from a report | Active managers of the employee's current project, project business account, and project department who have `overtime_view` | Yes | Tell responsible managers that previously reported overtime was removed | Implemented |
| `overtime.approved` | A manager approves an employee overtime report | Report employee | Yes | Tell the employee that the overtime report was approved | Implemented |
| `overtime.declined` | A manager declines an employee overtime report | Report employee | Yes | Tell the employee that the overtime report was declined and show the manager comment when present | Implemented |

## Vacation Notifications

| Event type | Business trigger | Business recipients | User-visible inbox | Business purpose | Status |
|------------|------------------|---------------------|--------------------|------------------|--------|
| `vacation.upcoming` | A planned vacation starts within the configured upcoming-vacation threshold | Employee; active managers copied in email; configured additional email recipients copied in email | No | Remind relevant people about the upcoming vacation and attach the vacation application template | Legacy email implemented |

## Salary Notifications

| Event type | Business trigger | Business recipients | User-visible inbox | Business purpose | Status |
|------------|------------------|---------------------|--------------------|------------------|--------|
| `salary_request.implemented` | An admin marks a salary increase or bonus request as implemented | Employee who created the salary request | Yes | Tell the request creator that the request has been implemented | Implemented |
| `salary_request.rejected` | An admin rejects a salary increase or bonus request | Employee who created the salary request | Yes | Tell the request creator that the request has been rejected and show the reject reason when present | Implemented |

## Project Transfer Notifications

These notifications cover the employee current-project transfer request flow.

| Event type | Business trigger | Business recipients | User-visible inbox | Business purpose | Status |
|------------|------------------|---------------------|--------------------|------------------|--------|
| `project_transfer.request_created` | A pending project transfer request is created | Assigned approver | Yes | Tell the approver that an employee transfer request needs a decision | Implemented |
| `project_transfer.request_approved` | An eligible approver approves the transfer request and the employee project is updated | Request creator; assigned approver if another eligible approver made the decision | Yes | Tell the requester and originally assigned approver that the transfer was approved and applied | Implemented |
| `project_transfer.request_rejected` | An eligible approver rejects the transfer request | Request creator; assigned approver if another eligible approver made the decision | Yes | Tell the requester and originally assigned approver that the transfer was rejected and show the decision comment when present | Implemented |
| `project_transfer.request_canceled` | The request creator or a global current-project manager cancels a pending transfer request | Assigned approver; request creator if canceled by another employee | Yes | Tell affected participants that the pending transfer request is no longer active | Implemented |
| `project_transfer.request_expired` | The expiration job marks an old pending transfer request as expired | Request creator; assigned approver | Yes | Tell participants that no decision was made before the configured expiration period | Implemented |

## Business Rules

- Business event types must be stable and should not contain transport-specific names.
- Every implemented business notification must have a clear trigger moment and recipient rule.
- Recipient rules must be checked against the backend role and permission model. The Platform backend is the source of truth for who is allowed to see or act on protected business objects.
- A user-visible notification should create a Platform inbox entry unless the product decision explicitly says otherwise.
- External delivery channels are a delivery concern and must not change the business meaning of the notification.
- If external delivery fails, the Platform inbox remains the user-visible source of truth.
- Employee-level notification preferences are not part of the MVP business rules.
- Assessment notifications are intentionally deferred until the assessment access and participation model is finalized.

## Adding A New Business Notification

Before implementation, update this catalog with:

1. Stable `eventType`.
2. Business trigger moment.
3. Business recipient rule.
4. Role and permission model impact for recipients and linked actions.
5. Whether the user should see it in the Platform inbox.
6. Business purpose.
7. Implementation status.
