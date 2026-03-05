import http from "@/lib/http";

export enum JuniorProgressType {
  DEGRADATION = 1,
  NO_PROGRESS = 2,
  PROGRESS = 3,
  GOOD_PROGRESS = 4,
}

export const juniorProgressTypes: JuniorProgressType[] = [
  JuniorProgressType.DEGRADATION,
  JuniorProgressType.NO_PROGRESS,
  JuniorProgressType.PROGRESS,
  JuniorProgressType.GOOD_PROGRESS,
];

export interface SimpleDict {
  id: number;
  name: string;
}

export interface ValueWithStatus<T> {
  value: T;
  status?: string | null;
}

export interface JuniorReportRatings {
  overallReadiness: number;
  competence: number;
  process: number;
  teamwork: number;
  contribution: number;
  motivation: number;
}

export interface JuniorReport {
  id: number;
  progress: JuniorProgressType;
  createdAt: string;
  createdBy: SimpleDict;
  comment: string;
  ratings: JuniorReportRatings;
}

export interface JuniorGraduation {
  graduatedAt: string;
  graduatedBy: SimpleDict;
  comment: string;
}

export interface JuniorDto {
  id: number;
  juniorEmpl: SimpleDict;
  juniorDateOfEmployment: string;
  juniorInCompanyMonths: ValueWithStatus<number>;
  monthsWithoutReport: ValueWithStatus<number>;
  mentor?: SimpleDict;
  role: string;
  currentProject?: SimpleDict;
  budgetingAccount?: SimpleDict;
  createdAt: string;
  createdBy: SimpleDict;
  graduation?: JuniorGraduation;
  reports: JuniorReport[];
  latestReport: JuniorReport;
}

export interface AddOrUpdateJuniorReportBody {
  progress: JuniorProgressType;
  comment: string;
  ratings: JuniorReportRatings;
}

export interface UpdateJuniorRegistryBody {
  mentorId: number | null;
  role: string;
  budgetingAccount: number | null;
}

export interface GraduateBody {
  comment: string | null;
}

export async function fetchJuniorsRegistry(): Promise<JuniorDto[]> {
  const response = await http.get<JuniorDto[]>("v1/udr/juniors");
  return response.data;
}

export async function fetchJuniorDetails(juniorId: number): Promise<JuniorDto> {
  const response = await http.get<JuniorDto>(`v1/udr/juniors/${juniorId}`);
  return response.data;
}

export async function updateJuniorRegistry(
  juniorId: number,
  payload: UpdateJuniorRegistryBody,
): Promise<number> {
  const response = await http.put<number>(`v1/udr/juniors/${juniorId}`, payload);
  return response.data;
}

export async function deleteJuniorFromRegistry(juniorId: number): Promise<number> {
  const response = await http.delete<number>(`v1/udr/juniors/${juniorId}`);
  return response.data;
}

export async function graduateJunior(
  juniorId: number,
  payload: GraduateBody,
): Promise<number> {
  const response = await http.put<number>(`v1/udr/juniors/${juniorId}/graduate`, payload);
  return response.data;
}

export async function cancelJuniorGraduation(juniorId: number): Promise<number> {
  const response = await http.delete<number>(`v1/udr/juniors/${juniorId}/graduate`);
  return response.data;
}

export async function createJuniorReport(
  juniorId: number,
  payload: AddOrUpdateJuniorReportBody,
): Promise<number> {
  const response = await http.post<number>(`v1/udr/juniors/${juniorId}/reports`, payload);
  return response.data;
}

export async function deleteJuniorReport(
  juniorId: number,
  reportId: number,
): Promise<number> {
  const response = await http.delete<number>(`v1/udr/juniors/${juniorId}/reports/${reportId}`);
  return response.data;
}
