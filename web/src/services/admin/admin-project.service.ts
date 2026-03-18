import http from "@/lib/http";
import type { DictItem } from "@/services/dict.service";

export interface CreateOrUpdateProjectBody {
  name: string;
  startDate?: string;
  endDate?: string;
  planStartDate?: string;
  planEndDate?: string;
  customer?: string;
  departmentId?: number;
  baId: number | null;
  info?: string;
}

export interface AdminProjectInfo {
  id: number;
  name: string;
  startDate?: string;
  endDate?: string;
  planStartDate?: string;
  planEndDate?: string;
  customer?: string;
  department?: DictItem;
  businessAccount?: DictItem;
  createdBy?: number;
  createdAt?: string;
  info?: string;
  active: boolean;
}

/** Admin CRUD contract for project management pages. */
export async function listAdminProjects(): Promise<AdminProjectInfo[]> {
  const response = await http.get<AdminProjectInfo[]>("v1/admin/projects");
  return response.data;
}

export async function fetchAdminProject(projectId: number): Promise<AdminProjectInfo> {
  const response = await http.get<AdminProjectInfo>(`v1/admin/projects/${projectId}`);
  return response.data;
}

export async function createAdminProject(body: CreateOrUpdateProjectBody): Promise<number> {
  const response = await http.post<number>("v1/admin/projects", body);
  return response.data;
}

export async function updateAdminProject(
  projectId: number,
  body: CreateOrUpdateProjectBody,
): Promise<number> {
  const response = await http.put<number>(`v1/admin/projects/${projectId}`, body);
  return response.data;
}
