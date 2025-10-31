import http from '@/lib/http';

export interface Dict {
  id: number;
  name: string;
}

export interface CurrentProjectDict extends Dict {
  role: string | null;
}

export interface Employee {
  id: number;
  displayName: string;
  currentProject?: CurrentProjectDict | null;
  ba?: Dict | null;
  department?: Dict | null;
  position?: Dict | null;
  officeLocation?: Dict | null;
  officeWorkplace?: string | null;
  email?: string | null;
  telegram?: string | null;
  telegramConfirmedAt?: string | null;
  hasAvatar?: boolean;
  hasOfficeLocationMap?: boolean;
}

export interface UpdateTelegramBody {
  telegram: string | null;
}

export async function findEmployee(id: number): Promise<Employee> {
  const response = await http.get<Employee>(`v1/employee/${id}`);
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

export async function updateEmployeeTelegram(
  employeeId: number,
  payload: UpdateTelegramBody
): Promise<number> {
  const response = await http.put<number>(`v1/employee/${employeeId}/telegram`, payload);
  return response.data;
}

function normalizeBaseUrl(baseUrl: string | undefined): string {
  const value = baseUrl ?? '/api/';
  return value.endsWith('/') ? value : `${value}/`;
}
