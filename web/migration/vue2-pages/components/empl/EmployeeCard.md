Component: src/components/empl/EmployeeCard.vue

Purpose
- Expanded employee card inside Employees list; shows profile summary, vacations, skills, tech profiles, and dialogs for project info/update.

Template/UI
- Layout: v-card with avatar uploader + v-list-item content.
- Displays:
  - employee.displayName
  - position name (if employee.position)
  - office location name + map preview button when officeLocation.mapName exists
  - telegram link using i18n key telegram_url with account param
  - current project info with optional project info dialog and edit button
  - current/future vacations chips
  - tech profiles chips (if allowed)
  - skills chips (if allowed)
- Dialogs:
  - Project info dialog (ProjectInfoCardComponent) when currentProject exists
  - Update current project dialog (EmployeeUpdateCurrentProject)
- MapPreviewComponent bound to previewMapAction data container.

Props
- employee (Employee, required).

State
- openUpdateCurrentProjectDialog (boolean)
- projectCardDialog (boolean)
- vacationsLoading (boolean)
- employeeVacations (EmployeeVacationShort[])
- previewMapAction (MapPreviewDataContainer)

Services / Utilities
- vacationService.currentOrFutureVacations(employee.id)
- DateTimeUtils.formatFromIso
- employeeService.getAvatarUrl (defined but unused in template)

Permissions / Visibility
- canUpdateCurrentProject(): permissionService.canUpdateCurrentProject(employee.id)
  - Controls visibility of edit button for current project.
- canDownloadTechProfiles(): permissionService.canDownloadTechProfiles(employee.id)
  - Controls visibility and loading of TechProfilesChips.
- canViewSkills(): permissionService.canViewEmplSkills(employee.id)
  - Controls visibility of SkillsChips.

Events
- Emits "employeeUpdated" when current project update dialog submits.

Lifecycle / Reactions
- mounted(): loadAdditionalData()
- watch employee: triggers loadAdditionalData after nextTick.
- loadAdditionalData(): loadVacations() then loadTechProfiles().

Dependencies
- Child components:
  - src/components/empl/EmployeeAvatarUploader.vue
  - src/components/empl/EmployeeUpdateCurrentProject.vue
  - src/components/empl/skills/SkillsChips.vue
  - src/components/empl/TechProfilesChips.vue
  - src/components/shared/ProjectInfoCardComponent.vue
  - src/components/admin/dict/office/maps/MapPreviewComponent.vue
- Data container: src/components/admin/dict/office/maps/MapPreviewDataContainer

Notes for Vue 3 migration
- Preserve permission checks for update project, tech profiles, and skills visibility.
- Keep vacations loading and chip formatting (formatFromIso, current flag -> primary color).
- Preserve map preview behavior: show only when officeLocation.mapName exists and pass workplace list.
- Maintain project info dialog and update current project flow + emit "employeeUpdated".
