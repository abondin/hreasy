# Resource Allocations

## Goal

Add a manager-facing monthly resource allocation matrix with batch revisions and project-scoped editing.

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
- A Spring/Testcontainers test intended to apply Flyway could not run: sandbox access to the Docker named pipe was denied; the escalated retry then could not reach the configured Nexus to resolve Maven plugin artifacts. The migration still needs one execution in an environment with Docker and Nexus access.
- The repository skill validator could not start because `python.exe` and `py.exe` are unavailable; frontmatter and structure were checked manually.

## Open Questions

- No product questions are blocking. Apply the migration once in an environment with Docker/PostgreSQL and Nexus access before release.
