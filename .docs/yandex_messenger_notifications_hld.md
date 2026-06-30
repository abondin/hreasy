# HLD: Notifications via Yandex Messenger

## Context

HR Easy already has a notifications concept, but today it is limited mostly to email jobs and future UI/system-event notifications. We want to add user notifications through Yandex Messenger without coupling the platform core to Yandex-specific APIs.

The proposed approach is a separate lightweight notification service with an internal HTTP API. HR Easy Platform sends normalized notification requests to this service. The service renders and delivers messages through Yandex Messenger Bot API.

If another delivery channel is needed later, for example Telegram, WhatsApp, email, SMS, or another corporate messenger, the channel-specific logic is changed or extended inside the notification service, not inside the platform business flows.

The existing `telegram` service in this repository is considered deprecated/outdated. It is a legacy Telegram Bot and should not be used as the architectural baseline for this notification service. Telegram support, if needed later, should be implemented as a redesigned provider or a separate replacement service.

## Goals

- Send HR Easy notifications to users in Yandex Messenger.
- Keep platform business modules independent from Yandex Messenger Bot API.
- Introduce a stable internal notification contract between platform and notification service.
- Support retries, idempotency, delivery status tracking, and operational diagnostics.
- Make the design extensible enough to add more delivery providers later.
- Use the current stable Java/Spring stack for new development.
- Support simple per-channel delivery policies, for example immediate messenger delivery during working hours and daily email digest.

## Non-Goals

- Replace existing email notifications in the first iteration.
- Build a full notification preference center in the first iteration.
- Implement inbound bot commands or conversational flows.
- Implement Telegram/WhatsApp providers immediately.
- Guarantee delivery if Yandex Messenger blocks private messages by user privacy settings.
- Build a complex event-streaming platform. The first implementation should stay DB-backed and simple.

## External Dependencies

Yandex Messenger integration is based on Bot API for Messenger in Yandex 360 for business:

- Bot operations are performed on behalf of a bot.
- Bot API calls are authorized with an OAuth token.
- Text messages are sent using `POST https://botapi.messenger.yandex.net/bot/v1/messages/sendText/`.
- A message can target either a private user by `login` or a group chat by `chat_id`.
- For group chats, the bot must be a member or an administrator.
- For private messages, the user must belong to the same organization and privacy settings may still block delivery.
- `payload_id` can be used for request idempotency on Yandex side.

Reference documentation:

- https://yandex.ru/dev/messenger/doc/ru/
- https://yandex.ru/dev/messenger/doc/ru/api-requests/message-send-text

## High-Level Architecture

```text
+-------------------+        HTTP         +--------------------------+
| HR Easy Platform  | ------------------> | Notification Service     |
|                   |                     |                          |
| Business events   |                     | Internal REST API        |
| User data         |                     | Template rendering       |
| Permissions       |                     | Delivery persistence     |
+-------------------+                     | Retry worker             |
                                          | Provider abstraction     |
                                          +------------+-------------+
                                                       |
                                                       | HTTPS
                                                       v
                                          +--------------------------+
                                          | Yandex Messenger Bot API |
                                          +--------------------------+
```

The platform owns business decisions: who should be notified and why. The notification service owns delivery mechanics: how the message is formatted, sent, retried, and audited.

## Target Technology Stack

The notification service should use the current stable Spring 4 generation stack:

- Java 25.
- Spring Boot 4.0.x, pinned to the latest stable patch version available at implementation time.
- Spring Framework 7 baseline through Spring Boot 4.
- Spring WebFlux.
- Spring Security for internal service authentication.
- Spring Boot Actuator.
- Spring Validation.
- PostgreSQL.
- R2DBC PostgreSQL for runtime persistence.
- Flyway with JDBC PostgreSQL driver for schema migrations.
- Testcontainers for integration tests.
- Jib for container image build, if the service is deployed the same way as other HR Easy services.

