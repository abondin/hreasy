import http from "@/lib/http";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface EmployeeShortInfo {
  employeeId: number;
  accessibleBas: number[];
}

export interface CurrentUser {
  username: string;
  email: string;
  authorities: string[];
  employee: EmployeeShortInfo;
}

export interface LoginResponse {
  currentUser: CurrentUser;
}

export type LogoutResponse = Record<string, never>;

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await http.post<LoginResponse>("v1/login", request);
  return response.data;
}

export async function logout(): Promise<LogoutResponse> {
  await http.post<LogoutResponse>("v1/logout");
  return {};
}

export async function currentUser(): Promise<CurrentUser> {
  const response = await http.get<CurrentUser>("v1/current-user");
  return response.data;
}
