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
    UpdateCurrentProjectGlobal = "update_current_project_global"
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
}

const namespace: string = 'auth';

class VuexPermissionService implements PermissionService {


    public canUpdateCurrentProject(employeeId: number): boolean {
        const securityInfo: SecurityInfo = store.getters['auth/securityInfo'];
        return securityInfo
            && (securityInfo.authorities
                && securityInfo.authorities.indexOf(Permissions.UpdateCurrentProjectGlobal) >= 0);
    }

    public canUpdateAvatar(employeeId: number): boolean {
        const securityInfo: SecurityInfo = store.getters['auth/securityInfo'];
        return securityInfo
            && (employeeId == securityInfo.employeeId ||
                (securityInfo.authorities && securityInfo.authorities.indexOf(Permissions.UpdateAvatar) >= 0));
    }
}

const permissionService: PermissionService = new VuexPermissionService();

export default permissionService;
