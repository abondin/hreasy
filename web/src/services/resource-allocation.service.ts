import http from "@/lib/http";

export interface ResourceAllocationEmployee {
  id: number;
  displayName: string;
  departmentId: number | null;
  departmentName: string | null;
  currentProjectId: number | null;
  currentProjectName: string | null;
}

export interface ResourceAllocationProject {
  id: number;
  name: string;
  departmentId: number | null;
  departmentName: string | null;
  baId: number | null;
  baName: string | null;
  active: boolean;
  editable: boolean;
  managed: boolean;
}

export interface ResourceAllocationValue {
  employeeId: number;
  projectId: number;
  percent: number;
  revisionId: number;
}

export interface ResourceAllocationSheet {
  period: number;
  employees: ResourceAllocationEmployee[];
  projects: ResourceAllocationProject[];
  allocations: ResourceAllocationValue[];
  previousAllocations: ResourceAllocationValue[];
}

export interface ResourceAllocationChange {
  period: number;
  employeeId: number;
  percent: number;
  expectedRevisionId: number | null;
}

export interface ResourceAllocationProjectInputValue {
  period: number;
  employeeId: number;
  percent: number;
  revisionId: number;
}

export interface ResourceAllocationOtherValue {
  period: number;
  employeeId: number;
  percent: number;
}

export interface ResourceAllocationInputMonth {
  period: number;
  closed: boolean;
}

export interface ResourceAllocationInputEmployee {
  id: number;
  displayName: string;
  currentProjectId: number | null;
  currentProjectName: string | null;
  dateOfEmployment: string | null;
  dateOfDismissal: string | null;
  dismissed: boolean;
}

export interface ResourceAllocationProjectInput {
  year: number;
  selectedProjectId: number | null;
  months: ResourceAllocationInputMonth[];
  employees: ResourceAllocationInputEmployee[];
  projects: ResourceAllocationProject[];
  allocations: ResourceAllocationProjectInputValue[];
  otherAllocations: ResourceAllocationOtherValue[];
  canManagePeriods: boolean;
}

export interface ResourceAllocationAnalyticsValue {
  period: number;
  employeeId: number;
  projectId: number;
  percent: number;
}

export interface ResourceAllocationAnalytics {
  year: number;
  employees: ResourceAllocationEmployee[];
  projects: ResourceAllocationProject[];
  allocations: ResourceAllocationAnalyticsValue[];
}

export async function fetchResourceAllocations(period: number): Promise<ResourceAllocationSheet> {
  const response = await http.get<ResourceAllocationSheet>(`v1/resource-allocations/${period}`);
  return response.data;
}

export async function fetchResourceAllocationProjectInput(
  year: number,
  projectId?: number,
): Promise<ResourceAllocationProjectInput> {
  const response = await http.get<ResourceAllocationProjectInput>(`v1/resource-allocations/input/${year}`, {
    params: projectId == null ? undefined : { projectId },
  });
  return response.data;
}

export async function fetchResourceAllocationAnalytics(
  year: number,
): Promise<ResourceAllocationAnalytics> {
  const response = await http.get<ResourceAllocationAnalytics>(
    `v1/resource-allocations/analytics/${year}`,
  );
  return response.data;
}

export async function saveResourceAllocations(
  year: number,
  projectId: number,
  changes: ResourceAllocationChange[],
): Promise<void> {
  await http.put(`v1/resource-allocations/input/${year}/${projectId}`, { changes });
}

export async function closeResourceAllocationPeriod(period: number): Promise<void> {
  await http.put(`v1/resource-allocations/closed-periods/${period}`, {});
}

export async function reopenResourceAllocationPeriod(period: number): Promise<void> {
  await http.delete(`v1/resource-allocations/closed-periods/${period}`);
}