At the time of implementation, Spring Boot 4.0.5 is the latest stable Spring Boot 4.0 patch version available in Maven Central metadata. Spring Boot 4.0.x also provides first-class Java 25 support.

## Components

### HR Easy Platform

Responsibilities:

- Detect notification-worthy business events.
- Resolve recipients from platform domain data.
- Align recipient selection and linked actions with the backend role and permission model.
- Send normalized notification requests to the notification service.
- Provide stable recipient identifiers, preferably corporate email/login.
- Optionally expose employee/profile data needed by the notification service, if not included in the request.

The platform should not:

- Store Yandex OAuth tokens.
- Call Yandex Messenger Bot API directly.
- Know Yandex-specific fields such as `payload_id`, `disable_web_page_preview`, or Bot API response schemas.

### Notification Service

Responsibilities:

- Expose an internal HTTP endpoint for incoming notification requests.
- Authenticate platform requests.
- Validate payloads.
- Persist notification requests and delivery attempts.
- Render final message text from event type and payload.
- Select delivery provider.
- Send messages through Yandex Messenger Bot API.
- Retry transient failures.
- Store final delivery state and external message identifiers.
- Provide operational endpoints for health checks and delivery diagnostics.

### Yandex Messenger Provider

Responsibilities:

- Convert internal rendered messages to Yandex Bot API requests.
- Send `sendText` requests.
- Use recipient `login` or `chat_id`.
- Set `payload_id` for idempotency.
- Map Yandex responses and errors to internal delivery statuses.

## Initial Delivery Model

The first implementation should support text messages only.

Supported target types:

| Target type | Description | Yandex field |
|-------------|-------------|--------------|
| `user` | Private message to employee | `login` |
| `chat` | Message to a group chat or channel | `chat_id` |

Private user messages are the primary scenario. Chat/channel messages can be added early if there is a known HR/management group use case.

## Channel Delivery Policies

The notification service should support multiple delivery channels, but keep the model simple.

The business notification catalog is maintained separately in [notification_catalog.md](notification_catalog.md). It lists each business notification, trigger moment, recipient rules, business purpose, and current implementation status. Technical delivery settings belong to the owning modules, primarily `platform` and `notify-ms`.

The platform sends one normalized notification event. The notification service stores it and creates delivery work items according to channel policies.

Example target behavior:

| Channel | Policy | Example |
|---------|--------|---------|
| Yandex Messenger | Send every event separately during working hours | Overtime approval at 14:00 is sent immediately |
| Yandex Messenger | Defer night events until next working morning | Salary approval event at 23:30 is sent at 09:00 |
| Email | Send digest | All daily events are grouped into one email at the end of the working day |

The first version should not implement a complex rule engine. Policies can be represented as configuration and a small amount of code:

```yaml
hreasy:
  notifications:
    working-hours:
      timezone: Europe/Moscow
      start: "09:00"
      end: "18:00"
      workdays: MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY
    channels:
      yandex-messenger:
        enabled: true
        mode: business-hours-immediate
      email:
        enabled: false
        mode: daily-digest
        digest-time: "18:30"
```

### Policy Types

| Policy | Meaning |
|--------|---------|
| `immediate` | Create delivery item due now |
| `business-hours-immediate` | Create delivery item due now if current time is inside working hours, otherwise due at next working start |
| `daily-digest` | Store event and aggregate it into one recipient/channel digest at configured digest time |

For MVP, Yandex Messenger can use `business-hours-immediate`. Email digest can be designed in schema and interfaces but implemented later.

### Preference Scope For MVP

The first implementation uses global delivery settings only. Employee-level notification preferences are deferred.

Platform configuration controls whether Platform calls the notification delivery service. Notification Service configuration controls which external channels are enabled and how each channel schedules delivery.

The Platform UI inbox remains the source of truth for user-visible notifications and is not controlled by external channel settings in the MVP.

Per-employee profile settings can be introduced later after the global channel behavior is validated.

## Simplified Platform Reliability Model

Platform outbox is optional/future for this feature.

