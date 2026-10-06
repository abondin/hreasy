# External Read-only API

## Context

HR Easy provides a server-to-server API for systems such as `pi-backend`. The API exposes HR data without creating a web session and performs every request on behalf of a configured HR Easy user.

The URL prefix is `/external/api/v1`. All operations are read-only.

## Goals

- Expose the employee list available through the basic employee API.
- Expose overtime totals by project and workstream for a requested month.
- Expose annual resource allocation analytics.
- Expose basic project information.
- Identify both the calling external system and the HR Easy user on whose behalf it operates.
- Leave external routing, source-IP restrictions, and traffic limits to the deployment load balancer.
- Preserve the current HR Easy permission and project-scope rules.

## Non-goals

- Write operations.
- A UI or database tables for managing integrations.
- An HR Easy endpoint that issues tokens.
- Reimplementation of employee, allocation, or project queries.
- Reverse-proxy routing, IP filtering, rate limiting, pagination, or a generic integration framework in v1.

## API Contract

The API identifies employees by email and projects, workstreams, and business accounts
by configured external keys. Responses do not expose database IDs.

| Endpoint | Parameters | Response |
|---|---|---|
| `GET /external/api/v1/employees` | none | Active employee profiles |
| `GET /external/api/v1/employees/avatar` | required `email` | Active employee PNG, or 404 |
| `GET /external/api/v1/overtimes/{period}` | ISO `YYYY-MM` | Monthly overtime reports |
| `GET /external/api/v1/resource-allocations/analytics/{year}` | calendar year | Annual allocation cells |
| `GET /external/api/v1/projects` | none | Projects with active workstreams |

### Business identifiers

- Employees use email exactly as stored in the database, with no output normalization.
- Projects use their configured `externalId`.
- Workstreams use `(project.externalId, workstream.externalId)`. A deleted workstream
  and its replacement with the same key deliberately represent the same external entity.
- Business accounts use their configured `externalId`, unique when populated.
- Project references contain `name`, `externalId`, and nullable `ba`.
- Workstream and BA references contain exactly `name` and `externalId`.
- Unconfigured keys are returned as `externalId: null`, never replaced by internal IDs.
  Such entities have no reliable integration key; their names are display labels.
- Names may change without changing an external key. An email change changes the employee key.

Business-account keys are edited in the existing admin BA form and retained in BA history.

### Employee profiles and avatars

The employee response contains only `email`, `displayName`, `department` (name),
`position` (name), nullable `currentProject` (project reference), and `hasAvatar`.
It contains no internal IDs, birthday, sex, messenger data, office/workplace, skills, or ratings.
Dismissed employees are never included.

Avatar lookup matches the complete email case-insensitively and ignores surrounding whitespace.
URL-encode the query value, especially plus signs. Missing/blank email returns 400.
A dismissed or missing employee, or a missing image, returns 404 without a fallback image.

### Overtimes

Each report contains `employeeEmail`, `period` in `YYYY-MM`, `totalHours`,
`lastUpdate`, `lastApprove`, `lastDecline`, `commonApprovalStatus`, and `items`.
Historical reports of dismissed employees remain available by email only, without profile data.

Each item contains `date`, `project`, nullable `workstream`, and `hours`.
The report key is `(employeeEmail, period)`; the item key within a report is
`(date, project.externalId, workstream.externalId)` when configured.

Items of deleted/replacement workstreams with the same configured key are summed.
The active workstream supplies the display name; if none is active, the last deleted
workstream supplies it. Workstreams without configured keys remain separate.
A null workstream is a separate project-level dimension, not a total over workstreams.

`lastUpdate` means
the latest creation timestamp among remaining non-deleted items, not a modification cursor.
It must not be used for incremental synchronization.

### Resource allocations

The response is `{year, allocations}`. Each allocation contains:

```json
{
  "employeeEmail": "alex.morgan@example.test",
  "period": "2026-09",
  "project": {
    "name": "Example project",
    "externalId": "project-alpha",
    "ba": {"name": "Example account", "externalId": "account-alpha"}
  },
  "workstream": {"name": "Delivery", "externalId": "delivery"},
  "percent": 50
}
```

The key is `(employeeEmail, period, project.externalId, workstream.externalId)` when
configured. Periods use ISO `YYYY-MM`.
Percentages of reused workstream keys are summed and may exceed 100. Explicit zeros remain; absent cells mean
no allocation. Null workstreams remain separate from workstream-level allocations.

Employee profiles and separate employee/project/workstream dictionaries are not included.
Historical allocations of dismissed employees remain available by email.
Allocation visibility is employee-based: current employees of accessible
projects or employees allocated to accessible projects qualify, and all their annual
allocations are included. Changes in user scope can therefore change the returned snapshot.

Replace the consumer's annual snapshot only after a successful complete response.
Upserting returned rows alone would retain deleted cells. No revision feed or closed-period
state is exposed.

### Projects

The project response contains `name`, `externalId`, nullable `ba`, `active`,
and `workstreams` (name/externalId references). It includes inactive projects and active
workstreams. Historical reports also resolve deleted workstreams.

## Authentication

Each request carries a random opaque Bearer token. HR Easy administrators generate it offline with `backend/platform/devops/generate-external-token.sh` and give only the raw token to the external system.

HR Easy stores only the SHA-256 hash and its binding:

| Field | Meaning |
|---|---|
| `system` | external system ID, for example `pi-backend` |
| `subject` | active HR Easy username/email used as the acting user |
| `sha256` | lowercase SHA-256 of the generated 64-character token |

