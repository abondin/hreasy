# Repository Guidelines

## Simplicity First
- Code must be simple, direct, and easy to read.
- Do not add extra queries, abstractions, payload enrichment, fallback branches, or broad infrastructure unless they are required for the current user path.
- If a change starts requiring a lot of code or a non-obvious design, stop and ask before implementing it.
- Prefer the smallest clear backend/frontend contract that solves the agreed scenario.

## Notification Documentation
- When adding, removing, or changing business notifications, update `.docs/notification_catalog.md` in the same change.
- Keep notification statuses in the catalog aligned with code: use `Implemented` only for events that are actually published by the current code path.

## Frontend Skills
- For changes under `web/**`, use `.agents/skills/hreasy-vue3-development`.
- For Vue tests, use `.agents/skills/vue-testing-best-practices`.
- For Vue debugging, use `.agents/skills/vue-debug-guides`.
