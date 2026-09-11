import http from "@/lib/http";
import type { ProjectWorkstream } from "@/services/projects.service";

export type ResourceAllocationDisplayUnit = "percent" | "personMonths";

/** Downloads all annual allocations; UI filters are intentionally not included. */
export async function exportResourceAllocationAnalytics(
  year: number,
  unit: ResourceAllocationDisplayUnit,
): Promise<void> {
  const response = await http.get<ArrayBuffer>(`v1/resource-allocations/analytics/${year}/export`, {
    params: { unit },
    responseType: "arraybuffer",
  });
  const blob = new Blob([response.data], {
    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `ResourceAllocations-${year}-${unit}.xlsx`;
  try {
    link.click();
  } finally {
    window.URL.revokeObjectURL(url);
  }
}
export interface ResourceAllocationEmployee {
  id: number;
  displayName: string;
  email?: string | null;
  departmentId: number | null;
  departmentName: string | null;
  currentProjectId: number | null;
  currentProjectName: string | null;
  currentProjectRole?: string | null;
}

export interface ResourceAllocationProject {
  id: number;
  name: string;
  departmentId: number | null;
  departmentName: string | null;
  baId: number | null;
  baName: string | null;
  startDate?: string | null;
  endDate?: string | null;
  active: boolean;
  editable: boolean;
}

export interface ResourceAllocationChange {
  period: number;
  employeeId: number;
  percent: number | null;
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
  sameProject: boolean;
}

export interface ResourceAllocationInputMonth {
  period: number;
  closed: boolean;
}

export interface ResourceAllocationInputEmployee {
  id: number;
  displayName: string;
  email?: string | null;
  currentProjectId: number | null;
  currentProjectName: string | null;
  currentProjectRole?: string | null;
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

export interface ResourceAllocationCommentCell {
  period: number;
  commentCount: number;
}

export interface ResourceAllocationCommentRow {
  employee: ResourceAllocationEmployee;
  project: Pick<ResourceAllocationProject,
    "id" | "name" | "departmentId" | "departmentName" | "baId" | "baName">;
  workstream: Pick<ProjectWorkstream, "id" | "displayName"> | null;
  cells: ResourceAllocationCommentCell[];
}

export interface ResourceAllocationCommentSummary {
  year: number;
  rows: ResourceAllocationCommentRow[];
}

export interface ResourceAllocationCommentCellKey {
  period: number;
  employeeId: number;
  projectId: number;
  workstreamId: number | null;
}

export interface ResourceAllocationComment {
  id: number;
  text: string;
  author: { id: number; displayName: string };
  createdAt: string;
  updatedAt: string | null;
  mine: boolean;
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

export async function fetchResourceAllocationCommentSummary(
  year: number,
): Promise<ResourceAllocationCommentSummary> {
  const response = await http.get<ResourceAllocationCommentSummary>(
    `v1/resource-allocations/comments/summary/${year}`,
  );
  return response.data;
}

export async function fetchResourceAllocationComments(
  cell: ResourceAllocationCommentCellKey,
): Promise<ResourceAllocationComment[]> {
  const response = await http.get<ResourceAllocationComment[]>("v1/resource-allocations/comments", {
    params: cellParams(cell),
  });
  return response.data;
}

export async function createResourceAllocationComment(
  cell: ResourceAllocationCommentCellKey,
  text: string,
): Promise<ResourceAllocationComment> {
  const response = await http.post<ResourceAllocationComment>("v1/resource-allocations/comments", {
    ...cell,
    text,
  });
  return response.data;
}

export async function updateResourceAllocationComment(
  commentId: number,
  text: string,
): Promise<ResourceAllocationComment> {
  const response = await http.put<ResourceAllocationComment>(
    `v1/resource-allocations/comments/${commentId}`,
    { text },
  );
  return response.data;
}

export async function deleteResourceAllocationComment(commentId: number): Promise<void> {
  await http.delete(`v1/resource-allocations/comments/${commentId}`);
}

function cellParams(cell: ResourceAllocationCommentCellKey) {
  return {
    period: cell.period,
    employeeId: cell.employeeId,
    projectId: cell.projectId,
    ...(cell.workstreamId == null ? {} : { workstreamId: cell.workstreamId }),
  };
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

export async function saveClosedResourceAllocationPeriods(
  year: number,
  closedPeriods: number[],
): Promise<number[]> {
  const response = await http.put<number[]>(`v1/resource-allocations/closed-periods/${year}`, {
    closedPeriods,
  });
  return response.data;
}

export async function fetchClosedResourceAllocationPeriods(year: number): Promise<number[]> {
  const response = await http.get<number[]>(`v1/resource-allocations/closed-periods/${year}`);
  return response.data;
}
