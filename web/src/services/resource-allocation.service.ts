import http from "@/lib/http";
import type { ProjectWorkstream } from "@/services/projects.service";

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
  selectedWorkstreamId?: number | null;
  months: ResourceAllocationInputMonth[];
  employees: ResourceAllocationInputEmployee[];
  projects: ResourceAllocationProject[];
  workstreams?: ProjectWorkstream[];
  allocations: ResourceAllocationProjectInputValue[];
  otherAllocations: ResourceAllocationOtherValue[];
}

export interface ResourceAllocationAnalyticsValue {
  period: number;
  employeeId: number;
  projectId: number;
  workstreamId?: number | null;
  percent: number;
}

export interface ResourceAllocationAnalytics {
  year: number;
  employees: ResourceAllocationEmployee[];
  projects: ResourceAllocationProject[];
  workstreams?: ProjectWorkstream[];
  allocations: ResourceAllocationAnalyticsValue[];
}

export async function fetchResourceAllocationProjectInput(
  year: number,
  projectId?: number,
  workstreamId?: number,
): Promise<ResourceAllocationProjectInput> {
  const response = await http.get<ResourceAllocationProjectInput>(`v1/resource-allocations/input/${year}`, {
    params: {
      ...(projectId == null ? {} : { projectId }),
      ...(workstreamId == null ? {} : { workstreamId }),
    },
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
  workstreamId: number | null,
  changes: ResourceAllocationChange[],
): Promise<void> {
  await http.put(`v1/resource-allocations/input/${year}/${projectId}`, { changes }, {
    params: workstreamId == null ? undefined : { workstreamId },
  });
}

export async function closeResourceAllocationPeriod(period: number): Promise<void> {
  await http.put(`v1/resource-allocations/closed-periods/${period}`, {});
}

export async function reopenResourceAllocationPeriod(period: number): Promise<void> {
  await http.delete(`v1/resource-allocations/closed-periods/${period}`);
}

export async function fetchClosedResourceAllocationPeriods(year: number): Promise<number[]> {
  const response = await http.get<number[]>(`v1/resource-allocations/closed-periods/${year}`);
  return response.data;
}
