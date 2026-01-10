import http from "@/lib/http";

export async function fetchDaysNotIncludedInVacations(
  years: number[],
): Promise<string[]> {
  if (!years.length) {
    return [];
  }
  const response = await http.get<string[]>(
    `v1/dict/calendar/days_not_included_in_vacations/${years.join(",")}`,
  );
  return response.data;
}