For MVP, the platform can use this simpler flow:

```text
business flow
  -> save notify.notification for Web UI inbox
  -> after successful save, call Notification Service
  -> if external call fails, keep UI notification and log warning
```

This is acceptable because the Web UI inbox remains the source of truth for user-visible notifications. Messenger delivery is a convenience channel, not the only way to see the notification.

The platform should avoid calling the notification service before the UI notification is successfully persisted.

If messenger/email delivery later becomes business-critical, add platform outbox then.

## Database Ownership and Schemas

Production target: HR Easy Platform and Notification Service use separate PostgreSQL databases.

Test and lightweight environments may run both services in one PostgreSQL database. Because of that, every service-owned schema name must be globally unique across all HR Easy services.

Current platform schemas include, for example, `notify`, `empl`, `sec`, `vac`, and other platform-owned schemas. The new notification service must not use `notify`, because it already belongs to platform UI notifications.

Recommended schema for the new service:

```text
notify_ms
```

Rules:

- Platform keeps using `notify.notification` for Web UI inbox.
- Notification Service owns only tables under `notify_ms`.
- No cross-service foreign keys, even if both services run in the same physical database.
- Shared test DB is allowed only through schema separation.
- Flyway migrations for the new service must create and manage only `notify_ms`.

## Reliability Pattern

The long-term reliability pattern can still be transactional outbox, but it is not required for MVP on the platform side.

There are two related but different reliability boundaries:

1. Platform side: `notify.notification` is persisted for Web UI inbox. External service call is best-effort.
2. Notification service side: accepted notification is persisted before any provider call, then a worker sends it to Yandex Messenger and records the attempt.

For the first implementation, the notification service must have its own database from the start. Without persistence, the service cannot provide deduplication, restart-safe retries, delivery audit, or asynchronous `202 Accepted` behavior.

The platform-side outbox can be introduced in stages:

- MVP integration can call the notification service directly from application code after the business transaction commits.
- For important workflows, platform should then write notification events to a platform outbox table inside the same transaction as the business change.
- A platform outbox publisher sends those events to the notification service and marks them as published after successful `202 Accepted`.

The notification service still keeps its own persistence even if platform outbox exists. Platform outbox guarantees that an event is not lost before it reaches the notification service. Notification service persistence guarantees that accepted events are not lost before they reach Yandex Messenger.

### Platform Outbox Draft

If implemented in platform, the outbox table can be generic and not Yandex-specific:

| Column | Description |
|--------|-------------|
| `id` | Outbox event id |
| `event_type` | Stable event code, for example `overtime.item_created` |
| `aggregate_type` | Business aggregate type, for example `overtime` |
| `aggregate_id` | Business aggregate id |
| `recipient_employee_id` | Optional employee recipient |
| `dedupe_key` | Business idempotency key |
| `payload` | JSON payload for notification service |
| `status` | `new`, `publishing`, `published`, `failed`, `retry_scheduled` |
| `attempt_count` | Publish attempts count |
| `next_attempt_at` | Next publish time |
| `created_at` | Creation timestamp |
| `updated_at` | Last update timestamp |

The platform outbox publisher should send events to `POST /api/v1/notifications` and use `dedupe_key` as the notification service `dedupeKey`.

For MVP, this table is deferred. It should be added only if losing an external messenger/email delivery creates operational risk.

## Internal API Draft

### Create Notification

```http
POST /api/v1/notifications
Authorization: Bearer <internal-service-token>
Content-Type: application/json
Idempotency-Key: <optional-request-id>
```

```json
{
  "eventType": "overtime.item_created",
  "recipient": {
    "type": "user",
    "login": "ivan.petrov@example.com",
    "employeeId": 123
  },
  "priority": "normal",
  "dedupeKey": "overtime.item_created:456:789:123",
  "locale": "ru",
  "title": "Overtime reported",
  "body": "An employee reported overtime for period 202605.",
  "data": {
    "overtimeReportId": 456,
    "overtimeItemId": 789,
    "period": 202605,
    "actorEmployeeId": 42
  }
}
```

