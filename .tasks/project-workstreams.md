# Project external IDs and workstreams

## Goal

- Add an optional string external ID to projects and expose/edit it in admin and external APIs.
- Add project workstreams with soft deletion and admin editing.
- Expose active workstreams nested in external project responses.
- Allow an optional workstream when recording overtime and resource allocations.

## Decisions

- Reuse project admin permissions for workstream mutations.
- Save workstreams atomically with the project form; removing a row sets `deleted_at` in the same transaction.
- Keep workstream API fields to `id`, `externalId`, `displayName`, and `description`; persistence also carries the required audit and soft-delete columns.
- Workstream selection is valid only when the workstream is active and belongs to the selected project.
- Existing overtime and allocation rows remain project-level with a null workstream.
- Detailed overtime items resolve the workstream display name even after soft deletion; creation dictionaries remain active-only.
- Project external IDs are optional and unique when present. Workstream external IDs are optional and unique among active workstreams within one project.

## Progress

- [x] Verified that projects currently have no external ID.
- [x] Traced project admin CRUD, external project API, overtime items, and allocation persistence/UI entry points.
- [x] Confirmed allocation workstream cardinality semantics.
- [x] Added migration and backend persistence/contracts.
- [x] Added project admin UI and selectors in overtime/allocation UI.
- [x] Replaced the project workstream editor with a compact table and adopted the UI term “Направление работ”.
- [x] Top-aligned workstream table cells so validation messages do not shift one input above neighboring fields.
- [x] Show only workstream names in the employee project dialog.
- [x] Show active workstreams in a full administrative project table with name, external ID, and description.
- [x] Keep the administrative workstream table inside the project's primary information card, below its summary and description.
- [x] Kept overtime summaries aggregated by date/project; detailed reports expose a separate direction column.
- [x] Added workstream hierarchy to allocation analytics and nested active workstreams to external projects.
- [x] Updated durable documentation and focused tests.

## Validation

- Focused backend suite passed: project/workstream admin mapping, request deserialization, allocation workstream validation, and external controller/authentication checks.
- PostgreSQL/Testcontainers `OvertimeServiceTest` passed: Flyway applied V1.3.0.23, detailed overtime resolves a soft-deleted workstream name, and the summary aggregates directed and undirected hours.
- Frontend type-check, targeted ESLint, 11 focused unit tests, and 4 Chromium E2E scenarios passed.
- `web/src/locales/ru.json` parses as JSON and `git diff --check` passes.

## Open questions

- Should a soft-deleted workstream with current allocation cells remain selectable for cleanup, or must project editing reject deletion until its current allocations are zero?

Project-level and multiple workstream-level allocation cells may coexist for the same employee, month, and project.
