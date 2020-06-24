import httpService from "../../components/http.service";
import {AxiosInstance} from "axios";

export class LoginRequest {
    constructor(public username: string, public password: string) {
    }
}

export class LoginResponse {
    constructor(public currentUser: CurrentUser) {
    }
}

export class LogoutResponse {

}

export class CurrentUser {
    constructor(
        public username: string,
        public email: string,
        public authorities: string[],
        public employee: EmployeeShortInfo
    ) {
    }
}

export class EmployeeShortInfo {
    constructor(public employeeId: number) {
    }
}


export interface AuthService {
    login(username: string, password: string): Promise<LoginResponse>;

    logout(): Promise<LogoutResponse>;

    currentUser(): Promise<CurrentUser>;
}

class RestAuthService implements AuthService {
    constructor(private httpService: AxiosInstance) {
    }

    public login(username: string, password: string): Promise<LoginResponse> {
        return httpService.post("v1/login", new LoginRequest(username, password)).then(response => response.data);
    }

    public logout(): Promise<LogoutResponse> {
        return httpService.post("v1/logout").then(r => new LogoutResponse());
    }

    public currentUser(): Promise<CurrentUser> {
        return httpService.get("v1/current-user").then(r => r.data);
    }
}

enum Authorities {
    /**
     * Update avatar of any employees
     */
   UpdateAvatar="update_avatar",

    /**
     * Update current project of any employees
     */
   UpdateCurrentProjectGlobal="update_current_project_global"
}

class AuthoritiesChecker {
    public canUpdateCurrentProject(authorities: Array<String>) : boolean{
        return true;
        //FIXME
        //return authorities && authorities.indexOf(Authorities.UpdateCurrentProjectGlobal)>=0;
    }
}

const authService: AuthService = new RestAuthService(httpService);

export default authService;
export const authoritiesChecker = new AuthoritiesChecker();






