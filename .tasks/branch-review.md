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

- User visual confirmation of the latest input/analytics styling.

## Latest iteration

- Closed project names are struck through in the input selector.
- Comment actions remain available in monthly cells where allocation editing is prohibited.
- Dirty input cells are highlighted; conflict highlighting keeps precedence.
- Analytics group rows use a pale gray background and terminal data rows stay white.
- The analytics year-total column has a right border.
- Allocation Playwright mocks and person-month assertions were fixed; all three scenarios pass.
- The external API security-chain HTTP test covers missing/invalid Bearer tokens, non-GET rejection, web-session isolation, and valid GET access.
- Repository-mock review is tracked separately in `.tasks/repository-mock-tests.md`.

## Status

Implementation and release checks are complete. Keep this task until the user confirms the latest visual changes.
