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
- Restrict the complete external API to an nginx IP allowlist; allow only localhost by default.
- Preserve the current HR Easy permission and project-scope rules.

## Non-goals

- Write operations.
- A UI or database tables for managing integrations.
- An HR Easy endpoint that issues tokens.
- Reimplementation of employee, overtime, allocation, or project queries.
- Pagination, rate limiting, or a generic integration framework in v1.

## API Contract

| Endpoint | Parameters | Response | Existing business flow |
|---|---|---|---|
| `GET /external/api/v1/employees` | `includeFired=false` | `EmployeeDto[]` | `EmployeeService.findAll` |
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

The current basic response does not contain `extErpId`. Adding it is a separate contract decision because it would require a different query/DTO than the web endpoint.

### Overtimes

The response matches the web overtime summary and contains employee/report identifiers, total hours, approval status timestamps, and items grouped by date and project.

The external contract uses `YYYY-MM` instead of exposing the existing zero-based numeric month representation. For example, `2026-09` maps internally to report period `202608`.

### Resource allocations

The response matches annual allocation analytics: year, referenced employees, referenced projects, and non-empty monthly allocation cells.

### Projects

The response matches the existing project dictionary: HR Easy project ID, name, active flag, and business account ID.
The current project model has no external project identifier.

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

## Nginx IP Restriction

Nginx is the only public entry point. The platform backend is available only on the internal network and `/external/**` is proxied by nginx after a global IP/CIDR check.

If `HREASY_EXTERNAL_ALLOWED_IPS` is not configured, the allowlist contains only:

- `127.0.0.1/32`
- `::1/128`

The variable is a comma-separated list of IPv4 addresses, IPv6 addresses, or CIDRs:

```text
HREASY_EXTERNAL_ALLOWED_IPS=10.20.30.40,10.20.31.0/24,2001:db8::10
```

At container startup, `web/devops/run.sh` converts the values to nginx `allow` directives and always appends `deny all`. Invalid characters stop startup. The generated file is included only in the `/external/` location.

Nginx evaluates the address of its direct client. If another load balancer or reverse proxy is placed in front of nginx, nginx `real_ip` settings must trust only that proxy's CIDR before the external allowlist is enabled. The application does not use forwarded headers for IP authorization.

## Authorization

Successful token validation loads the current HR Easy user details for the configured `subject`, including authorities and accessible departments, business accounts, and projects. The security chain adds a reserved `__external_api__` authority only to distinguish this authentication channel.

The external endpoint then calls the same application service as the web endpoint with the acting user's normal `AuthContext`. Existing checks remain the source of truth:

| Data | Existing authorization behavior |
|---|---|
| Employees | available to an authenticated user; project roles and skills are filtered by the current employee permissions and scope |
| Overtime summary | requires `overtime_view` |
| Allocation analytics | requires `resource_allocation_edit` |
| Projects | available to an authenticated user |

Access is therefore the intersection of:

1. the request source accepted by the global nginx allowlist;
2. a valid token bound to an external system and subject;
3. the acting user's current enabled state, authorities, and project hierarchy scope.

Disabling the employee or changing their permissions affects subsequent external requests without changing the external system configuration.

## Request Flow

```text
external system
  -> nginx global IP allowlist
  -> /external security chain
  -> calculate SHA-256 of the opaque Bearer token
  -> resolve its configured system and subject
  -> load active HR Easy user and current authorities
  -> call existing application service with AuthContext
  -> return existing read-only DTO
```

The chain is stateless and uses `NoOpServerSecurityContextRepository`. It disables form login, HTTP Basic, CSRF, and anonymous authentication for `/external/**`. Only `GET` is allowed; every other method under the prefix is denied.

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
| `403 Forbidden` | nginx rejects the source IP, or the subject/acting user lacks required access |
| `404 Not Found` | requested endpoint or referenced domain object does not exist |
| `5xx` | unexpected platform failure |

Backend errors use the platform's standard JSON format. An IP rejected before proxying receives nginx's standard `403` response. Neither response may reveal secrets or token contents.

## Logging and Operations

The existing nginx access log records the source IP, method, path, and result status. Successful backend requests retain the external system ID in Spring Security authentication details while application services receive and log the acting HR Easy user.

Do not log the Bearer token or response payload. Existing health endpoints remain unchanged. The endpoints have OpenAPI operation metadata; a separate generated OpenAPI group is deferred until a consumer needs an independently published specification.

## Implementation Outline

The minimum implementation is:

1. nginx `/external/` proxying and a global allowlist generated from `HREASY_EXTERNAL_ALLOWED_IPS`;
2. configuration properties for hashed tokens and their system/user bindings;
3. one ordered WebFlux security chain for `/external/**` with an opaque-token authentication converter;
4. read-only external controllers delegating to the four existing services;
5. focused security tests for valid, unknown, and malformed tokens plus existing business permissions;
6. focused controller tests for period conversion and delegation.

No new repositories, migrations, or Vue changes are required.

## Open Contract Questions

- Does the first consumer need employee `extErpId`, or is HR Easy employee ID/email sufficient?
- Do projects need a stable external identifier before the project endpoint is useful?
- Which external systems and acting users are required for the first deployment?
- Which production CIDRs must nginx allow, and is there another trusted proxy in front of nginx?
