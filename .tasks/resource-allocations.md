# Resource Allocations

## Goal

Add a manager-facing monthly resource allocation matrix with batch revisions and project-scoped editing.

Current UX iteration: make editable cells obvious, allow clearing a cell or employee row, add separate employee/project filters, and keep employee names readable.

Current performance/visual iteration: improve the empty allocated-only state, widen employee names, separate fixed columns visually, and remove matrix-wide recomputation from cell edits.

Current input/navigation iteration: add project search, render one active editor instead of an input in every cell, support spreadsheet-style keyboard movement, and move Assessments into the Managers menu group.

Current stability/performance iteration: keep project columns unchanged while editing and reduce filter/edit work on large populated sheets.

## Agreed Scope

- Add `Managers > Resource allocations` to the navigation.
- Work with one month at a time, using the existing zero-based `YYYYMM` period convention (`202005` means June 2020) and period switcher.
- Show employees as rows and projects as columns.
- Show each employee's total allocation: green at 100%, yellow below 100%, red above 100%.
- Accept integer percentages from 0 through 1000. Zero or blank removes the allocation.
- Show all project allocations, but disable cells outside the current user's project access.
- Save only changed cells in one transactional revision.
- Prefer the existing `ProjectHierarchyAccessor` even though its effective scope includes both manager-derived and manually granted project hierarchy access.
- Keep the implementation small and reuse existing backend and frontend patterns.

## Current Decisions

