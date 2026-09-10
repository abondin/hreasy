# Resource allocation branch review

## Goal

Review the complete working tree against local `origin/master` and reconcile resource-allocation/external-API documentation with implementation.

## Scope

- Include committed, modified, deleted, and untracked files in the current working tree.
- Trace allocation persistence, permissions, API, export, frontend behavior, and tests.
- Trace external API authentication, authorization, routing ownership, contracts, and tests.
- Review all other branch changes for correctness, security, regressions, and unnecessary complexity.

## Baseline

- Branch: `feature/resource_allocation`.
- Fetched `origin/master`; it remains `6f23515a0cf0c73bf72d41c0b5227e2992eefa80` (2026-08-27).

## Reconciliation

- Allocation scope, sparse explicit-zero model, project/workstream dimensions, locking/concurrency, period administration, external reuse, and Excel semantics match `.docs/resource_allocations.md`.
- Excel passes the acting username as `exportedBy`; template columns match the documented order.
- External API is a dedicated stateless GET-only backend security chain. Web/local proxy files have no final diff from `origin/master`; deployment load balancer ownership matches `.docs/external_api_hld.md`.
- `extErpId` is not exposed. Employee matching is by HR Easy ID/email; projects retain their separate `externalId`.

## Findings

- Fixed RevoGrid cell-template children and allocation test wrapper typings; frontend type-check passes.
- Fixed the analytics totals test to use the Grid remounted by the hierarchy-mode key.
- Allocation input now rejects non-integer, negative, over-1000, and non-numeric values instead of converting them to a meaningful explicit zero.
- Four-digit external API year validation is deliberately deferred as a minor issue.

## Checks

- `npm run lint -- --no-cache`: passed.
- `npm run type-check`: passed.
- `npm run test:unit -- --run`: 32 passed.
- `npm run build-only`: passed (existing chunk-size warning only).
- `npm run check:win-text-integrity`: passed.
- Focused non-container backend tests for external API, security, allocation service/export/Excel, avatars, project/manager/salary changes passed where runnable.
- Full Platform suite could not be validated because Docker is unavailable; 97 context errors are environmental.
- `FileStorageTest` symbolic-link case is not runnable on this Windows account because symlink creation is denied.
- `git diff --check origin/master`: passed.

## Status

- The first three review findings are fixed and verified; only the deliberately deferred year validation remains.
