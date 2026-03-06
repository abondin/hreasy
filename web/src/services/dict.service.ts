import http from "@/lib/http";
import type { Dict } from "@/services/employee.service";

export interface DictItem extends Dict {
  active?: boolean;
}

export async function fetchDaysNotIncludedInVacations(
  years: number[],
): Promise<string[]> {
  if (!years.length) {
    return [];
  }
  const response = await http.get<string[]>(
    `v1/dict/calendar/days_not_included_in_vacations/${years.join(",")}`,
  );
  return response.data;
}

export async function fetchDepartments(): Promise<DictItem[]> {
  const response = await http.get<DictItem[]>("v1/dict/departments");
  return response.data;
}

export async function fetchOrganizations(): Promise<DictItem[]> {
  const response = await http.get<DictItem[]>("v1/dict/organizations");
  return response.data;
}

export async function fetchPositions(): Promise<DictItem[]> {
  const response = await http.get<DictItem[]>("v1/dict/positions");
  return response.data;
}

export async function fetchLevels(): Promise<DictItem[]> {
  const response = await http.get<DictItem[]>("v1/dict/levels");
  return response.data;
}

export async function fetchOfficeLocations(): Promise<DictItem[]> {
  const response = await http.get<DictItem[]>("v1/dict/office_locations");
  return response.data;
}
