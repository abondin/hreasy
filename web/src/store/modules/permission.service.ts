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
     * Access to see all projects and admin them
     */
    AdminProjects = "project_admin_area",
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

    canEditAllVacations(): boolean;

    canExportOvertimes(): boolean;

    canAdminOvertimes(): boolean;

    canAdminProjects(): boolean;
}

const namespace: string = 'auth';

class VuexPermissionService implements PermissionService {


    public canUpdateCurrentProject(employeeId: number): boolean {
        return this.simplePermissionsCheckOrCurrentEmployee([Permissions.UpdateCurrentProjectGlobal,
            Permissions.UpdateCurrentProject], employeeId);
    }

    public canUpdateAvatar(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.UpdateAvatar, employeeId);
    }

    canViewAllOvertimes(): boolean {
        return this.simplePermissionCheck(Permissions.ViewOvertimes);
    }

    canViewAllVacations(): boolean {
        return this.simplePermissionCheck(Permissions.ViewVacations);
    }

    canViewOvertimes(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.ViewOvertimes, employeeId);
    }

    canEditAllVacations(): boolean {
        return this.simplePermissionCheck(Permissions.EditVacations);
    }

    canEditOvertimes(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.EditOvertimes, employeeId);
    }

    canApproveOvertimeReport(employeeId: number): boolean {
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
