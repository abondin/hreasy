import http from "@/lib/http";
import type { Skill } from "@/services/skills.service";
import type { Dict } from "@/services/employee.service";

export interface EmployeeWithAllDetails {
  id: number;
  displayName: string;
  birthday?: string;
  sex?: string;
  email?: string;
  phone?: string;
  skype?: string;
  telegram?: string;
  skills?: Skill[];
  active: boolean;
  currentProjectId?: number;
  currentProjectRole: string | null;
  baId?: number;
  departmentId?: number;
  organizationId?: number | null;
  dateOfEmployment?: string;
  levelId?: number;
  workType?: string;
  workDay?: string;
  registrationAddress?: string;
  documentSeries?: string;
  documentNumber?: string;
  documentIssuedBy?: string;
  documentIssuedDate?: string;
  foreignPassport?: string;
  cityOfResidence?: string;
  englishLevel?: string;
  familyStatus?: string;
  spouseName?: string;
  children?: string;
  dateOfDismissal?: string;
  documentFull?: string;
  positionId?: number;
  officeLocationId?: number;
  officeWorkplace?: string | null;
  hasAvatar: boolean;
}

export interface CreateOrUpdateEmployeeBody {
  currentProjectId?: number | null;
  currentProjectRole: string | null;
  displayName: string;
  departmentId?: number | null;
  organizationId?: number | null;
  birthday?: string | null;
  sex?: string | null;
  email?: string | null;
  phone?: string | null;
  skype?: string | null;
  telegram?: string | null;
  dateOfEmployment?: string | null;
  levelId?: number | null;
  workType?: string | null;
  workDay?: string | null;
  registrationAddress?: string | null;
  documentSeries?: string | null;
  documentNumber?: string | null;
  documentIssuedBy?: string | null;
  documentIssuedDate?: string | null;
  foreignPassport?: string | null;
  cityOfResidence?: string | null;
  englishLevel?: string | null;
  familyStatus?: string | null;
  spouseName?: string | null;
  children?: string | null;
  dateOfDismissal?: string | null;
  positionId?: number | null;
  officeLocationId?: number | null;
  officeWorkplaceId?: number | null;
  officeWorkplace?: string | null;
}

export interface EmployeeKid {
  id: number;
  displayName: string;
  birthday?: string;
  age?: number;
  parent: Dict & { active?: boolean };
}

export interface CreateOrUpdateEmployeeKidBody {
  displayName: string;
  birthday?: string;
}

export async function listAdminEmployees(
  includeFired = true,
): Promise<EmployeeWithAllDetails[]> {
  const response = await http.get<EmployeeWithAllDetails[]>("v1/admin/employees", {
    params: { includeFired },
  });
  return response.data;
}

export async function createAdminEmployee(
  payload: CreateOrUpdateEmployeeBody,
): Promise<number> {
  const response = await http.post<number>("v1/admin/employees", payload);
  return response.data;
}

export async function updateAdminEmployee(
  employeeId: number,
  payload: CreateOrUpdateEmployeeBody,
): Promise<number> {
  const response = await http.put<number>(`v1/admin/employees/${employeeId}`, payload);
  return response.data;
}

export async function exportAdminEmployees(includeFired: boolean): Promise<Blob> {
  const response = await http.get<ArrayBuffer>("v1/admin/employees/export", {
    params: { includeFired },
    responseType: "arraybuffer",
  });

  return new Blob([response.data], {
    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  });
}

export async function listEmployeeKids(): Promise<EmployeeKid[]> {
  const response = await http.get<EmployeeKid[]>("v1/admin/employees/kids");
  return response.data;
}

export async function createEmployeeKid(
  employeeId: number,
  payload: CreateOrUpdateEmployeeKidBody,
): Promise<number> {
  const response = await http.post<number>(`v1/admin/employees/${employeeId}/kids`, payload);
  return response.data;
}

export async function updateEmployeeKid(
  employeeId: number,
  kidId: number,
  payload: CreateOrUpdateEmployeeKidBody,
): Promise<number> {
  const response = await http.put<number>(
    `v1/admin/employees/${employeeId}/kids/${kidId}`,
    payload,
  );
  return response.data;
}
