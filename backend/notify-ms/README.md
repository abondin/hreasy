# HR Easy Notify MS

Notification delivery service for HR Easy.

The service accepts normalized notification requests from HR Easy Platform, stores them in its own `notify_ms` schema, and creates channel deliveries. The first implemented provider is Yandex Messenger.

## Stack

- Java 25
- Spring Boot 4.0.x
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
  "eventType": "assessment.assigned",
  "recipient": {
    "type": "user",
    "login": "ivan.petrov@example.com",
    "employeeId": 123
  },
  "priority": "normal",
  "dedupeKey": "assessment.assigned:456:123",
  "locale": "ru",
  "title": "Assessment assigned",
  "body": "A self assessment form was assigned. Due date: 2026-06-05.",
  "data": "{\"assessmentId\":456,\"dueDate\":\"2026-06-05\"}"
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

Delivery channel settings are global for the initial implementation. Employee-level notification preferences are deferred and must not be modeled in `empl.employee` until global channel behavior is validated.

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

Flyway is disabled by default, matching platform style. Enable it with:

```yaml
spring:
  flyway:
    enabled: true
```

## Build

```shell
mvn -q -f ../pom.xml -pl notify-ms -am -DskipTests package
```
