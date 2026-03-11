# E2E Migration Coverage Plan (Vue 3)

This plan tracks Playwright coverage for functionality already migrated from Vue 2 to Vue 3.
Tests run only against Vue 3 routes.

## Iteration 1 (current)

- Build base E2E scaffolding (`fixtures`, `support`, domain spec files).
- Keep smoke tests executable:
  - unauthenticated redirects
  - protected route guard behavior
  - optional login with env credentials
- Capture domain test backlog as `test.fixme`.

## Iteration 2

- Implement Profile and Employees core flows.
- Implement Mentorship reports core CRUD and permission checks.

## Iteration 3

- Implement Vacations and Overtimes core flows.
- Implement Admin Employees list/edit and kids flows.

## Iteration 4

- Implement import workflows (employees and kids).
- Add negative and edge cases.
- Stabilize CI retries/traces and flake handling.

## Environment Variables for Authenticated Scenarios

- `E2E_EMPLOYEE_USERNAME`
- `E2E_EMPLOYEE_PASSWORD`
- Optional additional role-specific pairs:
  - `E2E_MENTOR_AUTHOR_USERNAME`, `E2E_MENTOR_AUTHOR_PASSWORD`
  - `E2E_MENTOR_NON_AUTHOR_USERNAME`, `E2E_MENTOR_NON_AUTHOR_PASSWORD`
  - `E2E_ADMIN_EMPLOYEES_USERNAME`, `E2E_ADMIN_EMPLOYEES_PASSWORD`
  - `E2E_ADMIN_JUNIORS_USERNAME`, `E2E_ADMIN_JUNIORS_PASSWORD`
  - `E2E_OVERTIME_ADMIN_USERNAME`, `E2E_OVERTIME_ADMIN_PASSWORD`
  - `E2E_READONLY_USERNAME`, `E2E_READONLY_PASSWORD`
