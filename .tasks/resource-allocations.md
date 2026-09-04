# Resource Allocations

## Goal

Provide a project-scoped annual allocation input workflow for managers and a separate read-only analytics matrix.

## Agreed Requirements

- `Data entry` is the only editable tab; `Analytics` is read-only.
- Data entry defaults to the current calendar year and the first managed project alphabetically; users without managed projects fall back to their first available project.
- Exactly one project is edited at a time; one screen exposes all 12 months.
- Rows include current project employees, employees with allocations on that project in the selected year, and employees added in the current draft.
- A cell is editable only when the user can edit the project, the month is open, and employment overlaps the month. The dismissal month is inclusive.
- Dismissal and another current project remain visible in the employee text. Visual status styling is deferred until it can use native grid capabilities.
- Closed months are marked in the column name and remain backend-enforced. Additional month/status styling is deferred.
- Managers can edit only projects in their manager line. Explicit project access does not make a project `managed`; global edit permission remains global.
- Empty/zero values are not stored. Percent values are integers from 1 through 1000.
- Allocation data remains monthly, with physical `year` and `period` columns. Annual requests query the physical `year`; the DB constraint verifies it matches the legacy zero-based period convention.
- One Save creates one project/year revision, with one immutable change per employee/month.
- Per-cell optimistic revisions reject stale cells and return current server values plus the non-conflicting local draft for client-side rebase.
- A version conflict reloads current server values, preserves non-conflicting draft changes, and marks conflicting cells red until they are edited again.
- Month closing/reopening is backend-enforced and restricted by `resource_allocation_period_manage`.

## Implementation

- Flyway schema stores `year` explicitly on current allocations, annual revisions, and closed periods.
- Annual API: `GET /api/v1/resource-allocations/input/{year}` and `PUT /api/v1/resource-allocations/input/{year}/{projectId}`.
- Period API: `PUT/DELETE /api/v1/resource-allocations/closed-periods/{period}`; the grid currently has no custom close/reopen control.
- Annual analytics uses `GET /api/v1/resource-allocations/analytics/{year}` and returns only employees, projects, and employee/project pairs with non-zero values in that year.
- Analytics has two read-only hierarchy modes and defaults to projects. Project mode uses RevoGrid's native nested BA -> project grouping with employee children; employee mode groups project children by employee. Group rows show monthly allocation sums through the native group-cell template and remain expanded by default.
- Employee analytics is summary-first: employee groups start collapsed, retain their monthly totals, and show the sole project name or project count beside the employee. Project analytics remains expanded by default.
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

## Validation

- `mvn -q -f backend/pom.xml -pl platform -am -Dtest=ResourceAllocationServiceTest "-Dsurefire.failIfNoSpecifiedTests=false" test` passed.
- `npm run type-check` passed.
- Targeted ESLint for the allocation view, service, unit test, and E2E passed.
- `npm run test:unit -- ResourceAllocationsView` passed: 5 tests.
- Allocation Chromium E2E passed: 3 tests, including conflict rebase, annual save, direct sibling-route selection, nested analytics groups with totals, and a 500-employee/80-project matrix.
- `npm run check:win-text-integrity` and `git diff --check` passed.
- Route separation passed type-check, targeted ESLint, 5 unit scenarios, direct analytics navigation, route-tab navigation, and the large mocked browser scenario. Fixed grid height avoids RevoGrid resize-observer loops.
- The compact input and redesigned analytics toolbar were visually checked at 2048x1080; the final Chromium E2E also passed at the default viewport.
- The annual input browser scenario types the full employee name character by character, verifies the search value and selection, and asserts the grid is taller than its former collapsed height.

## Remaining

- Cell comment threads are not a built-in RevoGrid feature. RevoGrid supplies the cell renderer/interaction surface, but durable threads require a separate backend model, API, permissions, and comment UI; implementation awaits an explicit product decision.
- Apply the edited live migration by recreating the local database, as agreed.
- After user acceptance, update `changelogs/CHANGELOG.md` and remove this task file.