### Response

For asynchronous delivery:

```http
202 Accepted
```

```json
{
  "notificationId": "01JZ8H9ZVG8MZ3W4D4NFY3ATNR",
  "status": "accepted"
}
```

The endpoint should return `202 Accepted` after validation and persistence. Actual delivery happens in a background worker.

## Request Fields

| Field | Required | Description |
|-------|----------|-------------|
| `eventType` | Yes | Stable event code used for routing and template selection |
| `recipient` | Yes | Target user or chat |
| `priority` | No | `low`, `normal`, `high`; default is `normal` |
| `dedupeKey` | Yes | Business idempotency key |
| `locale` | No | Message locale; default can be `ru` |
| `title` | No | Short title for templates or fallback direct sending |
| `body` | No | Pre-rendered text for MVP |
| `data` | No | Event-specific structured data |

For MVP, `body` can be required to avoid building a full template system immediately. After the first event set is clear, templates can move into the notification service.

## Delivery State Machine

```text
accepted
  -> queued
  -> deferred
  -> sending
  -> sent

deferred
  -> queued

sending
  -> retry_scheduled
  -> sending

sending
  -> failed_permanent

retry_scheduled
  -> retry_exhausted
```

Suggested statuses:

| Status | Meaning |
|--------|---------|
| `accepted` | Request was validated and persisted |
| `queued` | Waiting for worker processing |
| `deferred` | Delivery is intentionally delayed by channel policy |
| `sending` | Delivery attempt is in progress |
| `sent` | Provider accepted the message |
| `retry_scheduled` | Temporary failure, retry planned |
| `failed_permanent` | Non-retryable provider or validation failure |
| `retry_exhausted` | Retry limit reached |

## Persistence Draft

### `notify_ms.notification`

| Column | Description |
|--------|-------------|
| `id` | Internal notification id |
| `event_type` | Event code |
| `recipient_type` | `user` or `chat` |
| `recipient_login` | Yandex/user login for private messages |
| `recipient_chat_id` | Yandex chat id for group messages |
| `employee_id` | Optional HR Easy employee id |
| `dedupe_key` | Unique business idempotency key |
| `priority` | Notification priority |
| `status` | Current delivery status |
| `title` | Optional title |
| `body` | Rendered or supplied message text |
| `data` | Event-specific JSON payload |
| `created_at` | Creation timestamp |
| `updated_at` | Last update timestamp |

Unique constraint:

```text
(dedupe_key)
```

### `notify_ms.notification_delivery`

This table is the DB-backed delivery queue. It allows one stored event to produce multiple channel deliveries with different due times.

| Column | Description |
|--------|-------------|
| `id` | Delivery id |
| `notification_id` | Parent notification |
| `channel` | `yandex_messenger`, `email`, etc. |
| `status` | `queued`, `deferred`, `sending`, `sent`, `retry_scheduled`, `failed_permanent`, `retry_exhausted` |
| `due_at` | Earliest time when this delivery can be processed |
| `digest_key` | Optional grouping key for digest deliveries |
| `attempt_count` | Attempts count |
| `error_count` | Failed attempts count |
| `max_attempts` | Delivery attempt limit copied from channel policy at creation time |
| `last_success_at` | Last successful provider delivery timestamp |
| `last_attempt_at` | Last attempt timestamp |
| `next_attempt_at` | Next retry timestamp |
| `external_message_id` | Last provider message id, if successfully sent |
| `provider_payload_id` | Idempotency id sent to provider |
| `provider_status_code` | Last provider HTTP/status code |
| `last_error_code` | Last normalized provider/internal error code |
| `last_error_message` | Last provider/internal error message |
| `created_at` | Creation timestamp |
| `updated_at` | Last update timestamp |

For Yandex Messenger, each notification creates one delivery row with `channel = yandex_messenger`.

