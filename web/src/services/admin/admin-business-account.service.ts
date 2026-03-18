import http from "@/lib/http";
import type { ManagerOfObject } from "@/services/projects.service";

export interface CreateOrUpdateBusinessAccountBody {
  name: string;
  description?: string;
  archived: boolean;
}

export interface BusinessAccountInfo {
  id: number;
  name: string;
  description?: string;
  managers: ManagerOfObject[];
  createdBy?: number;
  createdAt?: string;
  archived: boolean;
}

export interface BusinessAccountPosition {
  id: number;
  name: string;
  description?: string;
  archived: boolean;
  rate: number;
}

export interface CreateOrUpdateBusinessAccountPositionBody {
  name: string;
  description?: string;
  rate: number;
  archived: boolean;
}

/** Admin CRUD contract for business accounts and their positions. */
export async function listAdminBusinessAccounts(): Promise<BusinessAccountInfo[]> {
  const response = await http.get<BusinessAccountInfo[]>("v1/admin/business_account?includeArchived=true");
  return response.data;
}

export async function fetchAdminBusinessAccount(baId: number): Promise<BusinessAccountInfo> {
  const response = await http.get<BusinessAccountInfo>(`v1/admin/business_account/${baId}`);
  return response.data;
}

export async function createAdminBusinessAccount(
  body: CreateOrUpdateBusinessAccountBody,
): Promise<number> {
  const response = await http.post<number>("v1/admin/business_account", body);
  return response.data;
}

export async function updateAdminBusinessAccount(
  baId: number,
  body: CreateOrUpdateBusinessAccountBody,
): Promise<number> {
  const response = await http.put<number>(`v1/admin/business_account/${baId}`, body);
  return response.data;
}

export async function listBusinessAccountPositions(
  baId: number,
): Promise<BusinessAccountPosition[]> {
  const response = await http.get<BusinessAccountPosition[]>(`v1/admin/business_account/${baId}/positions`);
  return response.data;
}

export async function createBusinessAccountPosition(
  baId: number,
  body: CreateOrUpdateBusinessAccountPositionBody,
): Promise<number> {
  const response = await http.post<number>(`v1/admin/business_account/${baId}/positions`, body);
  return response.data;
}

export async function updateBusinessAccountPosition(
  baId: number,
  positionId: number,
  body: CreateOrUpdateBusinessAccountPositionBody,
): Promise<number> {
  const response = await http.put<number>(`v1/admin/business_account/${baId}/positions/${positionId}`, body);
  return response.data;
}