- Access requires a dedicated allocation permission plus project hierarchy access. `global_admin` has unrestricted access.
- A manager may allocate any employee to a project they can access; employee hierarchy does not restrict editing.
- Totals include allocations hidden by project filters.
- Default project view is the user's accessible projects; an all-project view shows other projects read-only.
- Save uses changed-cell semantics so unrelated project values are never replaced.
- Revisions retain the changes made by one Save, including removals.
- Allocation revision/change tables implement the feature's batch history requirement; there is no shared user-action audit integration.
- Follow repository naming: `*View` for SQL projections, `*Dto` for responses, and `*Body` for request payloads; no persistence `*Entry` is needed while allocation writes use one custom SQL repository.
- Keep permission rules in `ResourceAllocationSecurityValidator`; the service orchestrates the use case and the backend remains authoritative.
- Log save attempts briefly (period, actor, change count) without logging the full allocation payload.
- The agreed naming, repository, security-validator, logging, and history-separation practices are recorded in `.agents/skills/java-coding-style/SKILL.md`.
- Lombok is already used where it removes boilerplate (`@RequiredArgsConstructor`, `@Slf4j`). Allocation API values and SQL projections remain small immutable records; converting them to mutable Lombok beans would add code without behavior.
- MapStruct is reserved for mappings that are reused or structurally non-trivial. Allocation mapping stays explicit because it is local and includes runtime `active`/`editable` decisions; a dedicated mapper would add a file and dependency without simplifying the flow.
- The agreed Lombok/record and MapStruct selection rules are recorded in `.agents/skills/java-coding-style/SKILL.md`.
- Main allocation classes and public methods have concise Javadoc; the same documentation rule is recorded in the Java skill.
- Dynamic allocation columns exposed a shared table issue: forwarded slot names were cached before async data created the slots. `HREasyTableBase` now resolves slot names on render.
- Same-cell concurrent edits use last-write-wins for the first version; add optimistic checks only if real collisions appear.
- No period closing, approval workflow, notifications, comments, or import/export in the first version.
- Cell and employee-row clearing remain local edits until Save, like individual cell changes, and affect editable projects only. Project-column clearing is deferred because its header action reduced space for project names.
- Employee and project filters are separate compact groups. Employee filters contain name search, `All / From my projects`, and an independent BA multi-select; project filters contain `Mine / All`, an independent BA multi-select, and `Only with allocations`.
- `Only with allocations` keeps rows and project columns that contain a non-zero visible allocation and hides empty cell editors; switch it off to create new allocations.
- The allocation matrix is the bounded exception to the shared `HREasyTableBase`: RevoGrid Community virtualizes both axes and supplies spreadsheet selection, editing, clipboard, and autofill behavior. The surrounding page still uses the shared HR Easy layouts and Vuetify controls.
- Employee BA membership is derived from `employee.currentProjectId -> project.baId`; no backend contract change is needed for the first filter version.
- The employee column is 320px, the compact `Total` column is 90px, and a native Vuetify cell border separates the two fixed columns.
- Allocated-only mode uses a centered `v-empty-state` instead of leaving an empty table fragment on screen.
- Current values and allocation indexes are derived in one sparse pass over saved allocations and edits; row/project filters no longer rescan the employee-project matrix.
- Employee-row clearing follows the neighboring manager-table action pattern: a reserved 24px slot, `mdi-delete`, error color, and hover/focus visibility. The fixed slot prevents virtual-table columns from shifting when the action appears.
- Project search is independent from employee search and applies before project scope/BA/allocation filters.
- RevoGrid owns cell activation and the default Tab/Enter navigation; the page only validates percentages and records changed cells.
- Search fields use the shared `normalizeSearchInput` helper so Vuetify clear events cannot leave a null search value.
- Assessments and Resource Allocations are permission-controlled children of the Managers navigation group.
- Project columns use fixed 130px constraints. Native Delete/Backspace clears selected cells; no per-cell clear control is rendered.
- The API sheet is a `shallowRef` because nested response data is immutable on the page. Empty searches skip per-item lowercase conversion.
- Allocation statistics process unchanged server values plus overriding edits directly; they no longer allocate and scan a second merged map on every commit.
- The frequently repeated cell-clear control is a native accessible button with the existing MDI font icon instead of hundreds of hidden `v-btn` component instances.
- The RevoGrid Community proof of concept was accepted after user testing. One narrow adapter creates a 1x1 range when the native autofill handle is hovered, removing the library's Shift prerequisite without replacing its autofill implementation.
- Projects are visible when active or when they have a non-zero allocation in the selected month; archived empty projects are omitted.
- Pending edits use the shared `ConfirmDeleteDialog` pattern for refresh, period changes, and route navigation. A route-leave guard blocks menu navigation until the user cancels or confirms; confirming clears the local draft before leaving so KeepAlive activation cannot reopen a stale dialog.
- `All projects` is grouped under `Managers` instead of `Admin`; its existing route and `project_admin_area` permission remain unchanged.
- Employee scope defaults to `From my projects`. Both filter groups now also have a project multi-select; its options follow that group's selected BAs, and clearing all BAs restores every project eligible for the selected month. Selections that no longer belong to the chosen BAs are removed.
- Project columns use RevoGrid Community's native nested headers and are grouped one level by BA without collapse/expand. The project set stays stable while editing so RevoGrid preserves its default keyboard navigation; it is refreshed after load/save.
- Neighboring employee-oriented pages consistently use `v-autocomplete`, `multiple`, `clearable`, compact outlined styling, and shared `CollapsedSelectionContent` for project filters. The markup and local filtering are repeated across pages, but BA and project filters are independent there; BA-dependent project options are currently allocation-specific. Do not extract a shared filter until behavior is intentionally standardized.
- Allocation projects expose `managed` separately from `editable`. `managed` comes only from manager assignments on the project, its BA, or its department and drives both `Mine` filters; effective access, including explicit grants, continues to drive editability.
- Top-level `My projects` / `Business accounts` / `Company` are presets for both independent axes. A preset clears both axes' detailed selections and the employee search, but preserves unsaved edits and `Only with allocations`. Changing one axis clears only that axis when leaving `Select`; changing its BA prunes only invalid project selections.
- The two axis-local searches were replaced by one standard top-toolbar employee text search. Project search is deferred.
- `Only with allocations` is a global display mode and lives with period, presets, employee search, and Save. The combined mode block wraps as one toolbar item on narrower screens.
- Project headers expose their full names through native header titles. Untouched empty cells show the previous month's value at reduced opacity; hints do not affect totals, saved changes, or allocated-only filtering.
- The toolbar exposes both Save and Cancel Changes. Cancel uses the same standard unsaved-changes dialog as navigation/period changes. Employee-row clearing uses a dedicated standard `ConfirmDeleteDialog`; browser-native confirmation is no longer used.
- Allocation saves use per-cell optimistic revisions. The request carries the revision observed for each edited cell; saves for one period are serialized with a PostgreSQL transaction advisory lock and every write remains revision-conditional.
- A conflict rejects and rolls back the whole revision with HTTP 409. The response contains the current server allocations, conflicting cell identifiers, and the non-conflicting remainder of the submitted draft. The frontend applies that response without another GET: server values replace conflicts while the remaining draft stays editable.

