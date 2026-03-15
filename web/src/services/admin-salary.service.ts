import http from "@/lib/http";
import type { SalaryIncreaseRequest } from "@/services/salary.service";

export interface SalaryRequestImplementBody {
  increaseAmount: number;
  salaryAmount: number | null;
  increaseStartPeriod: number;
  newPosition: number | null;
  reason: string;
  comment: string | null;
}

export interface SalaryRequestUpdateImplIncreaseTextBody {
  increaseText: string | null;
}

export interface SalaryRequestRejectBody {
  reason: string;
  comment: string | null;
  rescheduleToNewPeriod: number | null;
}

export async function fetchAllSalaryRequests(periodId: number): Promise<SalaryIncreaseRequest[]> {
  const response = await http.get<SalaryIncreaseRequest[]>(`v1/admin/salaries/requests/${periodId}`);
  return response.data;
}

export async function fetchEmployeeSalaryRequestsForAllPeriods(
  employeeId: number,
): Promise<SalaryIncreaseRequest[]> {
  const response = await http.get<SalaryIncreaseRequest[]>(
    `v1/admin/salaries/requests/employee/${employeeId}`,
  );
  return response.data;
}

export async function markSalaryRequestImplemented(
  requestId: number,
  body: SalaryRequestImplementBody,
): Promise<number> {
  const response = await http.put<number>(`v1/admin/salaries/requests/${requestId}/implement`, body);
  return response.data;
}

export async function rejectSalaryRequest(
  requestId: number,
  body: SalaryRequestRejectBody,
): Promise<number> {
  const response = await http.put<number>(`v1/admin/salaries/requests/${requestId}/reject`, body);
  return response.data;
}

export async function updateSalaryRequestImplementationText(
  requestId: number,
  body: SalaryRequestUpdateImplIncreaseTextBody,
): Promise<number> {
  const response = await http.put<number>(
    `v1/admin/salaries/requests/${requestId}/impl/increase-text`,
    body,
  );
  return response.data;
}

export async function resetSalaryRequestImplementation(requestId: number): Promise<number> {
  const response = await http.delete<number>(`v1/admin/salaries/requests/${requestId}/implementation`);
  return response.data;
}

export async function closeSalaryRequestPeriod(periodId: number, comment?: string): Promise<void> {
  await http.post(`v1/admin/salaries/requests/periods/${periodId}/close`, { comment });
}

export async function reopenSalaryRequestPeriod(periodId: number, comment?: string): Promise<void> {
  await http.post(`v1/admin/salaries/requests/periods/${periodId}/reopen`, { comment });
}

export async function exportSalaryRequests(periodId: number): Promise<void> {
  const response = await http.get<ArrayBuffer>(`v1/admin/salaries/requests/${periodId}/export`, {
    responseType: "arraybuffer",
  });
  const blob = new Blob([response.data], {
    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  });
  const link = document.createElement("a");
  link.href = window.URL.createObjectURL(blob);
  link.download = `SalaryRequest_${periodId}.xlsx`;
  link.click();
}
