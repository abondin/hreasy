import http from "@/lib/http";
import type { CurrentProjectDict } from "@/services/employee.service";

export interface EmployeeVacationShort {
  id: number;
  year: number;
  startDate: string;
  endDate: string;
  current: boolean;
}

export interface VacPlanningPeriod {
  year: number;
}

export const vacationStatuses = [
  "PLANNED",
  "TAKEN",
  "COMPENSATION",
  "CANCELED",
  "REJECTED",
  "REQUESTED",
] as const;
export type VacationStatus = (typeof vacationStatuses)[number];

export interface Vacation {
  id: number;
  employee: number;
  employeeDisplayName: string;
  employeeCurrentProject?: CurrentProjectDict | null;
  year: number;
  startDate: string;
  endDate: string;
  notes: string;
  canceled: boolean;
  plannedStartDate?: string;
  plannedEndDate?: string;
  status: VacationStatus;
  documents: string;
  daysNumber: number;
}

export interface MyVacation {
  id: number;
  year: number;
  startDate: string;
  endDate: string;
  notes: string;
  plannedStartDate?: string;
  plannedEndDate?: string;
  status: VacationStatus;
  documents: string;
  daysNumber: number;
}

export interface RequestOrUpdateMyVacation {
  year: number;
  startDate: string;
  endDate: string;
  notes: string;
  daysNumber: number;
}

export interface CreateOrUpdateVacation {
  year: number;
  startDate: string;
  endDate: string;
  plannedStartDate: string;
  plannedEndDate: string;
  status: VacationStatus;
  notes: string;
  documents: string;
  daysNumber: number;
}

export async function fetchVacations(years: number[]): Promise<Vacation[]> {
  const response = await http.get<Vacation[]>("v1/vacations", {
    params: { years },
  });
  return response.data;
}

export async function fetchMyVacations(): Promise<MyVacation[]> {
  const response = await http.get<MyVacation[]>("v1/vacations/my");
  return response.data;
}

export async function fetchCurrentOrFutureVacations(
  employeeId: number,
): Promise<EmployeeVacationShort[]> {
  const response = await http.get<EmployeeVacationShort[]>(
    `v1/vacations/${employeeId}/currentOrFuture`,
  );
  return response.data;
}

export async function createVacation(
  employeeId: number,
  body: CreateOrUpdateVacation,
): Promise<number> {
  const response = await http.post<number>(`v1/vacations/${employeeId}`, body);
  return response.data;
}

export async function updateVacation(
  employeeId: number,
  vacationId: number,
  body: CreateOrUpdateVacation,
): Promise<number> {
  const response = await http.put<number>(
    `v1/vacations/${employeeId}/${vacationId}`,
    body,
  );
  return response.data;
}

export async function exportVacations(selectedYears: number[]): Promise<void> {
  const response = await http.get<ArrayBuffer>("v1/vacations/export", {
    params: { years: selectedYears ? selectedYears.join(",") : undefined },
    responseType: "arraybuffer",
  });

  const blob = new Blob([response.data], {
    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  });
  const link = document.createElement("a");
  link.href = window.URL.createObjectURL(blob);
  link.download = "Vacations.xlsx";
  link.click();
}

export async function openPlanningPeriods(): Promise<VacPlanningPeriod[]> {
  const response = await http.get<VacPlanningPeriod[]>(
    "v1/vacations/planning-period",
  );
  return response.data;
}

export async function requestVacation(
  body: RequestOrUpdateMyVacation,
): Promise<number> {
  const response = await http.post<number>("v1/vacations/request", body);
  return response.data;
}

export async function rejectVacationRequest(vacationId: number): Promise<number> {
  const response = await http.delete<number>(`v1/vacations/my/${vacationId}`);
  return response.data;
}

export async function updatePlanningVacation(
  vacationId: number,
  body: RequestOrUpdateMyVacation,
): Promise<number> {
  const response = await http.put<number>(
    `v1/vacations/request/${vacationId}`,
    body,
  );
  return response.data;
}

export function isNotWorkingDays(vacation: Vacation): boolean {
  return vacation.status === "PLANNED" || vacation.status === "TAKEN";
}
