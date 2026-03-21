import http from "@/lib/http";

export async function closeOvertimePeriod(
  periodId: number,
  comment?: string,
): Promise<void> {
  await http.post(`v1/admin/overtimes/${periodId}/close`, { comment });
}

export async function reopenOvertimePeriod(
  periodId: number,
  comment?: string,
): Promise<void> {
  await http.post(`v1/admin/overtimes/${periodId}/reopen`, { comment });
}
