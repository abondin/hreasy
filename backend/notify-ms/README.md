# HR Easy Notify MS

Notification delivery service for HR Easy.

The service accepts notification requests from HR Easy Platform, stores them in its own `notify_ms` schema, and creates channel deliveries. Yandex Messenger is the implemented external channel. Platform supplies the localized message text.

## Stack

- Java 25
- Spring Boot 4 (version pinned in `../parent/pom.xml`)
- Spring WebFlux
- Spring Security
- Spring Data R2DBC
- PostgreSQL
- Flyway

## Database

The service owns only the `notify_ms` schema.

Platform UI notifications stay in platform schema `notify`, especially `notify.notification`.

Production deployments should use a separate database for this service. Test environments may share one physical PostgreSQL database with platform as long as schemas stay separated.

## API

```http
POST /api/v1/notifications
Authorization: Bearer <hreasy.notifications.http-token>
Content-Type: application/json
```

```json
{
  "eventType": "salary_request.implemented",
  "recipient": {
    "type": "user",
    "login": "alex.morgan@example.test",
    "employeeId": 123
  },
  "priority": "normal",
  "dedupeKey": "salary_request.implemented:456:123:2026-10-06T12:00:00Z",
  "locale": "ru",
  "title": "Salary request implemented",
  "body": "Salary request for Taylor Reed was implemented for October 2026.",
  "data": "{\"salaryRequestId\":456,\"employeeId\":201}"
}
```

Successful response:

```http
202 Accepted
```

```json
{
  "notificationId": 1,
  "status": "accepted"
}
```

`eventType`, `recipient`, `dedupeKey`, and `body` are required. `data` is an optional string containing JSON, not a JSON object. Repeated `dedupeKey` values return the existing notification ID. `202 Accepted` confirms persistence, not provider delivery.

## Configuration

```yaml
hreasy:
  db:
    host: localhost
    port: 5432
    database: hreasy
    username: hreasy
    password: hreasy
  notifications:
    http-token: local-dev-token
    channels:
      yandex-messenger:
        enabled: true
        oauth-token: "<YANDEX_OAUTH_TOKEN>"
```

`hreasy.notifications.http-token` is required. The service fails startup when it is blank.

Email digest configuration is reserved for future work and must stay disabled until the digest worker is implemented.

### Channel Configuration

Delivery channel settings are global. Employee-level notification preferences are not supported.

Current channel semantics:

| Channel | Meaning | Current status | Owner |
|---------|---------|----------------|-------|
| `yandex_messenger` | Private or chat message through Yandex Messenger Bot API | Implemented | notify-ms |
| `email` | Email delivery or digest | Reserved, not implemented | notify-ms |

Global channel rules:

| Rule | Decision |
|------|----------|
| Yandex Messenger disabled | Do not create Yandex Messenger delivery work items |
| Email disabled | Do not create email delivery work items |
| Email enabled | Startup must fail until digest delivery is implemented |
| Per-employee channel preferences | Deferred |
| Per-event channel override | Deferred until there is more than one implemented external channel |

Platform UI inbox notifications are not controlled by notify-ms channel settings. The Platform inbox remains the user-visible source of truth; notify-ms owns only external delivery attempts.

The shared custom Flyway configuration runs the commands in `hreasy.db.flyway-commands` at startup; the default is `migrate`. Configure `hreasy.db.*` for the service database. The `spring.flyway.enabled` setting does not control this custom startup path.

## Build

```shell
mvn -q -f backend/pom.xml -pl notify-ms -am -DskipTests package
```

Run this command from the monorepository root. The service listens on port `8083` by default. Configure `hreasy.notifications.http-token` and database credentials before starting the executable JAR.

See [notification architecture](../../.docs/yandex_messenger_notifications_hld.md) for scheduling, retries, and reliability limits, and the [notification catalog](../../.docs/notification_catalog.md) for business events.
