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
  employeeId: number;
  projectId: number;
  percent: number;
  expectedRevisionId: number | null;
}

export async function fetchResourceAllocations(period: number): Promise<ResourceAllocationSheet> {
  const response = await http.get<ResourceAllocationSheet>(`v1/resource-allocations/${period}`);
  return response.data;
}

export async function saveResourceAllocations(
  period: number,
  changes: ResourceAllocationChange[],
): Promise<void> {
  await http.put(`v1/resource-allocations/${period}`, { changes });
}
