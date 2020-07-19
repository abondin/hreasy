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
    canUpdateAvatar(employeeId: number): boolean

    canViewOvertimes(): boolean;

    canViewVacations(): boolean;
}

const namespace: string = 'auth';

class VuexPermissionService implements PermissionService {


    public canUpdateCurrentProject(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.UpdateCurrentProjectGlobal, employeeId);
    }

    public canUpdateAvatar(employeeId: number): boolean {
        return this.simplePermissionCheckOrCurrentEmployee(Permissions.UpdateAvatar, employeeId);
    }

    canViewOvertimes(): boolean {
        return this.simplePermissionCheck(Permissions.ViewOvertimes);
    }

    canViewVacations(): boolean {
        return this.simplePermissionCheck(Permissions.ViewVacations);
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