For email digest, the service can store notification rows immediately and create or update digest deliveries grouped by recipient/date/channel. The digest implementation can be added later without changing the platform contract.

### Delivery Attempts and Error Limit

The first version should not keep a separate attempt history table. To stay simple, the service stores only the current delivery state, counters, last success, and last error directly in `notify_ms.notification_delivery`.

`notification_delivery` stores the current state and counters:

- `attempt_count`: every provider send attempt.
- `error_count`: failed attempts only.
- `max_attempts`: limit after which the service stops trying.
- `last_success_at`: set when provider accepts the message.
- `last_error_code` and `last_error_message`: latest failure summary for operational screens/logs.

Sending must be blocked when:

```text
error_count >= max_attempts
```

In that case the delivery status becomes `retry_exhausted`, and workers must not pick it up again unless an operator or admin action explicitly resets/requeues it.

Suggested default limits:

| Channel | Max attempts | Notes |
|---------|--------------|-------|
| `yandex_messenger` | 5 | Covers temporary provider/network issues |
| `email` | 3 | Digest can be recreated or resent manually later |

Permanent errors should not consume all retry attempts. They should move the delivery directly to `failed_permanent`.

Examples of permanent errors:

- Missing recipient address/login.
- Invalid recipient format.
- User is outside Yandex organization.
- Bot is not allowed to send to the target chat/user.
- Provider returns authorization/configuration error.

Examples of retryable errors:

- Provider HTTP 429.
- Provider HTTP 5xx.
- Network timeout.
- Temporary DNS/TLS/connection failure.

## Idempotency

Two idempotency levels are needed:

1. Platform to notification service: `dedupeKey` or `Idempotency-Key`.
2. Notification service to Yandex Messenger: `payload_id`.

`dedupeKey` should be based on business identity, not on transport retries. Examples:

- `overtime.item_created:<reportId>:<itemId>:<managerEmployeeId>`
- `overtime.item_deleted:<reportId>:<itemId>:<managerEmployeeId>`
- `overtime.approved:<reportId>:<employeeId>:<decisionId>`
- `overtime.declined:<reportId>:<employeeId>:<decisionId>`
- `vacation.upcoming:<vacationId>:<employeeId>`
- `salary_request.implemented:<salaryRequestId>:<creatorEmployeeId>:<implementedAt>`
- `salary_request.rejected:<salaryRequestId>:<creatorEmployeeId>:<implementedAt>`
- `salary_request.approval_required:<requestId>:<approverEmployeeId>`

If the platform resends the same notification, the notification service returns the existing `notificationId` instead of creating a duplicate delivery.

## Error Handling and Retries

Retryable cases:

- Network timeout.
- Temporary DNS/TLS/connection failure.
- HTTP 429 rate limit.
- HTTP 5xx from Yandex Bot API.

Permanent failure cases:

- Missing recipient login/chat id.
- Invalid request payload.
- Bot is not a member of the target chat.
- User is outside the organization.
- User privacy settings block private messages.
- Invalid or expired OAuth token, after operational alerting.

Suggested retry policy:

| Attempt | Delay |
|---------|-------|
| 1 | immediate |
| 2 | 1 minute |
| 3 | 5 minutes |
| 4 | 15 minutes |
| 5 | 1 hour |

After max attempts, set `retry_exhausted`.

## Security

- The notification service HTTP API must be internal-only.
- Platform requests must be authenticated with a service token, mTLS, or signed HMAC.
- Yandex OAuth token must be stored only in notification service secrets/configuration.
- Logs must not contain OAuth tokens or full provider authorization headers.
- Message bodies may contain HR-sensitive information, so logs should store message text carefully. Prefer storing body in DB for audit but avoid dumping it to application logs.
- Admin/diagnostic endpoints must require internal admin access.

## Observability

Metrics:

- Accepted notifications count by `eventType`.
- Sent notifications count by provider.
- Failed notifications count by provider and reason.
- Retry count.
- Delivery latency.
- Queue depth.

Logs:

