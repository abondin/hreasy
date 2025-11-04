import http from "@/lib/http";

export interface Dict {
  id: number;
  name: string;
}

export interface OfficeLocationDict extends Dict {
  mapName?: string | null;
}

export interface CurrentProjectDict extends Dict {
  role: string | null;
}

export interface WithAvatar {
  id: number;
  displayName: string;
  hasAvatar?: boolean;
}

export interface Employee extends WithAvatar {
  currentProject?: CurrentProjectDict | null;
  ba?: Dict | null;
  department?: Dict | null;
  position?: Dict | null;
  officeLocation?: OfficeLocationDict | null;
  officeWorkplace?: string | null;
  email?: string | null;
  telegram?: string | null;
  telegramConfirmedAt?: string | null;
}

export interface UpdateTelegramBody {
  telegram: string | null;
}

export interface EmployeeProjectChange {
  id: number;
  employee: Dict | null;
  project: CurrentProjectDict | null;
  ba: Dict | null;
  changedBy: Dict | null;
  changedAt: string;
}

export async function findEmployee(id: number): Promise<Employee> {
  const response = await http.get<Employee>(`v1/employee/${id}`);
  return response.data;
}

export async function listEmployees(): Promise<Employee[]> {
  const response = await http.get<Employee[]>("v1/employee");
  return response.data;
}

export function getEmployeeAvatarUrl(employeeId: number): string {
  const baseURL = normalizeBaseUrl(http.defaults.baseURL);
  return `${baseURL}v1/fs/avatar/${employeeId}`;
}

export function getEmployeeAvatarUploadUrl(employeeId: number): string {
  const baseURL = normalizeBaseUrl(http.defaults.baseURL);
  return `${baseURL}v1/fs/avatar/${employeeId}/upload`;
}

export async function uploadEmployeeAvatar(
  employeeId: number,
  file: Blob,
): Promise<void> {
  const formData = new FormData();
  formData.append("file", file, "avatar.jpg");
  await http.post<void>(`v1/fs/avatar/${employeeId}/upload`, formData);
}

export async function updateEmployeeTelegram(
  employeeId: number,
  payload: UpdateTelegramBody,
): Promise<number> {
  const response = await http.put<number>(
    `v1/employee/${employeeId}/telegram`,
    payload,
  );
  return response.data;
}

export async function updateEmployeeCurrentProject(
  employeeId: number,
  payload: { id: number; role: string | null } | undefined,
): Promise<number> {
  const response = await http.put<number>(
    `v1/employee/${employeeId}/currentProject`,
    payload,
  );
  return response.data;
}

export async function fetchEmployeeProjectChanges(
  employeeId: number,
): Promise<EmployeeProjectChange[]> {
  const response = await http.get<EmployeeProjectChange[]>(
    `v1/employee/${employeeId}/project_changes`,
  );
  return response.data;
}

function normalizeBaseUrl(baseUrl: string | undefined): string {
  const value = baseUrl ?? "/api/";
  return value.endsWith("/") ? value : `${value}/`;
}
