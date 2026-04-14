# E2E Harness

This folder contains a standalone Vue app used only for end-to-end testing of shared UI infrastructure.

## Why it exists

The main app is a noisy environment for generic table regressions because it brings in:

- auth flows
- router guards
- feature-specific data loading
- application shell side effects

The harness isolates tests for shared building blocks such as:

- `TableFirstPageLayout`
- `TablePageCard`
- `HREasyTableBase`
- `AdaptiveFilterBar`
- keep-alive route transitions around table pages

## Entry points

- HTML entry: [`e2e-harness.html`](/D:/stm/hreasy/src/web/e2e-harness.html)
- App bootstrap: [`main.ts`](/D:/stm/hreasy/src/web/src/e2e-harness/main.ts)
- Harness shell: [`HarnessApp.vue`](/D:/stm/hreasy/src/web/src/e2e-harness/HarnessApp.vue)
- Harness router: [`router.ts`](/D:/stm/hreasy/src/web/src/e2e-harness/router.ts)

## What belongs here

- test-only routes and views for isolated E2E scenarios
- controlled async datasets
- synthetic tabs, banners, filters, and route transitions used to stress shared table behavior

## What should not live here

- production routes
- feature-specific business workflows
- auth or permissions logic from the main app

## Current sandbox views

- [`TableSandboxView.vue`](/D:/stm/hreasy/src/web/src/e2e-harness/views/TableSandboxView.vue)
- [`TableSandboxPlainView.vue`](/D:/stm/hreasy/src/web/src/e2e-harness/views/TableSandboxPlainView.vue)
- [`TableSandboxVacationsLikeView.vue`](/D:/stm/hreasy/src/web/src/e2e-harness/views/TableSandboxVacationsLikeView.vue)
- [`TableSandboxEchoView.vue`](/D:/stm/hreasy/src/web/src/e2e-harness/views/TableSandboxEchoView.vue)

## Current fixture patterns

- generic table page with `v-window` tabs and adaptive filters
- plain keep-alive table page without tabs
- vacations-like page shell with top tabs, `TablePageCard`, `AdaptiveFilterBar`, and route roundtrip

## Related E2E suite

- Stable harness regressions live under [`e2e/harness/`](/D:/stm/hreasy/src/web/e2e/harness)
- Current stable specs:
  - [`table-sandbox-regression.spec.ts`](/D:/stm/hreasy/src/web/e2e/harness/table-sandbox-regression.spec.ts)
  - [`table-harness-vacations-roundtrip.spec.ts`](/D:/stm/hreasy/src/web/e2e/harness/table-harness-vacations-roundtrip.spec.ts)
