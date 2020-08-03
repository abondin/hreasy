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
     * View overtimes of all employees
     */
    ViewOvertimes = "overtime_view",

    /**
     * Edit overtimes of any employee
     */
    EditOvertimes = "overtime_edit",

    /**
     * View vacations of all employees
     */
    ViewVacations = "vacation_view",

    /**
     * Edit vacations of any employee
     */
    EditVacations = "vacation_edit",
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

    canViewAllOvertimes(): boolean;

    canViewAllVacations(): boolean;
}

const namespace: string = 'auth';

class VuexPermissionService implements PermissionService {


    public canUpdateCurrentProject(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.UpdateCurrentProjectGlobal, employeeId);
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

    canEditOvertimes(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.EditOvertimes, employeeId);
    }

    private simplePermissionCheck(permission: Permissions) {
        const securityInfo: SecurityInfo = store.getters['auth/securityInfo'];
        return securityInfo && securityInfo.authorities && securityInfo.authorities.indexOf(permission) >= 0;
    }

    private simplePermissionCheckOrCurrentEmployee(permission: Permissions, employeeId: number) {
        const securityInfo: SecurityInfo = store.getters['auth/securityInfo'];
        return securityInfo
            && (employeeId == securityInfo.employeeId ||
                (securityInfo.authorities && securityInfo.authorities.indexOf(permission) >= 0));
    }
}

const permissionService: PermissionService = new VuexPermissionService();

export default permissionService;
