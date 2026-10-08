# Notification Delivery Architecture

HR Easy Platform creates business notifications and stores the employee's web inbox. Notify MS accepts external delivery requests and sends their supplied text through Yandex Messenger. The [notification catalog](notification_catalog.md) describes implemented events and recipient rules; the [Notify MS README](../backend/notify-ms/README.md) describes its HTTP API and configuration.

## Service ownership

| Component | Responsibility | Database schema |
| --- | --- | --- |
| Platform | Business triggers, permissions, recipients, localized message text, web inbox | `notify` |
| Notify MS | Request deduplication, delivery scheduling, provider calls, retries, retention | `notify_ms` |
| Yandex Messenger provider | Convert delivery records to authenticated `sendText` requests | Owned by Notify MS |

Services can use separate PostgreSQL databases. Development and test environments can share a physical database while keeping schemas separate. There are no cross-service foreign keys.

Platform does not store the Yandex OAuth token. Notify MS uses the configured service token to authenticate incoming requests and its OAuth token for Yandex calls. The Notify MS API must be reachable only by trusted internal callers.

## Request flow

```text
Platform business event
  -> resolve recipients and render localized message
  -> save Platform inbox entry
  -> call Notify MS with a dedupe key and rendered body
Notify MS
  -> persist request and enabled channel deliveries
  -> return 202 Accepted
Delivery worker
  -> claim due deliveries
  -> send Yandex message
  -> persist success or retry/failure state
```

For employee notifications, the inbox is saved before the external HTTP call. External publication is best effort: a failed call is logged, without removing the inbox entry. Platform has no durable outbox or automatic replay of failed HTTP publications. Inbox persistence is not an end-to-end messenger delivery guarantee.

Notify MS returns `202 Accepted` after persistence; acceptance does not mean that Yandex has received the message. Platform supplies the final localized `body`; Notify MS does not have an event-template engine. The optional `data` field is a string containing JSON, as shown in the API example.

## Deduplication

`dedupeKey` identifies a business notification and is unique in `notify_ms.notification`. A repeated request returns the existing numeric notification ID. An `Idempotency-Key` HTTP header is not used.

The Yandex `payload_id` is derived from the dedupe key and channel and remains the same across retries. Business handlers choose event-specific keys: salary implementation notifications include the saved implementation timestamp so resetting and implementing a request again produces a new notification.

## Channels and scheduling

Yandex Messenger is the implemented external channel. It sends text messages to `recipient.type = user` using `login`, or to `recipient.type = chat` using `chatId`. Missing target identifiers produce a permanent delivery failure.

Settings are global. `hreasy.notifications.channels.yandex-messenger.enabled` controls creation of Yandex delivery rows; it does not control the Platform inbox. Enabling email delivery fails startup because the email digest worker is not implemented. Upcoming-vacation emails are handled separately by Platform.

Yandex delivery modes:

- `immediate`: the initial delivery is due immediately.
- `business-hours-immediate`: the initial delivery is due now during configured working hours, otherwise at the next working start.

The bundled application configuration uses weekdays, `08:00`–`19:00`, and `Europe/Moscow`. Working hours schedule the initial delivery; retries use their retry deadlines directly. Individual notification preferences, digest delivery, and per-event channel overrides are not supported.

## Delivery state and retries

`notify_ms.notification_delivery` is the persisted queue. The worker claims due `queued`, `deferred`, or `retry_scheduled` rows with `FOR UPDATE SKIP LOCKED` and marks them `sending`.

| Status | Meaning |
| --- | --- |
| `queued` | Ready for delivery at `due_at` |
| `deferred` | Initial delivery waits for working hours |
| `sending` | Claimed by the worker |
| `sent` | Provider accepted the message |
| `retry_scheduled` | A transient failure has a retry deadline |
| `failed_permanent` | Delivery cannot be retried automatically |
| `retry_exhausted` | Failure limit reached |

HTTP 429, HTTP 5xx, and request/network failures are retried. Other provider HTTP errors, missing tokens, and missing recipient identifiers are permanent failures. Retry delays after consecutive errors are one minute, five minutes, fifteen minutes, then one hour. The default Yandex failure limit is five.

Delivery rows store attempt/error counters, provider message ID, last success, last attempt, and last error. There is no separate attempt-history table. Worker defaults are a ten-second interval and a batch of twenty rows.

A delivery left in `sending` by a process crash is not automatically reclaimed by the current worker. Such rows require operational investigation; durable storage alone does not guarantee automatic recovery of every in-flight attempt.

## Operations

Both services run daily retention jobs with a default age of 365 days. Notify MS deletes deliveries belonging to expired notification records.

Configure tokens through deployment secrets. Logs must not expose Bearer or OAuth tokens. Use notification and delivery IDs to investigate provider failures. Queue state and failure details are stored in the service database; there is no dedicated delivery-management UI.

For provider requests and access requirements, consult the [Yandex Messenger Bot API documentation](https://yandex.ru/dev/messenger/doc/ru/).
