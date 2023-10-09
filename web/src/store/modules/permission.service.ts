import store from "@/store";
import {SecurityInfo} from "@/store/modules/auth";

export enum Permissions {
    /**
     * Update avatar of any employees
     */
    UpdateAvatar = "update_avatar",

    /**
     * Update current project of any employees
     */
    UpdateCurrentProjectGlobal = "update_current_project_global",

    /**
     * Allow to update project with update_current_project permission if logged in user is
     * 1. Manager of whole department
     * 2. Manager of old project (if exists) and new project
     */
    UpdateCurrentProject = "update_current_project",

    /**
     * View overtimes of all employees
     */
    ViewOvertimes = "overtime_view",


    /**
     * Edit overtimes of any employee
     * (if logged user is employee assigned or overtime reported project manager)
     */
    EditOvertimes = "overtime_edit",

    /**
     * Approve overtimes of any employee.
     * (if logged user is employee assigned or overtime reported project manager)
     * (= EditOvertimes)
     */
    ApproveOvertimes = "overtime_edit",

    /**
     * Export all overtimes for period
     * (= EditOvertimes)
     */
    ExportOvertimes = "overtime_edit",

    /**
     * Admin overtimes. Close overtime period
     */
    AdminOvertimes = "overtime_admin",

    /**
     * View vacations of all employees
     */
    ViewVacations = "vacation_view",

    /**
     * Edit vacations of any employee
     */
    EditVacations = "vacation_edit",

    /**
     * Export all vacations
     */
    ExportVacations = "vacation_edit",

    /**
     * Access to see all projects and admin them
     */
    AdminProjects = "project_admin_area",

    /**
     * Admin user. Assign roles. Assign accessible projects and departments
     */
    AdminUsers = "admin_users",

    /**
     * Edit skills of any employee
     */
    EditSkills = "edit_skills",

    /**
     * Add/Update business account and create/update BA positions
     */
    EditBusinessAccount = "edit_business_account",

    /**
     * Create/update and moderate articles and news
     */
    EditArticles = "edit_articles",

    /**
     * View all salaries requests
     */
    AdminSalaryRequests = "admin_salary_request",

    /**
     * Report salary request. View own requests
     */
    ReportSalaryRequest = "report_salary_request",

    /**
     * View employee all fields including personal
     */
    AdminViewEmployeeFull = "view_employee_full",

    /**
     * View last assessment date for employee. Schedule new assessment and invite managers
     */
    CreateAssessments = "create_assessment",

    /**
     * Only logged in user or user with permission techprofile_download can download tech profile
     */
    DownloadTechProfiles = "techprofile_download",

    /**
     * Only logged in user or user with permission techprofile_upload can upload or delete tech profile
     */
    UploadTechProfiles = "techprofile_upload",

    /**
     * Only user with permission admin_department can admin department
     */
    AdminDictDepartments = "admin_department",

    /**
     * Only user with permission admin_level can admin level
     */
    AdminDictLevels = "admin_level",

    /**
     * Only user with permission admin_department can admin position
     */
    AdminDictPositions = "admin_position",

    /**
     * Only user with permission admin_department can admin office location
     */
    AdminDictOfficeLocations = "admin_office_location",

    /**
     * View current project role
     */
    ViewEmplCurrentProjectRole = "view_empl_current_project_role",

    /**
     * View employee skills
     */
    ViewEmplSkills = "view_empl_skills",
    /**
     * Admin managers of departments, business accounts and projects
     */
    AdminManagers = "admin_managers",

}

interface PermissionService {

    /**
     * Check if given user can update project of given employee
     * @param employeeId - employee to change current project
     */
    canUpdateCurrentProject(employeeId: number): boolean

    /**
     * Check if given user can update project of given employee
     * @param employeeId - employee to change current project
     */
    canUpdateAvatar(employeeId: number): boolean;

    /**
     * Check if user can view overtime for given employee
     * @param employeeId
     */
    canViewOvertimes(employeeId: number): boolean;

    /**
     * Check if user can update overtime for given employee
     * @param employeeId
     */
    canEditOvertimes(employeeId: number): boolean;

    /**
     * Check if user can approve or decline overtime report for
     * given employee
     * @param employeeId
     */
    canApproveOvertimeReport(employeeId: number): boolean;

    canViewAllOvertimes(): boolean;

    canViewAllVacations(): boolean;

    canCreateAssessments(): boolean;

    canEditAllVacations(): boolean;

    canExportAllVacations(): boolean;

    canExportOvertimes(): boolean;

    canAdminOvertimes(): boolean;

    canAdminProjects(): boolean;

    canAdminEmployees(): boolean;

    canAdminUsers(): boolean;

    canEditSkills(employeeId: number): boolean;

    canAdminBusinessAccounts(): boolean;

    canAdminArticles(): boolean;

    canAdminSalaryRequests(): boolean;

    canReportSalaryRequest(): boolean;

    /**
     * Check if given user can download employee's tech profiles
     * @param employeeId
     */
    canDownloadTechProfiles(employeeId: number): boolean

    /**
     * Check if given user can upload employee's tech profiles
     * @param employeeId
     */
    canUploadTechProfiles(employeeId: number): boolean

    /**
     * Check if given user has grants to CRUD operations on employees departments
     */
    canAdminDictDepartments(): boolean;

    /**
     * Check if given user has grants to CRUD operations on employees levels
     */
    canAdminDictLevels(): boolean;

    /**
     * Check if given user has grants to CRUD operations on employees positions
     */
    canAdminDictPositions(): boolean;

    /**
     * Check if given user has grants to CRUD operations on office locations
     */
    canAdminDictOfficeLocations(): boolean;

    /**
     * Has access to view employee's skills
     */
    canViewEmplSkills(employeeId?: number): boolean;