## Plan

- [x] Inspect period, project hierarchy, manager, and role patterns.
- [x] Agree on the minimal access approach and task workflow.
- [x] Add the Flyway migration and allocation permission.
- [x] Add backend read/save API with transactional revision history.
- [x] Add focused backend tests for period handling, changed-only saves, and access flags.
- [x] Add the frontend service, route, menu group, and allocation table.
- [x] Add a focused frontend test for totals across hidden projects.
- [x] Add a Playwright app-mocked flow for project access, totals, editing, and changed-only save.
- [x] Run final backend and frontend validation available in the current environment.
- [x] Improve editable-cell affordance and employee-column sizing.
- [x] Add cell and row clear actions with focused tests; defer the project-column action.
- [x] Fix sticky column offsets and keep native horizontal scrolling.
- [x] Add separate employee/project filters and the only-allocated mode.
- [x] Validate the UX iteration.
- [x] Improve empty-state and fixed-column visual hierarchy.
- [x] Precompute sparse allocation values/totals/indexes instead of rescanning the matrix.
- [x] Exercise the page with a large mocked sheet and rerun validation.
- [x] Add project search and single-cell editing with keyboard navigation.
- [x] Move Assessments into the Managers navigation group.
- [x] Review the allocation page against neighboring web sections and document intentional differences.
- [x] Fix cell/column width stability and optimize the populated-matrix render path.
- [x] Evaluate current Excel-like Vue grids against the allocation UX, performance, licensing, and architecture.
- [x] Replace the allocation matrix with the bounded RevoGrid Community implementation and retain changed-only persistence.
- [x] Use native Delete, default Tab/Enter, clipboard, range selection, and autofill without Shift.
- [x] Hide archived projects without allocations in the selected month.
- [x] Replace the unsaved-changes browser prompt with the standard Vuetify dialog pattern.
- [x] Move `All projects` from the Admin navigation group to Managers.
- [x] Default employees to the user's projects and add BA-dependent project multi-selects for employee rows and project columns.
- [x] Assess BA column grouping and collapse support in the installed RevoGrid version.
- [x] Group project columns by BA using native RevoGrid Community headers, without collapsing.
- [x] Audit neighboring project filters for shared behavior and duplication.
- [x] Separate manager-derived project membership from effective edit access for `Mine` filters.
- [x] Replace the ambiguous always-visible filters with presets, progressive per-axis selection, and one employee search.
- [x] Add project-header titles and previous-period cell hints.
- [x] Replace row-clearing browser confirmation and add explicit cancellation of local changes.
- [x] Add transaction-safe per-cell optimistic locking and automatic conflict rebase without a second load.

## Validation

