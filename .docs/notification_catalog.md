# Notification Catalog

This document lists HR Easy business notifications, their trigger moments, recipients, and business meaning.

Technical delivery settings, channel configuration, API payloads, and provider behavior belong to the owning modules, primarily `platform` and `notify-ms`.

## Notifications

| Event type | Business trigger | Business recipients | User-visible inbox | Business purpose | Status |
|------------|------------------|---------------------|--------------------|------------------|--------|
| `overtime.item_created` | An employee adds an overtime item to a report | Active managers of the employee's current project, project business account, and project department who have `overtime_view` | Yes | Tell responsible managers that an employee has reported overtime requiring attention | Implemented |
| `overtime.item_deleted` | An employee deletes an overtime item from a report | Active managers of the employee's current project, project business account, and project department who have `overtime_view` | Yes | Tell responsible managers that previously reported overtime was removed | Implemented |
| `overtime.approved` | A manager approves an employee overtime report | Report employee | Yes | Tell the employee that the overtime report was approved | Implemented |
| `overtime.declined` | A manager declines an employee overtime report | Report employee | Yes | Tell the employee that the overtime report was declined and show the manager comment when present | Implemented |
| `vacation.upcoming` | A planned vacation is approaching | Employee; managers if required by HR policy | Planned reuse | Remind relevant people about the upcoming vacation | Candidate |
| `salary_request.approval_required` | A salary request is waiting for approval | Approver employee | Planned | Tell the approver that a salary request needs a decision | Candidate |
| `support.request.created` | A support request is created | Support group or responsible support target | Optional | Notify the support side about a new employee request | Candidate |

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
