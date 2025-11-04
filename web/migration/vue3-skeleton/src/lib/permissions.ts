import { computed } from "vue";
import { useAuthStore, type SecurityInfo } from "@/stores/auth";

export enum Permissions {
  /** Update avatar of any employees */
  UpdateAvatar = "update_avatar",
  /** Update current project of any employees */
  UpdateCurrentProjectGlobal = "update_current_project_global",
  /** Allow to update project when certain conditions are met */
  UpdateCurrentProject = "update_current_project",
  /** View overtimes of all employees */
  ViewOvertimes = "overtime_view",
  /** Edit overtimes of any employee */
  EditOvertimes = "overtime_edit",
  /** Approve overtimes of any employee */
  ApproveOvertimes = EditOvertimes,
  /** Export all overtimes for period */
  ExportOvertimes = EditOvertimes,
  /** Admin overtimes. Close overtime period */
  AdminOvertimes = "overtime_admin",
  /** View vacations of all employees */
  ViewVacations = "vacation_view",
  /** Edit vacations of any employee */
  EditVacations = "vacation_edit",
  /** Export all vacations */
  ExportVacations = EditVacations,
  /** Access to admin projects */
  AdminProjects = "project_admin_area",
  /** Admin user. Assign roles */
  AdminUsers = "admin_users",
  /** Edit skills of any employee */
  EditSkills = "edit_skills",
  /** Admin business account */
  EditBusinessAccount = "edit_business_account",
  /** Admin articles/news */
  EditArticles = "edit_articles",
  /** Admin salary requests */
  AdminSalaryRequests = "admin_salary_request",
  /** Report salary request */
  ReportSalaryRequest = "report_salary_request",
  /** Approve salary request */
  ApproveSalaryRequest = "approve_salary_request",
  /** View employee full data */
  AdminViewEmployeeFull = "view_employee_full",
  /** Create assessments */
  CreateAssessments = "create_assessment",
  /** View/modify juniors registry */
  AdminJuniorRegistry = "admin_junior_reg",
  AccessJuniorRegistry = "access_junior_reg",
  /** Upload tech profiles */
  UploadTechProfiles = "techprofile_upload",
  /** Download tech profiles */
  DownloadTechProfiles = "techprofile_download",
  /** Admin departments */
  AdminDictDepartments = "admin_department",
  /** Admin levels */
  AdminDictLevels = "admin_level",
  AdminDictOrganizations = "admin_organization",
  AdminDictPositions = "admin_position",
  AdminDictOffices = "admin_office",
  AdminDictOfficeLocations = "admin_office_location",
  /** View employee skills */
  ViewEmplSkills = "view_empl_skills",
  /** View current project role */
  ViewEmplCurrentProjectRole = "view_empl_current_project_role",
  /** Admin managers */
  AdminManagers = "admin_managers",
}

function hasAuthority(
  authorities: string[] | undefined,
  permission: Permissions,
): boolean {
  return Array.isArray(authorities) && authorities.includes(permission);
}

function hasAnyAuthority(
  authorities: string[] | undefined,
  permissions: Permissions[],
): boolean {
  if (!Array.isArray(authorities) || authorities.length === 0) {
    return false;
  }
  return permissions.some((permission) => authorities.includes(permission));
}

