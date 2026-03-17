import http from "@/lib/http";

/**
 * Shared shape for simple admin dictionaries that only support editing
 * records in-place via create/update actions.
 */
export interface AdminSimpleDictItem {
  id: number;
  name: string;
  archived: boolean;
}

export type AdminDepartment = AdminSimpleDictItem;

export interface AdminDepartmentPayload {
  name: string;
  archived: boolean;
}

export interface AdminLevel extends AdminSimpleDictItem {
  weight?: number | null;
}

export interface AdminLevelPayload {
  name: string;
  archived: boolean;
  weight?: number | null;
}

export interface AdminOrganization extends AdminSimpleDictItem {
  description?: string | null;
}

export interface AdminOrganizationPayload {
  name: string;
  archived: boolean;
  description?: string | null;
}

export interface AdminPosition extends AdminSimpleDictItem {
  category?: string | null;
}

export interface AdminPositionPayload {
  name: string;
  archived: boolean;
  category?: string | null;
}

export interface AdminOffice extends AdminSimpleDictItem {
  description?: string | null;
  address?: string | null;
}

export interface AdminOfficePayload {
  name: string;
  archived: boolean;
  description?: string | null;
  address?: string | null;
}

export interface AdminOfficeLocation extends AdminSimpleDictItem {
  description?: string | null;
  office?: {
    id: number;
    name: string;
  } | null;
  mapName?: string | null;
}

export interface AdminOfficeLocationPayload {
  name: string;
  archived: boolean;
  description?: string | null;
  officeId?: number | null;
  mapName?: string | null;
}

export interface AdminOfficeMap {
  mapName: string;
}

export async function fetchAdminDepartments(): Promise<AdminDepartment[]> {
  const response = await http.get<AdminDepartment[]>("v1/admin/dict/departments");
  return response.data;
}

export async function createAdminDepartment(
  payload: AdminDepartmentPayload,
): Promise<AdminDepartment> {
  const response = await http.post<AdminDepartment>(
    "v1/admin/dict/departments",
    payload,
  );
  return response.data;
}

export async function updateAdminDepartment(
  id: number,
  payload: AdminDepartmentPayload,
): Promise<AdminDepartment> {
  const response = await http.put<AdminDepartment>(
    `v1/admin/dict/departments/${id}`,
    payload,
  );
  return response.data;
}

export async function fetchAdminLevels(): Promise<AdminLevel[]> {
  const response = await http.get<AdminLevel[]>("v1/admin/dict/levels");
  return response.data;
}

export async function createAdminLevel(
  payload: AdminLevelPayload,
): Promise<AdminLevel> {
  const response = await http.post<AdminLevel>("v1/admin/dict/levels", payload);
  return response.data;
}

export async function updateAdminLevel(
  id: number,
  payload: AdminLevelPayload,
): Promise<AdminLevel> {
  const response = await http.put<AdminLevel>(
    `v1/admin/dict/levels/${id}`,
    payload,
  );
  return response.data;
}

export async function fetchAdminOrganizations(): Promise<AdminOrganization[]> {
  const response = await http.get<AdminOrganization[]>(
    "v1/admin/dict/organizations",
  );
  return response.data;
}

export async function createAdminOrganization(
  payload: AdminOrganizationPayload,
): Promise<AdminOrganization> {
  const response = await http.post<AdminOrganization>(
    "v1/admin/dict/organizations",
    payload,
  );
  return response.data;
}

export async function updateAdminOrganization(
  id: number,
  payload: AdminOrganizationPayload,
): Promise<AdminOrganization> {
  const response = await http.put<AdminOrganization>(
    `v1/admin/dict/organizations/${id}`,
    payload,
  );
  return response.data;
}

export async function fetchAdminPositions(): Promise<AdminPosition[]> {
  const response = await http.get<AdminPosition[]>("v1/admin/dict/positions");
  return response.data;
}

export async function createAdminPosition(
  payload: AdminPositionPayload,
): Promise<AdminPosition> {
  const response = await http.post<AdminPosition>(
    "v1/admin/dict/positions",
    payload,
  );
  return response.data;
}

export async function updateAdminPosition(
  id: number,
  payload: AdminPositionPayload,
): Promise<AdminPosition> {
  const response = await http.put<AdminPosition>(
    `v1/admin/dict/positions/${id}`,
    payload,
  );
  return response.data;
}

export async function fetchAdminOffices(): Promise<AdminOffice[]> {
  const response = await http.get<AdminOffice[]>("v1/admin/dict/offices");
  return response.data;
}

export async function createAdminOffice(
  payload: AdminOfficePayload,
): Promise<AdminOffice> {
  const response = await http.post<AdminOffice>("v1/admin/dict/offices", payload);
  return response.data;
}

export async function updateAdminOffice(
  id: number,
  payload: AdminOfficePayload,
): Promise<AdminOffice> {
  const response = await http.put<AdminOffice>(
    `v1/admin/dict/offices/${id}`,
    payload,
  );
  return response.data;
}

export async function fetchAdminOfficeLocations(): Promise<AdminOfficeLocation[]> {
  const response = await http.get<AdminOfficeLocation[]>(
    "v1/admin/dict/office_locations",
  );
  return response.data;
}

export async function createAdminOfficeLocation(
  payload: AdminOfficeLocationPayload,
): Promise<AdminOfficeLocation> {
  const response = await http.post<AdminOfficeLocation>(
    "v1/admin/dict/office_locations",
    payload,
  );
  return response.data;
}

export async function updateAdminOfficeLocation(
  id: number,
  payload: AdminOfficeLocationPayload,
): Promise<AdminOfficeLocation> {
  const response = await http.put<AdminOfficeLocation>(
    `v1/admin/dict/office_locations/${id}`,
    payload,
  );
  return response.data;
}

/**
 * Office maps are read from the public dictionary endpoint in legacy too.
 * Admin operations only differ for upload/delete.
 */
export async function fetchAdminOfficeMaps(): Promise<AdminOfficeMap[]> {
  const response = await http.get<AdminOfficeMap[]>("v1/dict/office_maps");
  return response.data;
}

export function getAdminOfficeMapUploadPath(): string {
  return "v1/admin/dict/office_maps";
}

export async function deleteAdminOfficeMap(filename: string): Promise<void> {
  await http.delete(`v1/admin/dict/office_maps/${filename}`);
}
