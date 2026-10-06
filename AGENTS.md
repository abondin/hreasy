# Repository Guidelines

HR Easy is a monorepository with separate backend services and a Vue frontend.

## Repository Layout

- `backend/` - Maven reactor; follow `backend/AGENTS.md` for backend work.
- `backend/platform/` - main HR Easy backend.
- `backend/notify-ms/` - notification delivery service.
- `backend/telegram/` - deprecated legacy Telegram bot; do not use it as a baseline for new work.
- `web/` - Vue frontend; follow `web/AGENTS.md` for frontend work.
- `.docs/` - architecture notes and diagrams.
- `.tasks/` - tracked working context for active tasks.
- `.hreasy-localdev/` - local Docker Compose environment.
- `devops/` - build and deployment scripts.

## Simplicity First

- Keep code simple, direct, and scoped to the requested user path.
- Reuse nearby project patterns before adding queries, abstractions, payload fields, fallback branches, or infrastructure.
- Prefer the smallest clear backend/frontend contract that solves the agreed scenario.
- The web frontend and backend are deployed together. Do not add compatibility with older web clients unless explicitly requested; update both sides of the internal web API contract together instead of adding fallbacks for omitted fields or legacy payloads. This rule does not apply to the external integration API.
- For internal web CRUD, enforce field-length limits in frontend forms and database column definitions; do not duplicate them in backend services or add DTO length constraints unless explicitly requested. Requests bypassing the frontend may receive database errors. Keep the existing DTO length validation for allocation comments.
- If a change requires substantial code or a non-obvious design, stop and ask before implementing it.

## Shared Rules

- Keep code comments, documentation, and tests in English.
- Do not revert unrelated local changes.
- Treat backend permissions as the source of truth for protected data and actions.
- Run the narrowest meaningful validation after changes.
- Record decisions in `.docs/` only when they affect architecture, service ownership, or deployment.
- Documentation describes current supported behavior and must be understandable without development history. Record important feature, API-contract, permission, and deployment changes in `changelogs/CHANGELOG.md`; do not put migration narratives or comparisons with previous implementations in usage or API documentation.
- Changelog entries describe the net result of a release compared with the previous release, not daily implementation steps. Consolidate related changes into one entry, omit minor UI adjustments and intermediate contracts of unreleased features, and update an existing entry instead of appending development history.
- The changelog bugfix section includes only fixes for problems present in the previous released version that affected user experience. Verify against release tags when uncertain. Omit regressions introduced and fixed within an unreleased version; record important security fixes separately from user-facing bugfixes.
- Technical changelog entries must help operators decide whether and how to upgrade: changed runtime/build requirements, deployment steps, compatibility, or security impact. Omit dependency-update lists, CI housekeeping, and internal file reorganizations without an operational consequence.

## Notification Documentation

- When adding, removing, or changing business notifications, update `.docs/notification_catalog.md` in the same change.
- Use `Implemented` only for events published by the current code path.

## Repository Skills

- For large, multi-stage tasks that need persistent working context, use `.agents/skills/task-lifecycle`. Small fixes and local enhancements do not require a task file.
- For Java changes, use `.agents/skills/java-coding-style`.
- For JUnit work, use `.agents/skills/junit-tests-rules`.
- For Platform Flyway migrations, use `.agents/skills/db-migration-style`.
- For changes under `web/**`, use `.agents/skills/hreasy-vue3-development`.
- For Vue tests, use `.agents/skills/vue-testing-best-practices`.
- For Vue debugging, use `.agents/skills/vue-debug-guides`.

## Task Lifecycle

- Create a persistent `.tasks/<task-name>.md` only for large tasks such as cross-module features, architectural changes, or substantial migrations. Analysis, implementation, and verification alone do not make a task large.
- For small fixes and local enhancements, skip the task file by default. If temporary notes help, delete them when implementation and validation finish; no separate user confirmation is needed.
- For large tasks, update the task file after each meaningful iteration.
- Record agreed requirements, decisions, progress, checks, and remaining questions; keep it concise and current rather than appending a diary.
- Keep persistent files for large tasks until the user confirms completion. Then update applicable durable documentation and `changelogs/CHANGELOG.md`, and delete the task file.
