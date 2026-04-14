# E2E Tests (Playwright)

This folder contains end-to-end tests for the Vue 3 application.

## Structure

- `smoke/` - fast route/auth/navigation checks.
- `harness/` - autonomous E2E regressions for the standalone `e2e-harness` app. No login or backend required.
- `profile/`, `employees/`, `mentorship/`, `vacations/`, `overtimes/`, `admin-employees/` - domain specs.
- `fixtures/` - auth and role helpers.
- `support/` - shared selectors, test data constants.

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

Run only autonomous harness regressions:

```sh
npx playwright test e2e/harness --project=chromium
```

## Authenticated scenarios

Some scenarios require test credentials from environment variables:

- `E2E_EMPLOYEE_USERNAME`
- `E2E_EMPLOYEE_PASSWORD`
- `PLAYWRIGHT_PORT` (optional, default: `5174` in local runs)
- `PLAYWRIGHT_BASE_PATH` (optional, for apps served from a subpath)
- `PLAYWRIGHT_BASE_URL` (optional override for full URL, has priority over port/base path)

If required credentials are missing, such tests are skipped.

## Local run with env vars

Use these variables when you run tests locally:

- Always set `E2E_EMPLOYEE_USERNAME` and `E2E_EMPLOYEE_PASSWORD` for authenticated flows.
- Local default for Playwright is `5174`.
- Keep `5173` free if you use it for manual inspection in a separate dev server.
- Set `PLAYWRIGHT_PORT` when Playwright should run against a different port.

Example for app on `http://localhost:5174`:

### Bash

```bash
PLAYWRIGHT_PORT=5174 \
E2E_EMPLOYEE_USERNAME=alexander.bondin \
E2E_EMPLOYEE_PASSWORD=qwe123 \
npx playwright test e2e/smoke/auth-and-routing.spec.ts --project=chromium
```

### PowerShell

```powershell
$env:PLAYWRIGHT_PORT='5174'; `
$env:E2E_EMPLOYEE_USERNAME='alexander.bondin'; `
$env:E2E_EMPLOYEE_PASSWORD='qwe123'; `
npx playwright test e2e/smoke/auth-and-routing.spec.ts --project=chromium
```