    /**
     * Has access to view employee's current project role
     * @param employeeId
     */
    canViewEmplCurrentProjectRole(employeeId?: number): boolean;

    /**
     * Has access to export all assessments summaries
     */
    canExportAssessments(): boolean;

    /**
     * Has access to manager admin page
     */
    canAdminManagers(): boolean;
}

const namespace = 'auth';

class VuexPermissionService implements PermissionService {


    public canUpdateCurrentProject(employeeId: number): boolean {
        return this.simplePermissionsCheckOrCurrentEmployee([Permissions.UpdateCurrentProjectGlobal,
            Permissions.UpdateCurrentProject], employeeId);
    }

    public canUpdateAvatar(employeeId: number): boolean {
        return this.simplePermissionCheck(Permissions.UpdateAvatar);
    }

    canViewAllOvertimes(): boolean {
        return this.simplePermissionCheck(Permissions.ViewOvertimes);
    }

    canViewAllVacations(): boolean {
        return this.simplePermissionCheck(Permissions.ViewVacations);
    }

    canCreateAssessments(): boolean {
        return this.simplePermissionCheck(Permissions.CreateAssessments);
    }

    canExportAssessments(): boolean {
        return this.canCreateAssessments();
    }

    canViewOvertimes(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.ViewOvertimes, employeeId);
    }

    canEditAllVacations(): boolean {
        return this.simplePermissionCheck(Permissions.EditVacations);
    }

    canExportAllVacations(): boolean {
        return this.simplePermissionCheck(Permissions.ExportVacations);
    }

    canEditOvertimes(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.EditOvertimes, employeeId);
    }

    canApproveOvertimeReport(employeeId: number): boolean {
        const securityInfo: SecurityInfo = store.getters['auth/securityInfo'];
        if (securityInfo && securityInfo.employeeId == employeeId){
            return false;
        }
        return this.simplePermissionCheck(Permissions.ApproveOvertimes);
    }

    canExportOvertimes(): boolean {
        return this.simplePermissionCheck(Permissions.ExportOvertimes);
    }

    canAdminOvertimes(): boolean {
        return this.simplePermissionCheck(Permissions.AdminOvertimes);
    }

    canAdminProjects(): boolean {
        return this.simplePermissionCheck(Permissions.AdminProjects);
    }

    canAdminUsers(): boolean {
        return this.simplePermissionCheck(Permissions.AdminUsers);
    }

    canAdminEmployees(): boolean {
        return this.simplePermissionCheck(Permissions.AdminViewEmployeeFull);
    }

    canEditSkills(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.EditSkills, employeeId);
    }

    canAdminBusinessAccounts(): boolean {
        return this.simplePermissionCheck(Permissions.EditBusinessAccount);
    }

    canAdminArticles(): boolean {
        return this.simplePermissionCheck(Permissions.EditArticles);
    }

    canAdminSalaryRequests(): boolean{
        return this.simplePermissionCheck(Permissions.AdminSalaryRequests);
    }

    canReportSalaryRequest(): boolean {
        return this.simplePermissionCheck(Permissions.ReportSalaryRequest);
    }

    canUploadTechProfiles(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.UploadTechProfiles, employeeId);
    }

    canDownloadTechProfiles(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.DownloadTechProfiles, employeeId);
    }

    canAdminDictDepartments(): boolean {
        return this.simplePermissionCheck(Permissions.AdminDictDepartments);
    }

    canAdminDictLevels(): boolean {
        return this.simplePermissionCheck(Permissions.AdminDictLevels);
    }

    canAdminDictPositions(): boolean {
        return this.simplePermissionCheck(Permissions.AdminDictPositions);
    }

    canAdminDictOfficeLocations(): boolean {
        return this.simplePermissionCheck(Permissions.AdminDictOfficeLocations);
    }

    canViewEmplCurrentProjectRole(employeeId?: number): boolean {
        if (this.canAdminEmployees()) {
            return true;
        }
        if (employeeId) {
            return this.simplePermissionCheckOrCurrentEmployee(Permissions.ViewEmplCurrentProjectRole, employeeId);
        } else {
            return this.simplePermissionCheck(Permissions.ViewEmplCurrentProjectRole);
        }
    }

    canViewEmplSkills(employeeId: number): boolean {
        return this.canAdminEmployees()
            || this.simplePermissionCheckOrCurrentEmployee(Permissions.ViewEmplSkills, employeeId);
    }

    canAdminManagers(): boolean {
        return this.simplePermissionCheck(Permissions.AdminManagers);
    }

    private simplePermissionCheck(permission: Permissions) {
        const securityInfo: SecurityInfo = store.getters['auth/securityInfo'];
        return securityInfo && securityInfo.authorities && securityInfo.authorities.indexOf(permission) >= 0;
    }

    private simplePermissionCheckOrCurrentEmployee(permission: Permissions, employeeId: number): boolean {
        const securityInfo: SecurityInfo = store.getters['auth/securityInfo'];
        return securityInfo
            && (employeeId == securityInfo.employeeId ||
                (securityInfo.authorities && securityInfo.authorities.indexOf(permission) >= 0));
    }

    /**
     *
     * @param permissions
     * @param employeeId
     * @private
     */
    private simplePermissionsCheckOrCurrentEmployee(permissions: Permissions[], employeeId: number): boolean {
        const securityInfo: SecurityInfo = store.getters['auth/securityInfo'];
        return securityInfo
            && (employeeId == securityInfo.employeeId ||
                permissions.map(p => securityInfo.authorities && securityInfo.authorities.indexOf(p))
                    .reduce((result, perm) => Boolean(result || perm >= 0), Boolean(false)));
    }

}

const permissionService: PermissionService = new VuexPermissionService();

export default permissionService;
