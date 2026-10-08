# Changelog

## 1.4.0 (Work in progress)

features:

- Added annual resource planning by project and workstream, with data entry, analytics, period locking, change history, conflict protection, cell comments, Excel export, and a project setting requiring workstream selection.
- Added a read-only external API for employees, avatars, projects, overtime, and allocations, using email and configurable external keys, Bearer authentication, acting-user permissions, and OpenAPI documentation.
- Employee Excel exports include a separate Children worksheet; Skype is no longer exported.
- Added a notification inbox and Yandex Messenger delivery.
- Current-project transfers support approval by an eligible manager; regular employees cannot change their project directly.
- Manager assignments on projects, business accounts, and departments contribute to access scope alongside manual user access settings.
- Project details include current employees, and employee project roles no longer require a separate viewing permission.
- Migrated the main web application from Vue 2 to Vue 3.

bugfix:

- Fixed manager assignments that their creators could add but could not delete; creators and administrators can now remove them.

technical:

- Backend services require Java 25 and use Spring Boot 4. Building the web frontend requires Node.js 26 and npm 11.19.1.
- Fixed security issues present in 1.3: Telegram API authentication could create a reusable web session, and file storage lacked filename protection and could modify files before authorization.


## 1.3.1 (2026-03-21)

Final web release on Vue 2, available under the `/old` base URL.
The next web release will be built on Vue 3.

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
