# AGENTS.md

This repository is HR Easy, an HR portal with separate backend/frontend/service projects.

All code comments, docs and tests only in English

## Repository Layout

- `platform/` - main HR Easy backend, Java/Spring, Maven project.
- `web/` - Vue/Vuetify frontend.
- `telegram/` - legacy Telegram bot service. Deprecated/outdated; do not use it as a baseline for new notification work.
- `notify-ms/` - notification delivery microservice.
- `.docs/` - architecture notes and diagrams.
- `.hreasy-localdev/` - local docker-compose and local environment helpers.
- `devops/` - build/deploy helper scripts.

There is no root Maven aggregator. Run Maven commands from the service directory.

## Backend Conventions

- Platform stack is Java 25, Spring Boot 3.5.x, WebFlux/R2DBC/PostgreSQL/Flyway.
- New services may use Java 25 and Spring Boot 4.x when explicitly chosen.
- Prefer existing package style under `ru.abondin.hreasy`.
- Use R2DBC repositories for runtime DB access.
- Use Flyway migrations under `src/main/resources/db/migration`.
- Do not add cross-service foreign keys.
- Keep service-owned schemas unique. Platform owns `notify`; notification microservice owns `notify_ms`.
- Treat the backend role and permission model as the source of truth for protected data, actions, and notification recipients.

## Frontend Conventions

- Frontend lives in `web/`.
- Vue + TypeScript + Vuetify.
- Prefer existing composables/components/API client patterns before adding new ones.
- Study the backend role and permission model before adding frontend access checks, hidden actions, badges, routes, or notification UI.

## Build And Test

- Platform:
  - `cd platform`
  - `mvn -q -DskipTests package`
  - targeted tests: `mvn -q -Dtest=SomeTest test`
- Notification service:
  - `cd notify-ms`
  - `mvn -q test`
  - `mvn -q -DskipTests package`
- Web:
  - `cd web`
  - use existing `package.json` scripts.

If dependency resolution fails, check whether the configured Nexus is reachable before changing dependency versions.

## Database

- Existing migrations are versioned and must not be edited after being applied. Add new migrations instead.
- Prefer schema-qualified SQL in migrations.
- For R2DBC entity mapping, avoid putting `schema.table` into `@Table` if Spring Data quotes it incorrectly; configure schema/search path instead.
- Shared physical DBs are allowed in test/dev only when schemas stay isolated.

## Working Rules

- Read nearby code before editing.
- Keep changes scoped to the requested task.
- Do not revert unrelated local changes.
- Use `rg` for search.
- Use `apply_patch` for manual file edits.
- Run the narrowest meaningful tests after code changes.
- Document new architecture decisions in `.docs/` when they affect service boundaries, data ownership, or deployment.
