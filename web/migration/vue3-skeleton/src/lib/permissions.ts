import {computed} from 'vue';
import {useAuthStore} from '@/stores/auth';

export enum Permissions {
  /** Update avatar of any employees */
  UpdateAvatar = 'update_avatar',
  /** Update current project of any employees */
  UpdateCurrentProjectGlobal = 'update_current_project_global',
  /** Allow to update project when certain conditions are met */
  UpdateCurrentProject = 'update_current_project',
  /** View overtimes of all employees */
  ViewOvertimes = 'overtime_view',
  /** Edit overtimes of any employee */
  EditOvertimes = 'overtime_edit',
  /** Approve overtimes of any employee */
  ApproveOvertimes = 'overtime_edit',
  /** Export all overtimes for period */
  ExportOvertimes = 'overtime_edit',
  /** Admin overtimes. Close overtime period */
  AdminOvertimes = 'overtime_admin',
  /** View vacations of all employees */
  ViewVacations = 'vacation_view',
  /** Edit vacations of any employee */
  EditVacations = 'vacation_edit',
  /** Export all vacations */
  ExportVacations = 'vacation_edit',
  /** Access to admin projects */
  AdminProjects = 'project_admin_area',
  /** Admin user. Assign roles */
  AdminUsers = 'admin_users',
  /** Edit skills of any employee */
  EditSkills = 'edit_skills',
  /** Admin business account */
  EditBusinessAccount = 'edit_business_account',
  /** Admin articles/news */
  EditArticles = 'edit_articles',
  /** Admin salary requests */
  AdminSalaryRequests = 'admin_salary_request',
  /** Report salary request */
  ReportSalaryRequest = 'report_salary_request',
  /** View/modify juniors registry */
  AdminJuniorRegistry = 'admin_junior_reg',
  AccessJuniorRegistry = 'access_junior_reg',
  /** Upload tech profiles */
  UploadTechProfiles = 'techprofile_upload',
  /** Download tech profiles */
  DownloadTechProfiles = 'techprofile_download',
  /** Admin departments */
  AdminDictDepartments = 'admin_department',
  /** Admin levels */
  AdminDictLevels = 'admin_level',
  AdminDictOrganizations = 'admin_organization',
  AdminDictPositions = 'admin_position',
  AdminDictOffices = 'admin_office',
  AdminDictOfficeLocations = 'admin_office_location',
  /** View employee skills */
  ViewEmplSkills = 'view_empl_skills',
  /** View current project role */
  ViewEmplCurrentProjectRole = 'view_empl_current_project_role',
  /** Admin managers */
  AdminManagers = 'admin_managers'
}

function hasAuthority(authorities: string[] | undefined, permission: Permissions): boolean {
  return Array.isArray(authorities) && authorities.includes(permission);
}

export function usePermissions() {
  const authStore = useAuthStore();
  const securityInfo = computed(() => authStore.securityInfo);

  function hasPermission(permission: Permissions): boolean {
    const info = securityInfo.value;
    if (!info) {
      return false;
    }
    return hasAuthority(info.authorities, permission);
  }

  return {
    hasPermission
  };
}
