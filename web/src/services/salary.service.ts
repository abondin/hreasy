import http from "@/lib/http";
import type { Dict, CurrentProjectDict } from "@/services/employee.service";

export const salaryRequestTypes = [1, 2] as const;
export type SalaryRequestType = (typeof salaryRequestTypes)[number];

export const salaryApprovalStates = [1, 2, 3] as const;
export type SalaryApprovalState = (typeof salaryApprovalStates)[number];

export const salaryRequestLinkTypes = [1, 2] as const;
export type SalaryRequestLinkType = (typeof salaryRequestLinkTypes)[number];

export const salaryRequestImplementationStates = [1, 2] as const;
export type SalaryRequestImplementationState = (typeof salaryRequestImplementationStates)[number];

/**
 * Main DTO for salary requests list/details. Keep this contract aligned with backend responses.
 */
export interface SalaryIncreaseRequest {
  id: number;
  employee: Dict;
  type: SalaryRequestType;
  budgetBusinessAccount: Dict | null;
  budgetExpectedFundingUntil: string | null;
  createdAt: string;
  createdBy: Dict;
  assessment: Dict | null;
  employeeInfo: {
    currentProject: CurrentProjectDict | null;
    dateOfEmployment: string | null;
    ba: Dict | null;
    position: Dict | null;
    currentSalaryAmount: number | null;
    previousSalaryIncreaseText: string | null;
    previousSalaryIncreaseDate: string | null;
  };
  req: {
    increaseAmount: number | null;
    plannedSalaryAmount: number | null;
    increaseStartPeriod: number;
    newPosition: Dict | null;
    reason: string;
    comment: string | null;
  };
  impl?: {
    implementedAt: string | null;
    implementedBy: Dict | null;
    state: SalaryRequestImplementationState;
    newPosition: Dict | null;
    increaseAmount: number | null;
    salaryAmount: number | null;
    increaseStartPeriod: number;
    rejectReason: string | null;
    comment: string | null;
    increaseText: string | null;
  };
  approvals: SalaryRequestApproval[];
  links: SalaryRequestLink[];
}

export interface ClosedSalaryRequestPeriod {
  period: number;
  closedBy: number;
  closedAt: string;
  comment: string;
}

export interface SalaryRequestReportBody {
  employeeId: number;
  type: SalaryRequestType;
  budgetBusinessAccount: number | null;
  budgetExpectedFundingUntil: string | null;
  increaseAmount: number | null;
  currentSalaryAmount: number | null;
  previousSalaryIncreaseDate: string | null;
  previousSalaryIncreaseText: string | null;
  plannedSalaryAmount: number | null;
  increaseStartPeriod: number;
  assessmentId: number | null;
  reason: string | null;
  comment: string | null;
}

export interface SalaryRequestUpdateBody {
  budgetBusinessAccount: number | null;
  budgetExpectedFundingUntil: string | null;
  increaseAmount: number | null;
  currentSalaryAmount: number | null;
  previousSalaryIncreaseDate: string | null;
  previousSalaryIncreaseText: string | null;
  plannedSalaryAmount: number | null;
  increaseStartPeriod: number;
  assessmentId: number | null;
  reason: string | null;
  comment: string | null;
  newPosition: number | null;
}

export interface SalaryRequestCommentBody {
  comment: string | null;
}

export interface SalaryRequestApproveBody {
  comment: string | null;
}

export interface SalaryRequestDeclineBody {
  comment: string | null;
}

export interface SalaryRequestLinkCreateBody {
  source: number;
  destination: number;
  type: SalaryRequestLinkType;
  comment: string | null;
}

export interface EmployeeWithLatestSalaryRequest {
  id: number;
  employeeId: number;
  employeeDisplayName: string;
  employeeEmail: string;
  employeeBusinessAccount: Dict | null;
  employeeCurrentProject: CurrentProjectDict | null;
  employeeDateOfEmployment: string | null;
  requestId: number | null;
  requestCreatedAt: string | null;
  requestStartPeriod: number | null;
  requestReqIncreaseAmount: number | null;
  requestImplIncreaseAmount: number | null;
  requestImplSalaryAmount: number | null;
  requestImplState: SalaryRequestImplementationState | null;
}

