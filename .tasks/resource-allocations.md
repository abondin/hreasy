# Resource Allocations

## Goal

Provide a project-scoped annual allocation input workflow for managers and a separate read-only analytics matrix.

## Agreed Requirements

- `Data entry` is the only editable tab; `Analytics` is read-only.
- Data entry lists only writable projects and defaults to the first one alphabetically; all allocations remain readable in analytics.
- Exactly one project is edited at a time; one screen exposes all 12 months.
- An optional project workstream selects an independent allocation dimension. Project-level and multiple workstream-level values may coexist for one employee/month/project.
- Rows include current project employees, employees with allocations on any direction of that project in the selected year, and employees added in the current draft.
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
- The input project selector excludes projects outside the selected year unless they have allocations in that year. Projects ended within the year show their closed status and actual end date as a Vuetify item subtitle.
- The employee selector and input rows show the current project role. Selector search covers employee name, current project, and role, but not dismissal date. Analytics shows and searches by the role too.
- Month closing/reopening is backend-enforced and restricted by `resource_allocation_admin`. The analytics lock dialog edits all twelve months of the selected year and saves the selection once.

## Implementation

- Flyway schema stores `year` explicitly on current allocations, annual revisions, and closed periods.
- Annual API: `GET /api/v1/resource-allocations/input/{year}` and `PUT /api/v1/resource-allocations/input/{year}/{projectId}`, both with optional `workstreamId`.
- Annual input returns employee/month sums from dimensions other than the selected project/workstream pair and marks whether they include the same project; the UI uses that flag to retain project employees across workstreams and renders positive values as a muted `+ N%` hint.
- Input initializes `year` and `projectId` from the URL and replaces those query parameters after the backend resolves the selected project.
- Period API: `GET` and `PUT /api/v1/resource-allocations/closed-periods/{year}`. The PUT compares requested and current sets and writes only real state transitions.
- Current closed periods retain their original closer and timestamp. An immutable period history stores both close and reopen actors/timestamps; unchanged checked months create no history events.
- The period dialog uses twelve checkboxes with Save/Cancel and highlights the current month. Closed input-grid headers use the installed MDI lock instead of a Unicode emoji.
- Annual analytics uses `GET /api/v1/resource-allocations/analytics/{year}` and returns only employees, projects, and employee/project pairs with non-zero values in that year.
- Analytics has two read-only hierarchy modes and defaults to projects. In employee mode, workstreams are terminal value rows under projects, without a duplicate project leaf. A sole workstream is shown inline, projects remain expandable with one stream, and stream branches start collapsed.
- Project summaries count project-level allocation as a separate variant, so a workstream name is shown inline only when it is the sole child row.
- In both hierarchy modes, projects without workstream allocations skip `Without workstream`; it remains a separate branch only when it distinguishes project-level values from workstream values.
- Direction counts use the shared vue-i18n Russian plural rule, and terminal data rows have a subtle background in both hierarchy modes.
- Group and terminal-row indentation uses one shared hierarchy step; a single leaf cell template positions terminal text one level below its parent instead of relying on RevoGrid's ignored container padding.
- Regular and pseudo-group terminal cells use the same marker and one grid-cell CSS rule, so both row shapes have the same full-cell background, including empty months.
- Employee analytics is summary-first: employee groups start collapsed, retain their monthly totals, and show the sole project name or project count beside the employee. Business-account groups start expanded; projects with workstreams and all workstream groups start collapsed.
- BA and dependent project multi-selects filter complete hierarchy branches. The shared text-search field matches employee, current project role, project, or workstream and keeps matching employee/project pairs; its final parent/child retention semantics remain open for UX review.
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

- Focused backend allocation tests pass, including employee project roles, year-based project filtering, the allocated-project exception, admin access, and writing only real period-state transitions.
- PostgreSQL/Testcontainers `OvertimeServiceTest` passed after all 49 migrations through V1.3.0.23, including the allocation period history schema.
- Frontend type-check, targeted ESLint, and text-integrity checks passed.
- Focused frontend allocation checks passed, including role display/search, annual period selection, project closure subtitles, MDI closed-month headers, skipping `Without workstream` in both hierarchy modes, declined project/direction counts, full-width terminal-row highlighting, and consistent depth-based indentation; targeted Chromium E2E passes.
- `web/src/locales/ru.json` parses as JSON and `git diff --check` passes.

## Remaining

- Cell comment threads are not a built-in RevoGrid feature. RevoGrid supplies the cell renderer/interaction surface, but durable threads require a separate backend model, API, permissions, and comment UI; implementation awaits an explicit product decision.
- Decide how current allocations on a soft-deleted workstream are retired: analytics can still resolve the deleted stream, but annual input intentionally lists active workstreams only, so such cells cannot currently be set to zero in the UI.
- Recreate the local database after the edited allocation/workstream migrations, as agreed.
- After user acceptance, update `changelogs/CHANGELOG.md` and remove this task file.
