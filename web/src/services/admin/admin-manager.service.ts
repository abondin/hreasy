import http from "@/lib/http";
import type { SimpleDict } from "@/services/projects.service";

export const managerResponsibilityTypes = ["technical", "organization", "hr"] as const;
export const managerResponsibilityObjectTypes = ["project", "business_account", "department"] as const;

export type ManagerResponsibilityType = typeof managerResponsibilityTypes[number];
export type ManagerResponsibilityObjectType = typeof managerResponsibilityObjectTypes[number];

export interface ManagerResponsibilityObjectId {
  id: number;
  type: ManagerResponsibilityObjectType;
}

export interface ManagerResponsibilityObject extends ManagerResponsibilityObjectId {
  name: string;
}

export interface Manager {
  id: number;
  employee: SimpleDict;
  responsibilityObject: ManagerResponsibilityObject;
  responsibilityType: ManagerResponsibilityType;
  comment?: string;
  createdAt?: string;
  createdBy?: number;
}

export interface CreateManagerBody {
  employee: number | null;
  responsibilityObjectType: ManagerResponsibilityObjectType;
  responsibilityObjectId: number | null;
  responsibilityType?: ManagerResponsibilityType | null;
  comment?: string;
}

export interface UpdateManagerBody {
  responsibilityType: ManagerResponsibilityType;
  comment?: string;
}

/** Admin CRUD contract for manager links across projects, business accounts, and departments. */
export async function listManagers(): Promise<Manager[]> {
  const response = await http.get<Manager[]>("v1/admin/managers");
  return response.data;
}

/** Admin CRUD contract for manager links across projects, business accounts, and departments. */
export async function listManagersByObject(
  selectedObject: ManagerResponsibilityObjectId,
): Promise<Manager[]> {
  const response = await http.get<Manager[]>(
    `v1/admin/managers/object/${selectedObject.type}/${selectedObject.id}`,
  );
  return response.data;
}

export async function createManager(body: CreateManagerBody): Promise<number> {
  const response = await http.post<number>("v1/admin/managers", body);
  return response.data;
}

export async function updateManager(managerId: number, body: UpdateManagerBody): Promise<number> {
  const response = await http.put<number>(`v1/admin/managers/${managerId}`, body);
  return response.data;
}

export async function deleteManager(managerId: number): Promise<void> {
  await http.delete(`v1/admin/managers/${managerId}`);
}
