import http from "@/lib/http";
import type { DictItem } from "@/services/dict.service";
import type { ProjectWorkstream } from "@/services/projects.service";

export interface ProjectWorkstreamBody {
  externalId?: string | null;
  displayName: string;
  description?: string | null;
  archived: boolean;
}

export interface CreateOrUpdateProjectBody {
  workstreamRequired: boolean;
  externalId?: string;
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
  workstreamRequired: boolean;
  id: number;
  externalId?: string;
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

export async function listProjectWorkstreams(projectId: number): Promise<ProjectWorkstream[]> {
  return (await http.get<ProjectWorkstream[]>(`v1/admin/projects/${projectId}/workstreams`)).data;
}

export async function createProjectWorkstream(projectId: number, body: ProjectWorkstreamBody): Promise<number> {
  return (await http.post<number>(`v1/admin/projects/${projectId}/workstreams`, body)).data;
}

export async function updateProjectWorkstream(projectId: number, id: number, body: ProjectWorkstreamBody): Promise<number> {
  return (await http.put<number>(`v1/admin/projects/${projectId}/workstreams/${id}`, body)).data;
}

export async function deleteProjectWorkstream(projectId: number, id: number): Promise<number> {
  return (await http.delete<number>(`v1/admin/projects/${projectId}/workstreams/${id}`)).data;
}
