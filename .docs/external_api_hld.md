# HLD: External Read-only API

## Context

HR Easy needs a small server-to-server API for systems such as `pi-backend`. The API exposes existing HR data without creating a web session and performs every request on behalf of a real HR Easy user.

The URL prefix is `/external/api/v1`. All operations are read-only.

The implementation may reuse the stateless Bearer authentication shape of the existing Telegram integration, but must use separate configuration and security components. The legacy Telegram module is not a domain API baseline.

## Goals

- Expose the employee list available through the basic employee API.
- Expose the web overtime summary for a requested month.
- Expose annual resource allocation analytics.
- Expose basic project information.
- Identify both the calling external system and the HR Easy user on whose behalf it operates.
- Leave external routing, source-IP restrictions, and traffic limits to the deployment load balancer.
- Preserve the current HR Easy permission and project-scope rules.

## Non-goals

- Write operations.
- A UI or database tables for managing integrations.
- An HR Easy endpoint that issues tokens.
- Reimplementation of employee, overtime, allocation, or project queries.
- Reverse-proxy routing, IP filtering, rate limiting, pagination, or a generic integration framework in v1.

## API Contract

| Endpoint | Parameters | Response | Existing business flow |
|---|---|---|---|
| `GET /external/api/v1/employees` | `includeFired=false` | `EmployeeDto[]` | `EmployeeService.findAll` |
| `GET /external/api/v1/employees/{employeeId}/avatar` | HR Easy employee ID | PNG image, or 404 | `EmployeeService.avatar` / `FileStorage.streamImage` |
| `GET /external/api/v1/employees/avatar` | required `email` query parameter | PNG image, or 404 | `EmployeeService.avatarByEmail` / `FileStorage.streamImage` |
| `GET /external/api/v1/overtimes/{period}` | `period` is `YYYY-MM` | `OvertimeEmployeeSummary[]` | `OvertimeService.getSummary` |
| `GET /external/api/v1/resource-allocations/analytics/{year}` | four-digit calendar year | `ResourceAllocationAnalyticsDto` | `ResourceAllocationService.getAnalytics` |
| `GET /external/api/v1/projects` | none | `ProjectDictDto[]` | `DictService.findProjects` |

The external controller is an adapter over the existing services. It converts the ISO overtime period to the current internal report period and otherwise returns the existing web DTOs unchanged. This keeps the first contract and implementation small.

Example:

```http
GET /external/api/v1/overtimes/2026-09 HTTP/1.1
Host: hr.example.org
Authorization: Bearer <opaque-token>
Accept: application/json
```

### Employee list

The response matches `GET /api/v1/employee`:

- active employees by default;
- dismissed employees when `includeFired=true`;
- the same fields, ordering, permission-based role visibility, and skill visibility as the web API.

The current basic response does not contain `extErpId`; consumers match employees by HR Easy ID or email. ERP-specific identifiers are outside this contract.

### Employee avatars

Both avatar endpoints use the same external Bearer authentication as the employee list. Active and dismissed employees are supported. A successful response contains the actual image bytes with `Content-Type: image/png`.

- By ID: `GET /external/api/v1/employees/101/avatar`.
- By email: `GET /external/api/v1/employees/avatar?email=alex.morgan%40example.test`.
- Email lookup is exact and case-insensitive; surrounding whitespace is ignored. URL-encode the query value, especially `+` as `%2B`. SQL wildcard characters have no special meaning.
- A missing employee or avatar returns `404`; no default profile image is substituted. Missing or blank email returns `400`.
- The employee list's `hasAvatar` field can be used to skip downloads for employees without an image.

### Overtimes

The response matches the web overtime summary and contains employee/report identifiers, total hours, approval status timestamps, and items grouped by date and project. Workstream-level overtime details remain available in the employee report API, not in the summary.

The external contract uses `YYYY-MM` instead of exposing the existing zero-based numeric month representation. For example, `2026-09` maps internally to report period `202608`.

### Resource allocations

The response matches annual allocation analytics: `year`, referenced `employees`, `projects`, `workstreams`, and recorded monthly `allocations`. Each cell has an optional `workstreamId`; project-level and multiple workstream-level cells may coexist for the same employee and month.

Allocation periods retain the internal zero-based numeric convention: `202600` is January 2026, `202608` is September, and `202611` is December. This differs from the ISO month in the overtime request URL.

A cell with `percent: 0` is an explicit zero allocation. An absent cell means no allocation; the read response does not emit a dense matrix of null cells. Clearing through the internal write API sends `percent: null`, deletes the current cell, and preserves a nullable before/after history entry.

For a full annual import:

1. Fetch `/external/api/v1/resource-allocations/analytics/{year}`.
2. Match cells to the included employees and workstreams by their HR Easy IDs. Referenced soft-deleted workstreams are included in analytics; do not rely only on the active workstream list from `/projects`.
3. Fetch `/external/api/v1/projects` if the consumer needs project `externalId`, and join by `projectId`.
4. Identify each cell by `(period, employeeId, projectId, workstreamId)`, treating null workstream as a separate project-level dimension. Keep explicit zeros.
5. Replace the consumer's annual snapshot after a successful complete response. Upserting only returned cells would leave previously deleted allocations behind.

The external API provides no writes, allocation revision/history feed, or closed-period states. Consumers cannot infer whether an allocation month is finalized from this response. These are contract limitations, not permissions that can be enabled on the existing endpoint.

### Projects

The response matches the existing project dictionary and includes the HR Easy project ID, optional `externalId`, name, active flag, business account ID, and active workstreams. Each workstream contains `id`, optional `externalId`, `displayName`, and optional `description`.

External IDs are stable integration keys. Project external IDs are globally unique when present; workstream external IDs are unique among active workstreams in one project.

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

The external endpoint then calls the same application service as the web endpoint with the acting user's normal `AuthContext`. Existing checks remain the source of truth:

| Data | Existing authorization behavior |
|---|---|
| Employees | available to an authenticated user; project roles and skills are filtered by the current employee permissions and scope |
| Employee avatars | available to an authenticated external user, including dismissed employees |
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
  -> return existing read-only DTO
```

The chain is stateless and uses `NoOpServerSecurityContextRepository`. It disables form login, HTTP Basic, and anonymous authentication for `/external/**`. Standard Spring CSRF protection remains enabled; the allowed `GET` requests do not require a CSRF token. Every other method under the prefix is denied by authorization.

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

Token rotation is a configuration/deployment operation in v1. Runtime credential management and automatic expiration are deferred until there is a concrete operational need.

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

Do not log the Bearer token or response payload. Existing health endpoints remain unchanged. The endpoints have OpenAPI operation metadata; a separate generated OpenAPI group is deferred until a consumer needs an independently published specification.

## Implementation Outline

The minimum implementation is:

1. configuration properties for hashed tokens and their system/user bindings;
2. one ordered WebFlux security chain for `/external/**` with an opaque-token authentication converter;
3. read-only external controllers delegating to the existing services;
4. focused security tests for valid, unknown, and malformed tokens plus existing business permissions;
5. focused controller tests for period conversion and delegation.

No new repositories, migrations, or Vue changes are required.

## Open Contract Questions

- Which external systems and acting users are required for the first deployment?