/**
 * One approve, decline or comment for salary request.
 */
export interface SalaryRequestApproval {
  id: number;
  requestId: number;
  state: SalaryApprovalState;
  comment: string | null;
  createdAt: string;
  createdBy: Dict;
}

/**
 * Link to another salary request of the same employee.
 */
export interface SalaryRequestLink {
  id: number;
  initiator: boolean;
  linkedRequest: SalaryLinkedRequest;
  type: SalaryRequestLinkType;
  comment: string | null;
  createdAt: string;
  createdBy: Dict;
}

/**
 * Opposite salary request in link.
 */
export interface SalaryLinkedRequest {
  id: number;
  period: number;
  implState: number;
  createdAt: string;
  createdBy: Dict;
}

export async function fetchSalaryRequests(period: number): Promise<SalaryIncreaseRequest[]> {
  const response = await http.get<SalaryIncreaseRequest[]>(`v1/salaries/requests/${period}`);
  return response.data;
}

export async function fetchSalaryRequest(
  period: number,
  requestId: number,
): Promise<SalaryIncreaseRequest> {
  const response = await http.get<SalaryIncreaseRequest>(
    `v1/salaries/requests/${period}/${requestId}`,
  );
  return response.data;
}

export async function reportSalaryRequest(body: SalaryRequestReportBody): Promise<number> {
  const response = await http.post<number>("v1/salaries/requests", body);
  return response.data;
}

export async function deleteSalaryRequest(requestId: number): Promise<number> {
  const response = await http.delete<number>(`v1/salaries/requests/${requestId}`);
  return response.data;
}

export async function updateSalaryRequest(
  requestId: number,
  body: SalaryRequestUpdateBody,
): Promise<number> {
  const response = await http.put<number>(`v1/salaries/requests/${requestId}`, body);
  return response.data;
}

export async function fetchClosedSalaryRequestPeriods(): Promise<ClosedSalaryRequestPeriod[]> {
  const response = await http.get<ClosedSalaryRequestPeriod[]>("v1/salaries/requests/periods");
  return response.data;
}

export async function approveSalaryRequest(
  requestId: number,
  body: SalaryRequestApproveBody,
): Promise<number> {
  const response = await http.post<number>(
    `v1/salaries/requests/${requestId}/approvals/approve`,
    body,
  );
  return response.data;
}

export async function declineSalaryRequest(
  requestId: number,
  body: SalaryRequestDeclineBody,
): Promise<number> {
  const response = await http.post<number>(
    `v1/salaries/requests/${requestId}/approvals/decline`,
    body,
  );
  return response.data;
}

export async function commentSalaryRequest(
  requestId: number,
  body: SalaryRequestCommentBody,
): Promise<number> {
  const response = await http.post<number>(
    `v1/salaries/requests/${requestId}/approvals/comment`,
    body,
  );
  return response.data;
}

export async function deleteSalaryRequestApproval(
  requestId: number,
  approvalId: number,
): Promise<number> {
  const response = await http.delete<number>(
    `v1/salaries/requests/${requestId}/approvals/${approvalId}`,
  );
  return response.data;
}

export async function fetchSalaryRequestApprovals(
  requestId: number,
): Promise<SalaryRequestApproval[]> {
  const response = await http.get<SalaryRequestApproval[]>(
    `v1/salaries/requests/${requestId}/approvals`,
  );
  return response.data;
}

export async function fetchEmployeesWithLatestSalaryRequest():
Promise<EmployeeWithLatestSalaryRequest[]> {
  const response = await http.get<EmployeeWithLatestSalaryRequest[]>("v1/salaries/requests/latest");
  return response.data;
}

export async function addSalaryRequestLink(body: SalaryRequestLinkCreateBody): Promise<number> {
  const response = await http.post<number>("v1/salaries/requests/links", body);
  return response.data;
}

export async function deleteSalaryRequestLink(linkId: number): Promise<number> {
  const response = await http.delete<number>(`v1/salaries/requests/links/${linkId}`);
  return response.data;
}
