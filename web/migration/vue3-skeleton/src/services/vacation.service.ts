import http from "@/lib/http";

export interface EmployeeVacationShort {
  id: number;
  year: number;
  startDate: string;
  endDate: string;
  current: boolean;
}

export async function fetchCurrentOrFutureVacations(
  employeeId: number,
): Promise<EmployeeVacationShort[]> {
  const response = await http.get<EmployeeVacationShort[]>(
    `v1/vacations/${employeeId}/currentOrFuture`,
  );
  return response.data;
}
