import http from "@/lib/http";

export enum JuniorProgressType {
  DEGRADATION = 1,
  NO_PROGRESS = 2,
  PROGRESS = 3,
  GOOD_PROGRESS = 4,
}

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

export async function fetchJuniorsRegistry(): Promise<JuniorDto[]> {
  const response = await http.get<JuniorDto[]>("v1/udr/juniors");
  return response.data;
}