- `notificationId`
- `eventType`
- `dedupeKey`
- provider
- final status
- provider error code/description

Health checks:

- Application liveness.
- DB connectivity.
- Queue/worker readiness.
- Optional provider configuration check without sending a message.

## Configuration

Suggested environment variables:

```text
HREASY_NOTIFICATIONS_HTTP_TOKEN=<internal token>
HREASY_NOTIFICATIONS_YANDEX_ENABLED=true
HREASY_NOTIFICATIONS_YANDEX_BOT_TOKEN=<oauth token>
HREASY_NOTIFICATIONS_YANDEX_BASE_URL=https://botapi.messenger.yandex.net/bot/v1
HREASY_NOTIFICATIONS_RETRY_MAX_ATTEMPTS=5
```

## Candidate First Events

These events should be confirmed before implementation:

| Event | Recipient | Notes |
|-------|-----------|-------|
| `overtime.item_created` | Project, BA, and department managers with `overtime_view` | Employee added an overtime item |
| `overtime.item_deleted` | Project, BA, and department managers with `overtime_view` | Employee deleted an overtime item |
| `overtime.approved` | Employee | Manager approved overtime report |
| `overtime.declined` | Employee | Manager declined overtime report |
| `vacation.upcoming` | Employee, managers | Existing email job can later reuse notification service |
| `salary_request.implemented` | Request creator | Salary manager implemented a salary increase or bonus request |
| `salary_request.rejected` | Request creator | Salary manager rejected a salary increase or bonus request |
| `salary_request.approval_required` | Approver | High-value workflow notification |
| `support.request.created` | Support group/chat | Could be sent to chat instead of private user |

MVP should start with one or two high-signal events to validate delivery and user mapping.

## Sequence: Async Notification Delivery

```text
HR Easy Platform
  -> Notification Service: POST /api/v1/notifications
Notification Service
  -> DB: insert notification if dedupeKey is new
Notification Service
  -> HR Easy Platform: 202 Accepted
Notification Worker
  -> DB: load queued notification
Notification Worker
  -> Yandex Bot API: POST /messages/sendText
Yandex Bot API
  -> Notification Worker: ok/message_id or error
Notification Worker
  -> DB: update status and attempt
```

## Deployment Options

### Option A: New Standalone Service

Pros:

- Clear ownership boundary.
- Can be deployed and scaled independently.
- Keeps provider dependencies outside platform.

Cons:

- Requires separate deployment, configuration, monitoring, and database/schema ownership.

### Option B: New Module Inside Existing Platform Runtime

Pros:

- Faster initial implementation.
- Reuses existing DB, auth, logging, and deployment.

Cons:

- Weaker boundary.
- Higher risk that Yandex-specific logic leaks into platform business code.
- Harder to replace with another channel service later.

Recommendation: start with a standalone service if deployment overhead is acceptable. If speed is more important, implement an internal module with the same HTTP contract and keep the provider abstraction strict, so it can be extracted later.

## Open Questions

- Do all active employees have a Yandex 360 login in HR Easy today?
- Is employee email always equal to Yandex Messenger login?
- Should users opt in/out of messenger notifications?
- Which notifications are allowed to contain sensitive HR data?
- Should failed messenger delivery fall back to email?
- Do we need chat/channel notifications in MVP or only private user messages?
- Where should templates live: platform, notification service, or DB-managed admin UI?
- Do we need delivery status visible in HR Easy admin UI in the first release?

## MVP Proposal

1. Create notification service with `POST /api/v1/notifications`.
2. Support only text messages and `recipient.type = user`.
3. Require `body`, `eventType`, `recipient.login`, and `dedupeKey`.
4. Persist notifications and delivery attempts.
5. Send through Yandex `sendText` with OAuth token and `payload_id`.
6. Implement retry policy for transient failures.
7. Add first platform integration for one selected event.
8. Add operational metrics and basic delivery log.

This keeps the first release small while preserving the architecture needed for future providers and richer notification scenarios.
