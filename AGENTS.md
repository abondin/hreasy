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
- If a change requires substantial code or a non-obvious design, stop and ask before implementing it.

## Shared Rules

- Keep code comments, documentation, and tests in English.
- Do not revert unrelated local changes.
- Treat backend permissions as the source of truth for protected data and actions.
- Run the narrowest meaningful validation after changes.
- Record decisions in `.docs/` only when they affect architecture, service ownership, or deployment.

## Notification Documentation

- When adding, removing, or changing business notifications, update `.docs/notification_catalog.md` in the same change.
- Use `Implemented` only for events published by the current code path.

## Repository Skills

- For any task that spans analysis, implementation, or verification iterations, use `.agents/skills/task-lifecycle`.
- For Java changes, use `.agents/skills/java-coding-style`.
- For JUnit work, use `.agents/skills/junit-tests-rules`.
- For Platform Flyway migrations, use `.agents/skills/db-migration-style`.
- For changes under `web/**`, use `.agents/skills/hreasy-vue3-development`.
- For Vue tests, use `.agents/skills/vue-testing-best-practices`.
- For Vue debugging, use `.agents/skills/vue-debug-guides`.

## Task Lifecycle

- Keep the current task context in `.tasks/<task-name>.md` and update it after each meaningful iteration.
- Record agreed requirements, decisions, progress, checks, and remaining questions; keep it concise and current rather than appending a diary.
- Do not remove the task file when implementation merely appears complete. After the user confirms the task is finished, update applicable durable documentation and `changelogs/CHANGELOG.md`, then delete the task file.