export function usePermissions() {
  const authStore = useAuthStore();
  const securityInfo = computed(() => authStore.securityInfo);

  function getSecurityInfo(): SecurityInfo | null {
    return securityInfo.value;
  }

  function simplePermissionCheck(permission: Permissions): boolean {
    const info = getSecurityInfo();
    return Boolean(info && hasAuthority(info.authorities, permission));
  }

  function simplePermissionCheckOrCurrentEmployee(
    permission: Permissions,
    employeeId: number,
  ): boolean {
    const info = getSecurityInfo();
    if (!info) {
      return false;
    }
    if (info.employeeId === employeeId) {
      return true;
    }
    return hasAuthority(info.authorities, permission);
  }

  function simplePermissionsCheckOrCurrentEmployee(
    permissions: Permissions[],
    employeeId: number,
  ): boolean {
    const info = getSecurityInfo();
    if (!info) {
      return false;
    }
    if (info.employeeId === employeeId) {
      return true;
    }
    return hasAnyAuthority(info.authorities, permissions);
  }

  function permissionCheckWithAccessToBa(
    permission: Permissions,
    ba: number,
  ): boolean {
    const info = getSecurityInfo();
    if (!info) {
      return false;
    }
    return (
      hasAuthority(info.authorities, permission) &&
      Array.isArray(info.accessibleBas) &&
      info.accessibleBas.includes(ba)
    );
  }

  function canUpdateCurrentProject(employeeId: number): boolean {
    return simplePermissionsCheckOrCurrentEmployee(
      [Permissions.UpdateCurrentProjectGlobal, Permissions.UpdateCurrentProject],
      employeeId,
    );
  }

  function canUpdateAvatar(employeeId: number): boolean {
    void employeeId;
    return simplePermissionCheck(Permissions.UpdateAvatar);
  }

  function canViewAllOvertimes(): boolean {
    return simplePermissionCheck(Permissions.ViewOvertimes);
  }

  function canViewOvertimes(employeeId: number): boolean {
    return simplePermissionCheckOrCurrentEmployee(
      Permissions.ViewOvertimes,
      employeeId,
    );
  }

  function canEditOvertimes(employeeId: number): boolean {
    return simplePermissionCheckOrCurrentEmployee(
      Permissions.EditOvertimes,
      employeeId,
    );
  }

  function canApproveOvertimeReport(employeeId: number): boolean {
    const info = getSecurityInfo();
    if (!info || info.employeeId === employeeId) {
      return false;
    }
    return simplePermissionCheck(Permissions.ApproveOvertimes);
  }

  function canExportOvertimes(): boolean {
    return simplePermissionCheck(Permissions.ExportOvertimes);
  }

  function canAdminOvertimes(): boolean {
    return simplePermissionCheck(Permissions.AdminOvertimes);
  }

  function canViewAllVacations(): boolean {
    return simplePermissionCheck(Permissions.ViewVacations);
  }

  function canCreateAssessments(): boolean {
    return simplePermissionCheck(Permissions.CreateAssessments);
  }

  function canExportAssessments(): boolean {
    return canCreateAssessments();
  }

  function canEditAllVacations(): boolean {
    return simplePermissionCheck(Permissions.EditVacations);
  }

  function canExportAllVacations(): boolean {
    return simplePermissionCheck(Permissions.ExportVacations);
  }

  function canAdminProjects(): boolean {
    return simplePermissionCheck(Permissions.AdminProjects);
  }

  function canAdminUsers(): boolean {
    return simplePermissionCheck(Permissions.AdminUsers);
  }

  function canAdminEmployees(): boolean {
    return simplePermissionCheck(Permissions.AdminViewEmployeeFull);
  }

  function canEditSkills(employeeId: number): boolean {
    return simplePermissionCheckOrCurrentEmployee(
      Permissions.EditSkills,
      employeeId,
    );
  }

  function canAdminBusinessAccounts(): boolean {
    return simplePermissionCheck(Permissions.EditBusinessAccount);
  }

  function canAdminArticles(): boolean {
    return simplePermissionCheck(Permissions.EditArticles);
  }

  function canAdminSalaryRequests(): boolean {
    return simplePermissionCheck(Permissions.AdminSalaryRequests);
  }

  function canDeleteSalaryRequests(requestCreator: number): boolean {
    return simplePermissionCheckOrCurrentEmployee(
      Permissions.AdminSalaryRequests,
      requestCreator,
    );
  }

  function canUpdateSalaryRequests(requestCreator: number): boolean {
    return simplePermissionCheckOrCurrentEmployee(
      Permissions.AdminSalaryRequests,
      requestCreator,
    );
  }

  function canReportSalaryRequest(): boolean {
    return simplePermissionCheck(Permissions.ReportSalaryRequest);
  }

  function canApproveSalaryRequest(ba: number): boolean {
    return (
      canAdminSalaryRequests() ||
      permissionCheckWithAccessToBa(Permissions.ApproveSalaryRequest, ba)
    );
  }

  function canAdminJuniorRegistry(): boolean {
    return simplePermissionCheck(Permissions.AdminJuniorRegistry);
  }

  function canAccessJuniorsRegistry(): boolean {
    return simplePermissionCheck(Permissions.AccessJuniorRegistry);
  }

  function canUpdateJuniorRegistryInfo(registryCreator: number): boolean {
    return simplePermissionCheckOrCurrentEmployee(
      Permissions.AdminJuniorRegistry,
      registryCreator,
    );
  }

  function canUpdateJuniorReport(reportCreator: number): boolean {
    return simplePermissionCheckOrCurrentEmployee(
      Permissions.AdminJuniorRegistry,
      reportCreator,
    );
  }

  function canUploadTechProfiles(employeeId: number): boolean {
    return simplePermissionCheckOrCurrentEmployee(
      Permissions.UploadTechProfiles,
      employeeId,
    );
  }

  function canDownloadTechProfiles(employeeId: number): boolean {
    return simplePermissionCheckOrCurrentEmployee(
      Permissions.DownloadTechProfiles,
      employeeId,
    );
  }

  function canAdminDictDepartments(): boolean {
    return simplePermissionCheck(Permissions.AdminDictDepartments);
  }

  function canAdminDictLevels(): boolean {
    return simplePermissionCheck(Permissions.AdminDictLevels);
  }

  function canAdminDictOrganizations(): boolean {
    return simplePermissionCheck(Permissions.AdminDictOrganizations);
  }

  function canAdminDictPositions(): boolean {
    return simplePermissionCheck(Permissions.AdminDictPositions);
  }

  function canAdminDictOfficeLocations(): boolean {
    return simplePermissionCheck(Permissions.AdminDictOfficeLocations);
  }

  function canAdminDictOffices(): boolean {
    return simplePermissionCheck(Permissions.AdminDictOffices);
  }

  function canViewEmplCurrentProjectRole(employeeId?: number): boolean {
    if (canAdminEmployees()) {
      return true;
    }
    if (typeof employeeId === "number") {
      return simplePermissionCheckOrCurrentEmployee(
        Permissions.ViewEmplCurrentProjectRole,
        employeeId,
      );
    }
    return simplePermissionCheck(Permissions.ViewEmplCurrentProjectRole);
  }

  function canViewEmplSkills(employeeId: number): boolean {
    return (
      canAdminEmployees() ||
      simplePermissionCheckOrCurrentEmployee(
        Permissions.ViewEmplSkills,
        employeeId,
      )
    );
  }

  function canAdminManagers(): boolean {
    return simplePermissionCheck(Permissions.AdminManagers);
  }

  return {
    canUpdateCurrentProject,
    canUpdateAvatar,
    canViewOvertimes,
    canEditOvertimes,
    canApproveOvertimeReport,
    canViewAllOvertimes,
    canViewAllVacations,
    canCreateAssessments,
    canExportAssessments,
    canEditAllVacations,
    canExportAllVacations,
    canExportOvertimes,
    canAdminOvertimes,
    canAdminProjects,
    canAdminEmployees,
    canAdminUsers,
    canEditSkills,
    canAdminBusinessAccounts,
    canAdminArticles,
    canAdminSalaryRequests,
    canDeleteSalaryRequests,
    canUpdateSalaryRequests,
    canReportSalaryRequest,
    canApproveSalaryRequest,
    canAdminJuniorRegistry,
    canAccessJuniorsRegistry,
    canUpdateJuniorRegistryInfo,
    canUpdateJuniorReport,
    canUploadTechProfiles,
    canDownloadTechProfiles,
    canAdminDictDepartments,
    canAdminDictLevels,
    canAdminDictOrganizations,
    canAdminDictPositions,
    canAdminDictOfficeLocations,
    canAdminDictOffices,
    canViewEmplSkills,
    canViewEmplCurrentProjectRole,
    canAdminManagers,
  };
}
