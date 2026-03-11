# E2E Tests (Playwright)

This folder contains end-to-end tests for the Vue 3 application.

## Structure

- `smoke/` - fast route/auth/navigation checks.
- `profile/`, `employees/`, `mentorship/`, `vacations/`, `overtimes/`, `admin-employees/` - domain specs.
- `fixtures/` - auth and role helpers.
- `support/` - shared selectors, test data constants.
- `TEST_PLAN.md` - migration coverage roadmap and iteration plan.

## Run

From repository root:

```sh
npm run test:e2e
```

List all discovered tests:

```sh
npm run test:e2e -- --list
```

Run a specific file:

```sh
npm run test:e2e -- e2e/smoke/auth-and-routing.spec.ts
```

## Authenticated scenarios

Some scenarios require test credentials from environment variables:

- `E2E_EMPLOYEE_USERNAME`
- `E2E_EMPLOYEE_PASSWORD`
- `PLAYWRIGHT_PORT` (optional, default: `5173` in local runs)
- `PLAYWRIGHT_BASE_PATH` (optional, for apps served from subpath, for example `/app-v3`)
- `PLAYWRIGHT_BASE_URL` (optional override for full URL, has priority over port/base path)

Optional role-specific credentials are described in [TEST_PLAN.md](./TEST_PLAN.md).

If required credentials are missing, such tests are skipped.

## Local run with env vars

Use these variables when you run tests locally:

- Always set `E2E_EMPLOYEE_USERNAME` and `E2E_EMPLOYEE_PASSWORD` for authenticated flows.
- Set `PLAYWRIGHT_PORT` when local app runs on non-default port.
- Set `PLAYWRIGHT_BASE_PATH` when app is served under non-root base path.

Example for app on `http://localhost:5174/app-v3`:

### Bash

```bash
PLAYWRIGHT_PORT=5174 \
PLAYWRIGHT_BASE_PATH=/app-v3 \
E2E_EMPLOYEE_USERNAME=alexander.bondin \
E2E_EMPLOYEE_PASSWORD=qwe123 \
npx playwright test e2e/smoke/auth-and-routing.spec.ts --project=chromium
```

### PowerShell

```powershell
$env:PLAYWRIGHT_PORT='5174'; `
$env:PLAYWRIGHT_BASE_PATH='/app-v3'; `
$env:E2E_EMPLOYEE_USERNAME='alexander.bondin'; `
$env:E2E_EMPLOYEE_PASSWORD='qwe123'; `
npx playwright test e2e/smoke/auth-and-routing.spec.ts --project=chromium
```
