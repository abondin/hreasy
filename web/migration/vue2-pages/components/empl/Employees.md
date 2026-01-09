Component: src/components/empl/Employees.vue

Purpose
- Shows employees list with filters and expandable rows that render EmployeeCard.

Template/UI
- Vuetify v-card with header filters: search text, current project (multi), business account (multi).
- v-data-table with expand rows enabled on smAndUp; single-expand; no pagination/footer.
- Expanded row renders <employee-card> and listens to @employeeUpdated to refresh list.
- Custom cell for currentProject shows project name and role (if present).

Inputs/State
- Local state: headers (DataTableHeader[]), loading (boolean), employees (Employee[]), filtered (Employee[]).
- Filter model (local class Filter): selectedProjects (Array<number | null>), selectedBas (number[]), search (string).
- allProjects: array of objects used by project autocomplete (includes "Без проекта" entry + divider + active projects).

Vuex / Store
- Getters from dict namespace:
  - businessAccounts -> allBas
  - positions -> allPositions (not used in template)
  - projects -> projects (used to build allProjects)
- Dispatches in created(): reloadBusinessAccounts, reloadProjects, reloadSkillGroups, reloadSharedSkills, reloadPositions, reloadCurrentProjectRoles.

Services / Utilities
- employeeService.findAll() to load employees.
- searchUtils.array + searchUtils.textFilter; TextFilterBuilder for text search across displayName, position.name, currentProject.role, telegram, email, skills[].name.

Permissions / Visibility
- Columns: adds "Роль на проекте" header only if permissionService.canViewEmplCurrentProjectRole().
- Filtering/search includes currentProject.role and skills; visibility of those columns/rows determined elsewhere.

Lifecycle / Reactions
- created(): sets headers, loads employees, reloads dicts, builds allProjects list.
- Watcher on filter (deep) recomputes filtered list.

Dependencies
- Child component: src/components/empl/EmployeeCard.vue.
- Uses dict data and permissionService from store/modules/permission.service.

Notes for Vue 3 migration
- Keep same filter behavior (project/BA filters + text search across listed fields).
- Preserve dict reload sequence and the "Без проекта" option in project filter.
- Respect permission-based header addition for current project role.
- Maintain expanded row behavior and @employeeUpdated -> refresh list.
