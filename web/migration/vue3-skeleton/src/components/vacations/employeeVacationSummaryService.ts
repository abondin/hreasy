import type { CurrentProjectDict } from "@/services/employee.service";
import type { Vacation, VacationStatus } from "@/services/vacation.service";
import { parseDateOnly } from "@/lib/vacation-dates";

const SUMMARY_STATUSES: VacationStatus[] = [
  "PLANNED",
  "TAKEN",
  "COMPENSATION",
  "REQUESTED",
];

export interface EmployeeVacationSummary {
  employee: number;
  employeeDisplayName: string;
  employeeCurrentProject?: CurrentProjectDict | null;
  year: number;
  upcomingVacation?: Vacation;
  vacationsNumber: number;
  vacationsTotalDays: number;
}

function isUpcoming(start?: string, end?: string): boolean {
  if (!start || !end) {
    return false;
  }
  const startDate = parseDateOnly(start);
  const endDate = parseDateOnly(end);
  if (!startDate || !endDate) {
    return false;
  }
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return endDate >= today;
}

function pickUpcoming(a: Vacation | undefined, b: Vacation): Vacation {
  if (!a) {
    return b;
  }
  const aStart = parseDateOnly(a.startDate);
  const bStart = parseDateOnly(b.startDate);
  if (!aStart) {
    return b;
  }
  if (!bStart) {
    return a;
  }
  return bStart < aStart ? b : a;
}

export const employeeVacationSummaryMapper = {
  map(vacations: Vacation[]): EmployeeVacationSummary[] {
    const grouped = new Map<number, Vacation[]>();

    vacations.forEach((vacation) => {
      if (!grouped.has(vacation.employee)) {
        grouped.set(vacation.employee, []);
      }
      grouped.get(vacation.employee)?.push(vacation);
    });

    const result: EmployeeVacationSummary[] = [];

    grouped.forEach((employeeVacations) => {
      const base = employeeVacations[0];
      if (!base) {
        return;
      }
      const summary: EmployeeVacationSummary = {
        employee: base.employee,
        employeeDisplayName: base.employeeDisplayName,
        employeeCurrentProject: base.employeeCurrentProject,
        year: base.year,
        vacationsNumber: 0,
        vacationsTotalDays: 0,
        upcomingVacation: undefined,
      };

      employeeVacations.forEach((vacation) => {
        if (!SUMMARY_STATUSES.includes(vacation.status)) {
          return;
        }
        summary.vacationsNumber += 1;
        summary.vacationsTotalDays += vacation.daysNumber || 0;
        if (isUpcoming(vacation.startDate, vacation.endDate)) {
          summary.upcomingVacation = pickUpcoming(
            summary.upcomingVacation,
            vacation,
          );
        }
      });

      result.push(summary);
    });

    return result;
  },
};