The external system cannot create tokens for another system or user because it never receives a signing key. A token is revoked by removing its hash from configuration and redeploying HR Easy. Multiple entries for the same system/user pair allow a replacement token to be introduced before the old one is removed.

Generation:

```shell
backend/platform/devops/generate-external-token.sh
```

The script asks for the external system and acting HR Easy subject. It prints the raw token once and a ready-to-paste Docker Compose `environment` fragment containing the system, subject, and token hash. The raw Bearer token is a secret and must be transferred and stored as such.

## Network boundary

The web container does not proxy `/external/**` and does not implement external IP restrictions. The deployment's external load balancer routes this prefix directly to the platform backend and owns source-IP allowlists, trusted-proxy handling, TLS, and traffic limits. The platform remains responsible for Bearer authentication, acting-user authorization, and read-only method enforcement.

## Authorization

Successful token validation loads the current HR Easy user details for the configured `subject`, including authorities and accessible departments, business accounts, and projects. The security chain adds a reserved `__external_api__` authority only to distinguish this authentication channel.

External endpoints delegate to application services with the acting user's `AuthContext`. Access rules are:

| Data | Authorization behavior |
|---|---|
| Employees | active profiles available to an authenticated user; no roles, skills, offices or ratings are exported |
| Employee avatars | active employees only, by email |
| Overtime summary | requires `overtime_view` |
| Allocation analytics | requires `resource_allocation_read`; only employees with annual allocations whose current project is accessible to the acting user or who have an allocation on an accessible project in that year are included; all annual allocations of these employees are returned, across projects and accounts |
| Projects | available to an authenticated user |

Access is therefore the intersection of:

1. the request accepted and routed by the external load balancer;
2. a valid token bound to an external system and subject;
3. the acting user's current enabled state, authorities, and project hierarchy scope.

Disabling the employee or changing their permissions affects subsequent external requests without changing the external system configuration.

## Request Flow

```text
external system
  -> external load balancer routing and IP/traffic policy
  -> /external security chain
  -> calculate SHA-256 of the opaque Bearer token
  -> resolve its configured system and subject
  -> load active HR Easy user and current authorities
  -> call existing application service with AuthContext
  -> map to isolated external DTOs with business keys
```

The chain is stateless and uses `NoOpServerSecurityContextRepository` and `NoOpServerRequestCache`. It disables form login, HTTP Basic, CSRF, and anonymous authentication for `/external/**`. Business access is limited to `GET /external/api/v1/**` with an external Bearer token; a web session grants no access. Every other method and unlisted external path is denied by authorization. The documentation GET routes listed below are public.

## Configuration

Token hashes and their bindings live in deployment configuration. The raw token is stored only in the external system's secret store and must not be committed to the repository.

```yaml
hreasy:
  external-api:
    tokens:
      - system: pi-backend
        subject: integration.user@company.example
        sha256: ${HREASY_EXTERNAL_PI_BACKEND_TOKEN_SHA256}
```

The equivalent Docker environment variables for the first configured token are:

```text
HREASY_EXTERNAL_API_TOKENS_0_SYSTEM=pi-backend
HREASY_EXTERNAL_API_TOKENS_0_SUBJECT=integration.user@company.example
HREASY_EXTERNAL_API_TOKENS_0_SHA256=<hash-produced-by-the-script>
```

Configuration validation fails application startup when the system, subject, or 64-character hexadecimal SHA-256 is invalid, or when a token hash is duplicated.

Token rotation is a configuration/deployment operation. Runtime credential management and automatic expiration are not supported.

## Error Handling

| Status | Meaning |
|---|---|
| `400 Bad Request` | invalid `YYYY-MM`, year, or query parameter |
| `401 Unauthorized` | missing, malformed, unknown, or revoked token |
| `403 Forbidden` | the external load balancer rejects the request, or the subject/acting user lacks required access |
| `404 Not Found` | requested endpoint or referenced domain object does not exist |
| `5xx` | unexpected platform failure |

Backend errors use the platform's standard JSON format. Requests rejected by the external load balancer use its configured response. Neither response may reveal secrets or token contents.

## Logging and Operations

The external load balancer records the source IP, method, path, and result status. Successful backend requests retain the external system ID in Spring Security authentication details while application services receive and log the acting HR Easy user.

Do not log the Bearer token or response payload. Springdoc publishes documentation containing only `/external/api/v1/**` operations:

- Swagger UI: `/external/docs/swagger-ui.html`.
- OpenAPI JSON: `/external/docs/openapi`.
- OpenAPI YAML: `/external/docs/openapi.yaml`.

Documentation GETs do not require a token and do not create a web session. The allowlist also includes `/external/docs/openapi/swagger-config` and `/external/docs/swagger-ui/**` assets. Use Swagger UI Authorize with the opaque token to execute API calls. API calls require Bearer authentication and the permissions listed above. The external load balancer must forward the entire `/external/**` prefix, including documentation and its assets, to the backend; the web container does not proxy these paths.

## Implementation Outline

The implementation consists of:

1. configuration properties for hashed tokens and their system/user bindings;
2. one ordered WebFlux security chain for `/external/**` with an opaque-token authentication converter;
3. read-only external controllers delegating to an external adapter over existing services;
4. focused security tests for valid, unknown, and malformed tokens plus existing business permissions;
5. focused controller tests for period conversion and delegation.

Public records live in `service.external.dto.ExternalApiDto`.
`ExternalApiService` adapts authorized domain reads to these records.
The BA external key has a Flyway migration and an optional field in the Vue admin form.
