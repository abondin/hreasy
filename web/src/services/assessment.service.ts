import http from "@/lib/http";
import type { CurrentProjectDict, Dict } from "@/services/employee.service";

export interface AssessmentEmployeeSummary {
  employeeId: number;
  displayName: string;
  lastAssessmentId: number | null;
  lastAssessmentDate: string | null;
  lastAssessmentCompletedDate: string | null;
  employeeDateOfEmployment: string | null;
  latestActivity: string | null;
  currentProject: CurrentProjectDict | null;
  ba: Dict | null;
  daysWithoutAssessment: number | null;
}

/**
 * Lightweight assessment payload reused by salary request forms and assessment tables.
 */
export interface AssessmentBase {
  id: number;
  plannedDate: string | null;
  createdAt: string | null;
  employee: Dict | null;
  createdBy: Dict | null;
  completedAt: string | null;
  completedBy: Dict | null;
  canceledAt: string | null;
  canceledBy: Dict | null;
}

export interface NewAssessmentBody {
  plannedDate: string;
  managers: number[];
}

export interface AssessmentForm {
  id: number;
  owner: number;
  formType: number;
  content: string | null;
  completedAt: string | null;
  completedBy: number | null;
}

export interface AssessmentWithFormsAndFiles extends AssessmentBase {
  forms: AssessmentForm[];
  attachmentsFilenames: string[];
  attachmentsAccessToken: string;
}

export interface DeleteAssessmentAttachmentResponse {
  deleted: boolean;
}

export async function fetchAssessmentsSummary(): Promise<AssessmentEmployeeSummary[]> {
  const response = await http.get<AssessmentEmployeeSummary[]>("v1/assessment");
  return response.data;
}

export async function fetchAssessmentDetails(
  employeeId: number,
  assessmentId: number,
): Promise<AssessmentWithFormsAndFiles> {
  const response = await http.get<AssessmentWithFormsAndFiles>(
    `v1/assessment/${employeeId}/${assessmentId}`,
  );
  return response.data;
}

export async function fetchEmployeeAssessments(
  employeeId: number,
): Promise<AssessmentBase[]> {
  const response = await http.get<AssessmentBase[]>(`v1/assessment/${employeeId}`);
  return response.data;
}

export async function scheduleAssessment(
  employeeId: number,
  body: NewAssessmentBody,
): Promise<number> {
  const response = await http.post<number>(`v1/assessment/${employeeId}`, body);
  return response.data;
}

export async function cancelAssessment(
  employeeId: number,
  assessmentId: number,
): Promise<void> {
  await http.delete(`v1/assessment/${employeeId}/${assessmentId}`);
}

export async function completeAssessment(
  employeeId: number,
  assessmentId: number,
): Promise<void> {
  await http.post(`v1/assessment/${employeeId}/${assessmentId}/complete`);
}

export function getAssessmentAttachmentUploadUrl(
  employeeId: number,
  assessmentId: number,
): string {
  const baseUrl = normalizeBaseUrl(http.defaults.baseURL);
  return `${baseUrl}v1/assessment/${employeeId}/${assessmentId}/attachment`;
}

export function getAssessmentAttachmentPath(
  employeeId: number,
  assessmentId: number,
  attachmentFilename: string,
  accessToken: string,
): string {
  const baseUrl = normalizeBaseUrl(http.defaults.baseURL);
  return `${baseUrl}v1/fs/assessment/${employeeId}/${assessmentId}/${attachmentFilename}/${accessToken}`;
}

export async function deleteAssessmentAttachment(
  employeeId: number,
  assessmentId: number,
  attachmentFilename: string,
): Promise<DeleteAssessmentAttachmentResponse> {
  const response = await http.delete<DeleteAssessmentAttachmentResponse>(
    `v1/assessment/${employeeId}/${assessmentId}/attachment/${attachmentFilename}`,
  );
  return response.data;
}

export async function exportAssessments(): Promise<void> {
  const response = await http.get<ArrayBuffer>("v1/assessment/export", {
    responseType: "arraybuffer",
  });

  const blob = new Blob(
    [response.data],
    { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
  );
  const link = document.createElement("a");
  link.href = window.URL.createObjectURL(blob);
  link.download = "AssessmentsSummary.xlsx";
  link.click();
}

function normalizeBaseUrl(baseUrl: string | undefined): string {
  const value = baseUrl ?? "/api/";
  return value.endsWith("/") ? value : `${value}/`;
}