- `mvn -q -f backend/pom.xml -pl platform -am -DskipTests compile` passed.
- `mvn -q -f backend/pom.xml -pl platform -am -Dtest=ResourceAllocationServiceTest "-Dsurefire.failIfNoSpecifiedTests=false" test` passed (3 tests), including backend rejection of an inaccessible project.
- `npm run type-check` passed.
- `npm run lint` passed after fixing three local lint findings.
- `npm run test:unit -- --run ResourceAllocationsView` passed (1 test).
- `npm run build` passed; Vite reported only the repository's existing large-chunk warning.
- `npm run build-only` passed after the E2E selectors and dynamic-slot fix; only the existing large-chunk warning remains.
- Targeted ESLint for the allocation E2E, view, shared table, and support files passed.
- `npm run test:e2e -- app-mocked/resource-allocations-page.spec.ts --reporter=line` passed (1 Chromium test) without a backend.
- `npm run check:win-text-integrity` passed.
- UX iteration: full `npm run type-check`, `npm run lint`, `npm run test:unit -- --run` (7 files / 19 tests), `npm run build-only`, and `npm run check:win-text-integrity` passed.
- `npm run test:e2e -- app-mocked/resource-allocations-page.spec.ts --reporter=line` passed after installing the Chromium version required by the updated Playwright. It verifies the 320px employee column, no fixed-column overlap, cell clearing, employee/BA/only-allocated filters, access behavior, and changed-only save.
- After the filter iteration, full `npm run type-check`, `npm run lint`, `npm run test:unit -- --run` (7 files / 19 tests), `npm run build-only`, and `npm run check:win-text-integrity` passed again.
- Visual/performance iteration: targeted type-check, ESLint, and allocation unit tests passed. The Chromium allocation E2E has a 500-employee × 80-project scenario covering single-editor rendering, a 2-second edit-response budget, a 2-second employee-BA filter budget, and the allocated-only empty state.
- After that iteration, full `npm run type-check`, `npm run lint`, `npm run test:unit -- --run` (7 files / 19 tests), `npm run build-only`, and `npm run check:win-text-integrity` passed. Vite reported only the existing large-chunk warning.
- Row-action iteration: project-header clearing was removed, employee clearing now reuses the neighboring manager-table action pattern, and the allocation E2E asserts that revealing the row action does not move the first project column. Type-check, targeted ESLint, 2 allocation unit tests, and both Chromium allocation scenarios passed.
- Input/navigation/menu iteration: project search, click-to-edit cells, Tab/Enter movement, and Assessments under Managers pass both Chromium allocation scenarios. Full lint, 7 unit files / 19 tests, production build, and Windows text-integrity validation passed; Vite reports only the existing large-chunk warning.
- Stability/performance iteration: Chromium verifies identical cell and project-column widths before, during, and after editing. The large scenario now contains 500 employees, 80 projects, and 5,000 non-zero allocations; activation, commit, and employee-BA filtering each stay within a 2-second budget. The dense scenario passed twice consecutively, and the full two-scenario allocation E2E passed.
- After the stability/performance changes, full lint, 7 unit files / 19 tests, production build, and Windows text-integrity validation passed. Vite reports only the existing large-chunk warning.
- RevoGrid iteration: full type-check, lint, 7 unit files / 19 tests, production build, Windows text-integrity check, and both Chromium allocation scenarios passed. The E2E covers default Tab/Enter, native Delete, autofill without Shift, archived-project visibility, fixed column widths, and the 500-employee / 80-project / 5,000-allocation case. Vite reports only the existing large-chunk warning.
- Unsaved-dialog iteration: the shared dialog now guards internal actions and Vue Router navigation. Targeted unit tests pass; Chromium verifies cancel, confirm, navigation to Employees, and returning without a stale dialog.
- Navigation grouping iteration: type-check, targeted lint, and the Chromium drawer scenario passed; the scenario verifies `All projects` under Managers for a user with `project_admin_area`.
- Project-filter iteration: type-check, targeted ESLint, 3 allocation unit tests, and both Chromium allocation scenarios passed. Tests cover the default employee scope, BA-dependent project options, independent employee/project filtering, and the existing 500-employee / 80-project responsiveness case.
- BA-grouping iteration: type-check, targeted ESLint, 3 allocation unit tests, and both Chromium allocation scenarios passed. Chromium exposed and verified the fix for column recreation breaking default Enter navigation; project visibility is now stable during edits.
- Managed-scope iteration: the targeted `ResourceAllocationServiceTest`, frontend type-check, targeted ESLint, 3 allocation unit tests, and both Chromium allocation scenarios passed. The backend test verifies that effective edit access and manager-derived membership are independent.
- Filter/past-period iteration: the targeted backend service test, frontend type-check, targeted ESLint, 3 allocation unit tests, and both Chromium allocation scenarios passed. Tests cover January-to-December previous-period calculation, preset resets, employee search, progressive BA/project filters, header titles, and previous-value hints.
- A Spring/Testcontainers test intended to apply Flyway could not run: sandbox access to the Docker named pipe was denied; the escalated retry then could not reach the configured Nexus to resolve Maven plugin artifacts. The migration still needs one execution in an environment with Docker and Nexus access.
- The repository skill validator could not start because `python.exe` and `py.exe` are unavailable; frontmatter and structure were checked manually.

## Open Questions

- Apply the migration once in an environment with Docker/PostgreSQL and Nexus access before release.
- A shared employee/project scope was rejected: allocating an employee from an unrelated source project to a managed target project requires asymmetric axes. Proposed UX keeps two semantic controls with progressive disclosure: `Employees` (`From my projects` / `All` / `Select source`) and `Allocation projects` (`Mine` / `Business account` / `All`). BA/project selectors appear only for the modes that need them; employee and project searches remain axis-local.
