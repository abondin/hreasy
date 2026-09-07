# Resource Allocations

## Goal

Provide a project-scoped annual allocation input workflow for managers and a separate read-only analytics matrix.

## Agreed Requirements

- `Data entry` is the only editable tab; `Analytics` is read-only.
- Data entry defaults to the current calendar year and the first writable project alphabetically; other projects remain readable.
- Exactly one project is edited at a time; one screen exposes all 12 months.
- An optional project workstream selects an independent allocation dimension. Project-level and multiple workstream-level values may coexist for one employee/month/project.
- Rows include current project employees, employees with allocations on that project in the selected year, and employees added in the current draft.
- A cell is editable only when the user has write permission and shared hierarchy access to the target project, the month is open, and employment overlaps the month. The dismissal month is inclusive.
- Dismissal and another current project remain visible in the employee text. Visual status styling is deferred until it can use native grid capabilities.
- Closed months are marked in the column name and remain backend-enforced. Additional month/status styling is deferred.
- Authentication already merges manager responsibility with explicit project/department/BA access. Allocation write scope reuses `ProjectHierarchyAccessor`; any employee can be assigned to an accessible project.
- Read permission exposes all allocation input and analytics data. Write and read permissions are granted by default to `pm`, `finance`, `pm_finance`, `salary_manager`, and `global_admin`; allocation admin is granted to `global_admin`.
- Empty/zero values are not stored. Percent values are integers from 1 through 1000.
- Allocation data remains monthly, with physical `year` and `period` columns. Annual requests query the physical `year`; the DB constraint verifies it matches the legacy zero-based period convention.
- One Save creates one project/year revision, with one immutable change per employee/month.
- Per-cell optimistic revisions reject stale cells and return current server values plus the non-conflicting local draft for client-side rebase.
- A version conflict reloads current server values, preserves non-conflicting draft changes, and marks conflicting cells red until they are edited again.
- Input cells show a muted `+ N%` only when the employee has a positive allocation on other projects in that month; the selected-project value remains primary.
- The selected-project value includes `%` outside edit mode while the editor keeps the raw numeric value.
- The selected input year and project are persisted as `year` and `projectId` URL query parameters.
- Month closing/reopening is backend-enforced and restricted by `resource_allocation_admin`.

## Implementation

- Flyway schema stores `year` explicitly on current allocations, annual revisions, and closed periods.
- Annual API: `GET /api/v1/resource-allocations/input/{year}` and `PUT /api/v1/resource-allocations/input/{year}/{projectId}`, both with optional `workstreamId`.
- Annual input returns employee/month sums from projects other than the selected project; the UI renders positive values as a muted `+ N%` hint.
- Input initializes `year` and `projectId` from the URL and replaces those query parameters after the backend resolves the selected project.
- Period API: `GET /api/v1/resource-allocations/closed-periods/{year}` and `PUT/DELETE /api/v1/resource-allocations/closed-periods/{period}`. Allocation admins manage a selected month from the analytics toolbar.
- Annual analytics uses `GET /api/v1/resource-allocations/analytics/{year}` and returns only employees, projects, and employee/project pairs with non-zero values in that year.
- Analytics has two read-only hierarchy modes and defaults to projects. Both use the same nested project/workstream behavior: a sole workstream is shown inline, projects remain expandable with one stream, and stream branches start collapsed.
- Employee analytics is summary-first: employee groups start collapsed, retain their monthly totals, and show the sole project name or project count beside the employee. Business-account groups start expanded; projects with workstreams and all workstream groups start collapsed.
- BA and dependent project multi-selects filter complete hierarchy branches. The current search matches an employee or project name and keeps matching employee/project pairs; its final parent/child retention semantics remain open for UX review.
- RevoGrid provides virtualized annual editing, clipboard, range selection, native Tab/Enter, and native autofill behavior.
- Shared HREasy page/layout, Vuetify controls, dialogs, period switcher, search normalization, and permissions remain in use.
- Data entry now uses the same `AdaptiveFilterBar` and `TableToolbarActions` composition as neighboring manager pages. The project selector is a normal toolbar filter and the annual grid is full-width without a nested card.
- Annual input uses a 600px resizable employee column and twelve fixed 110px month columns, fitting a 2048px viewport; smaller screens use native horizontal scrolling.
- `Data entry` and `Analytics` are independent sibling child routes and components under a shared route-tab layout. The base URL redirects to `/management/resource-allocations/input`; analytics remains `/management/resource-allocations/analytics`.
- Both allocation grids use native RevoGrid headers, editor, sizing, theme, resize, and autofill without deep selectors, CSS variables, or geometry workarounds.
- The employee column uses RevoGrid's documented Vue cell-template adapter: dismissed employees get a Vuetify badge, another current project is small muted inline text, and the final row contains the employee autocomplete.
- Successful saves rely on the standard loading state of the Save button and refreshed data; only errors and conflict warnings render page alerts.
- The child route, input card, table slot, and RevoGrid now share one flex-height chain, so the input grid fills the remaining viewport instead of falling back to its approximately 300px intrinsic height.
- The employee cell renderer disables Vue attribute fallthrough: RevoGrid metadata no longer reaches the Vuetify autocomplete input and resets its search text.
- Analytics switches grouping direction by remounting only RevoGrid because the library otherwise preserves its previous internal grouping model for rows with stable IDs.
- Durable business, access, API, and persistence documentation is available in `.docs/resource_allocations.md` and linked from the README key features.
- The durable document is user-first: workflows and cell behavior precede access restrictions, while API and persistence details remain at the end.

## Validation

- Focused backend suite passed: 18 tests covering allocation access, project/workstream admin mapping, request deserialization, and external authentication/controller delegation.
- PostgreSQL/Testcontainers `OvertimeServiceTest` passed: all 49 migrations through V1.3.0.23 applied, detailed overtime retained its optional workstream, and summaries aggregated it with project-level overtime.
- Frontend type-check and targeted ESLint passed.
- Focused frontend suite passed: 11 unit tests and 4 Chromium E2E scenarios, including the three-level analytics hierarchy and large mocked matrix.
- `web/src/locales/ru.json` parses as JSON and `git diff --check` passes.

## Remaining

- The input project dropdown currently renders every project returned for global read access. Restrict data-entry selection to projects with `editable=true`; analytics remains global.
- Cell comment threads are not a built-in RevoGrid feature. RevoGrid supplies the cell renderer/interaction surface, but durable threads require a separate backend model, API, permissions, and comment UI; implementation awaits an explicit product decision.
- Decide how current allocations on a soft-deleted workstream are retired: analytics can still resolve the deleted stream, but annual input intentionally lists active workstreams only, so such cells cannot currently be set to zero in the UI.
- Recreate the local database after the edited allocation/workstream migrations, as agreed.
- After user acceptance, update `changelogs/CHANGELOG.md` and remove this task file.
