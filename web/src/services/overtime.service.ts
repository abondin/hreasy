import http from "@/lib/http";

export interface OvertimeItem {
  id?: number;
  date: string;
  projectId?: number;
  hours: number;
  notes?: string;
  createdAt?: string | null;
  deletedAt?: string | null;
}

export type ApprovalDecisionType = "APPROVED" | "DECLINED";

export interface ApprovalDecision {
  id: number;
  decisionTime: string;
  decision: ApprovalDecisionType;
  approver: number;
  approverDisplayName: string;
  comment?: string;
  outdated: boolean;
}

export interface OvertimeReport {
  employeeId: number;
  id?: number;
  period: number;
  items: OvertimeItem[];
  approvals: ApprovalDecision[];
  lastUpdate: string | null;
}

export type CommonApprovalStatus =
  | "NO_DECISIONS"
  | "DECLINED"
  | "APPROVED_NO_DECLINED"
  | "APPROVED_OUTDATED";

export interface OvertimeDaySummary {
  date: string;
  projectId: number;
  hours: number;
}

export interface OvertimeEmployeeSummary {
  employeeId: number;
  items: OvertimeDaySummary[];
  commonApprovalStatus: CommonApprovalStatus;
}

export interface ClosedOvertimePeriod {
  period: number;
  closedBy: number;
  closedAt: string;
  comment: string;
}

export class ReportPeriod {
  static fromPeriodId(periodId: number): ReportPeriod {
    const year = Math.floor(periodId / 100);
    const month = periodId - year * 100;
    return new ReportPeriod(year, month);
  }

  static currentPeriod(shiftMonths = 0): ReportPeriod {
    const now = new Date();
    const date = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    if (date.getDate() < 5) {
      date.setMonth(date.getMonth() - 1);
    }
    if (shiftMonths !== 0) {
      date.setMonth(date.getMonth() + shiftMonths);
    }
    return new ReportPeriod(date.getFullYear(), date.getMonth());
  }

  static currentAndNextPeriods(shift = 0): ReportPeriod[] {
    return [
      ReportPeriod.currentPeriod(shift),
      ReportPeriod.currentPeriod(shift + 1),
      ReportPeriod.currentPeriod(shift + 2),
    ];
  }

  constructor(
    private year: number,
    private month: number,
  ) {}

  periodId(): number {
    return this.year * 100 + this.month;
  }

  get id(): number {
    return this.periodId();
  }

  increment(): void {
    this.month += 1;
    if (this.month >= 12) {
      this.month = 0;
      this.year += 1;
    }
  }

  decrement(): void {
    this.month -= 1;
    if (this.month < 0) {
      this.month = 11;
      this.year -= 1;
    }
  }

  daysCount(): number {
    return new Date(this.year, this.month + 1, 0).getDate();
  }

  toString(): string {
    const text = new Intl.DateTimeFormat("ru-RU", {
      month: "long",
      year: "numeric",
    }).format(new Date(this.year, this.month, 1));
    return text.replace(" г.", "");
  }
}

export async function fetchOvertimeReport(
  employeeId: number,
  reportPeriod: number,
): Promise<OvertimeReport> {
  const response = await http.get<OvertimeReport>(
    `v1/overtimes/${employeeId}/report/${reportPeriod}`,
  );
  return response.data;
}

export async function fetchOvertimesSummary(
  reportPeriod: number,
): Promise<OvertimeEmployeeSummary[]> {
  const response = await http.get<OvertimeEmployeeSummary[]>(
    `v1/overtimes/summary/${reportPeriod}`,
  );
  return response.data;
}

export async function fetchClosedOvertimes(): Promise<ClosedOvertimePeriod[]> {
  const response = await http.get<ClosedOvertimePeriod[]>("v1/overtimes/closed-periods");
  return response.data;
}

export async function addOvertimeItem(
  employeeId: number,
  reportPeriod: number,
  item: OvertimeItem,
): Promise<OvertimeReport> {
  const payload: OvertimeItem = {
    ...item,
    createdAt: item.createdAt ?? new Date().toISOString(),
  };
  const response = await http.post<OvertimeReport>(
    `v1/overtimes/${employeeId}/report/${reportPeriod}/item`,
    payload,
  );
  return response.data;
}

export async function deleteOvertimeItem(
  employeeId: number,
  reportPeriod: number,
  itemId: number,
): Promise<OvertimeReport> {
  const response = await http.delete<OvertimeReport>(
    `v1/overtimes/${employeeId}/report/${reportPeriod}/item/${itemId}`,
  );
  return response.data;
}

export async function approveOvertimeReport(
  employeeId: number,
  reportPeriod: number,
  comment: string | null,
  previousDecisionId: number | null,
): Promise<OvertimeReport> {
  const response = await http.post<OvertimeReport>(
    `v1/overtimes/${employeeId}/report/${reportPeriod}/approve`,
    {
      comment,
      previousApprovalId: previousDecisionId,
    },
  );
  return response.data;
}

export async function declineOvertimeReport(
  employeeId: number,
  reportPeriod: number,
  comment: string,
  previousDecisionId: number | null,
): Promise<OvertimeReport> {
  const response = await http.post<OvertimeReport>(
    `v1/overtimes/${employeeId}/report/${reportPeriod}/decline`,
    {
      comment,
      previousApprovalId: previousDecisionId,
    },
  );
  return response.data;
}

export async function exportOvertimes(reportPeriod: number): Promise<void> {
  const response = await http.get<ArrayBuffer>(`v1/overtimes/summary/${reportPeriod}/export`, {
    responseType: "arraybuffer",
  });
  const blob = new Blob([response.data], {
    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  });
  const link = document.createElement("a");
  link.href = window.URL.createObjectURL(blob);
  link.download = `Overtimes-${reportPeriod + 1}.xlsx`;
  link.click();
  window.URL.revokeObjectURL(link.href);
}

export function totalOvertimeHours(report: OvertimeReport | null): number {
  if (!report || report.items.length === 0) {
    return 0;
  }
  return report.items.reduce((sum, item) => sum + item.hours, 0);
}
