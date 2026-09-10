# Changelog

## 1.4.0 (Work in progress)

features:

- Added monthly employee resource allocations by project and workstream, with annual data entry, scoped project- and employee-based analytics, employee profile previews, period locking, change history, concurrent-edit conflict protection, and flat annual Excel export in percentages or person-months.
- Added per-cell resource-allocation comments in data entry and analytics, with shared read access and author-only edit/delete actions.
- Added a read-only external API for employees, avatar downloads by employee ID or email, project dictionaries, overtime summaries, and annual allocation analytics, with opaque Bearer tokens, acting-user permissions, deployment-owned network controls, and Swagger documentation.
- Moved Java backend services under `backend/`, added backend Maven reactor/parent/common modules, upgraded services to Spring Boot 4.0.5, and updated GitHub Actions/devops scripts.
- Implemented notification inbox UI and Yandex Messenger notification delivery.
- Disabled self-service current project updates for regular employees
- Added backend error contract for current project transfers that require approval.
- Added current project transfer approver candidates for approval-required transfers.
- Manager assignments for projects, business accounts, and departments now contribute to effective manager-scoped access in addition to manual user access settings.
- Full migration from vue2 to vue3 

bugfix:

- Fixed menu navigation from an open employee details panel returning users to the employee directory; manual panel closure preserves search filters.
- Allowed manager-link deletion by its creator or an administrator and surfaced backend deletion errors in the confirmation dialog.
- Prevented Telegram API authentication from creating a reusable web session and hardened file storage against unsafe filenames and filesystem changes before authorization.

technical:

- Updated compatible frontend dependencies and retained TypeScript 6 until the Vue toolchain supports TypeScript 7.


## 1.3.1 (2026-03-21)

Final web release on Vue 2, available under the `/old` base URL.
The next web release will be built on Vue 3.`

## 1.3.0 (2026-03-21)

- Employee import from Excel extended (including organization data import).
- Salary request workflow expanded (report/approve/implement/bonuses/history/links).
- New page with employees and their latest salary request.
- Vacation planning upgraded (calendar/timeline, request form updates, cancel flow).
- Office locations map added and refined (CRUD, employee details, pan/zoom).
- Support request flow improved (categories and email template updates).
- Telegram bot capabilities added and daily activity logging introduced.
- Juniors module expanded (extended ratings, markdown progress report, Excel export).
- Employee/project UX updates (adaptive project card, BA managers, project history, avatar in admin form).
- Access/validation/security fixes across key HR flows.

## v1.2.0 (2022-11-08)

- Internal users support.
- Role on current project.
- Project card with detailed description.
- Managers support (projects, business accounts, departments).
- Working day calendar.
- Technical: TypeScript update and EC2020 migration on web.

## RELEASE_1.1.0 (2022-06-05)

- Full migration from SQL Server to PostgreSQL.
- Vacation notification feature.
- Technical profile card upload feature.
- Dictionary admin pages.
- Telegram account setup in employee profile.

## v1.0.0 / SQL_SERVER_LAST_UPDATE (2021-11-16)

- Last version with SQL Server.
- Docker images published for platform and web.
