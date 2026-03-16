import http from "@/lib/http";
import type { Dict } from "@/services/employee.service";

/**
 * Lightweight assessment payload used by salary request forms.
 */
export interface AssessmentBase {
  id: number;
  plannedDate: string | null;
  canceledAt: string | null;
  employee: Dict | null;
}

export async function fetchEmployeeAssessments(employeeId: number): Promise<AssessmentBase[]> {
  const response = await http.get<AssessmentBase[]>(`v1/assessment/${employeeId}`);
  return response.data;
}
