# Resource allocation and external API production readiness

## Goal

Close the remaining release checks found by the full branch review against `origin/master`.

## Completed

- Allocation and external API implementation, authorization, routing ownership, documentation, Excel export, and `exportedBy` were reviewed.
- The comment mutation race was fixed with stale cell/request guards.
- Resource-allocation comments were implemented, documented, visually verified, and moved to the changelog.
- Platform tests passed on Testcontainers PostgreSQL 15; web unit tests, type-check, lint, build, text-integrity, and `git diff --check` passed.
- Four-digit year validation, external load-balancer controls, file access, and actuator exposure were explicitly accepted or deferred by the user.

## Remaining

- In allocation input, strike through closed project names, allow comments on non-editable cells, highlight dirty cells, and add the year-column right border.
- In allocation analytics, keep terminal data rows white, shade group rows light gray, and add the year-column right border.
- Fix and rerun `web/e2e/app-mocked/resource-allocations-page.spec.ts`: its mock still does not handle the annual comment-summary request, and its analytics URL assertion still expects no `year`/`unit` query parameters.
- Add an HTTP-level external security-chain test proving missing/invalid Bearer tokens return 401, non-GET methods are rejected, and a web session cannot authenticate `/external/**`.
- Replace the repository-mocking comment test with database-backed coverage; this is tracked in `.tasks/repository-mock-tests.md` together with the other repository mocks.

## Status

The review itself is complete, but the branch is not ready to call production-ready until the remaining E2E and external security-chain checks pass. Keep this task as the release checklist.
