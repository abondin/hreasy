import { type Page, type Route } from "@playwright/test";
import { mockAuthenticatedUser } from "./auth-mocks";

type EmployeeRecord = {
  id: number;
  displayName: string;
  email: string;
  birthday?: string | null;
  currentProject?: {
    id: number;
    name: string;
    role: string | null;
  } | null;
  ba?: {
    id: number;
    name: string;
  } | null;
  department?: {
    id: number;
    name: string;
  } | null;
};

type VacationRecord = {
  id: number;
  employee: number;
  employeeDisplayName: string;
  employeeCurrentProject?: {
    id: number;
    name: string;
    role: string | null;
  } | null;
  year: number;
  startDate: string;
  endDate: string;
  notes: string;
  canceled: boolean;
  status: "PLANNED" | "TAKEN" | "REQUESTED";
  documents: string;
  daysNumber: number;
};

type OvertimeSummaryRecord = {
  employeeId: number;
  items: Array<{ date: string; projectId: number; hours: number }>;
  commonApprovalStatus: "NO_DECISIONS" | "DECLINED" | "APPROVED_NO_DECLINED" | "APPROVED_OUTDATED";
};

function jsonResponse(route: Route, body: unknown, status = 200): Promise<void> {
  return route.fulfill({
    status,
    contentType: "application/json",
    body: JSON.stringify(body),
  });
}

function downloadResponse(route: Route, body = "stub"): Promise<void> {
  return route.fulfill({
    status: 200,
    headers: {
      "content-type": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "content-disposition": 'attachment; filename="stub.xlsx"',
    },
    body,
  });
}

const baseEmployees: EmployeeRecord[] = [
  {
    id: 101,
    displayName: "Alex Morgan",
    email: "alex.morgan@example.test",
    birthday: "14.09",
    currentProject: { id: 301, name: "Retail Terminal Platform", role: "Backend C#" },
    ba: { id: 401, name: "Northwind Delivery" },
    department: { id: 501, name: "Software Development" },
  },
  {
    id: 102,
    displayName: "Jordan Lee",
    email: "jordan.lee@example.test",
    currentProject: { id: 302, name: "Pi.One M2M IoT Dev", role: "Analyst" },
    ba: { id: 402, name: "STM RND" },
    department: { id: 501, name: "Software Development" },
  },
  {
    id: 103,
    displayName: "Casey Taylor",
    email: "casey.taylor@example.test",
    currentProject: { id: 303, name: "Billing Gateway", role: "Backend Java" },
    ba: { id: 401, name: "Northwind Delivery" },
    department: { id: 501, name: "Software Development" },
  },
  {
    id: 104,
    displayName: "Riley Brooks",
    email: "riley.brooks@example.test",
    currentProject: { id: 301, name: "Retail Terminal Platform", role: "System Analyst" },
    ba: { id: 401, name: "Northwind Delivery" },
    department: { id: 501, name: "Software Development" },
  },
  {
    id: 105,
    displayName: "Morgan Reed",
    email: "morgan.reed@example.test",
    currentProject: { id: 304, name: "Healthcare Portal", role: "QA" },
    ba: { id: 403, name: "Acme Telecom" },
    department: { id: 502, name: "Quality Assurance" },
  },
  {
    id: 106,
    displayName: "Jamie Parker",
    email: "jamie.parker@example.test",
    currentProject: { id: 305, name: "UpKeep Service", role: "Cleaning specialist" },
    ba: { id: 404, name: "Administrative Services" },
    department: { id: 503, name: "Operations" },
  },
];

export const appMockedEmployees: EmployeeRecord[] = Array.from({ length: 4 }, (_, batchIndex) =>
  baseEmployees.map((employee, employeeIndex) => {
    const suffix = batchIndex === 0 ? "" : ` ${batchIndex + 1}`;
    const idOffset = batchIndex * 100 + employeeIndex;
    return {
      ...employee,
      id: employee.id + idOffset,
      displayName: `${employee.displayName}${suffix}`,
      email: employee.email.replace("@", `.${batchIndex + 1}@`),
    };
  }),
).flat();

export const appMockedProjects = [
  { id: 301, name: "Retail Terminal Platform", baId: 401, active: true },
  { id: 302, name: "Pi.One M2M IoT Dev", baId: 402, active: true },
  { id: 303, name: "Billing Gateway", baId: 401, active: true },
  { id: 304, name: "Healthcare Portal", baId: 403, active: true },
  { id: 305, name: "UpKeep Service", baId: 404, active: true },
];

export const appMockedProjectRoles = [
  { value: "Backend C#" },
  { value: "Backend Java" },
  { value: "Analyst" },
  { value: "QA" },
  { value: "System Analyst" },
];

const baseVacations: VacationRecord[] = [
  {
    id: 1001,
    employee: 101,
    employeeDisplayName: "Alex Morgan",
    employeeCurrentProject: { id: 301, name: "Retail Terminal Platform", role: "Backend C#" },
    year: 2026,
    startDate: "2026-03-02",
    endDate: "2026-03-06",
    notes: "",
    canceled: false,
    status: "PLANNED",
    documents: "",
    daysNumber: 5,
  },
  {
    id: 1002,
    employee: 101,
    employeeDisplayName: "Alex Morgan",
    employeeCurrentProject: { id: 301, name: "Retail Terminal Platform", role: "Backend C#" },
    year: 2026,
    startDate: "2026-08-06",
    endDate: "2026-08-19",
    notes: "",
    canceled: false,
    status: "PLANNED",
    documents: "",
    daysNumber: 14,
  },
  {
    id: 1003,
    employee: 102,
    employeeDisplayName: "Jordan Lee",
    employeeCurrentProject: { id: 302, name: "Pi.One M2M IoT Dev", role: "Analyst" },
    year: 2026,
    startDate: "2026-01-26",
    endDate: "2026-02-01",
    notes: "",
    canceled: false,
    status: "TAKEN",
    documents: "",
    daysNumber: 7,
  },
  {
    id: 1004,
    employee: 102,
    employeeDisplayName: "Jordan Lee",
    employeeCurrentProject: { id: 302, name: "Pi.One M2M IoT Dev", role: "Analyst" },
    year: 2026,
    startDate: "2026-08-24",
    endDate: "2026-08-30",
    notes: "",
    canceled: false,
    status: "PLANNED",
    documents: "",
    daysNumber: 7,
  },
  {
    id: 1005,
    employee: 103,
    employeeDisplayName: "Casey Taylor",
    employeeCurrentProject: { id: 303, name: "Billing Gateway", role: "Backend Java" },
    year: 2026,
    startDate: "2026-06-29",
    endDate: "2026-07-12",
    notes: "",
    canceled: false,
    status: "PLANNED",
    documents: "",
    daysNumber: 14,
  },
  {
    id: 1006,
    employee: 104,
    employeeDisplayName: "Riley Brooks",
    employeeCurrentProject: { id: 301, name: "Retail Terminal Platform", role: "System Analyst" },
    year: 2026,
    startDate: "2026-10-26",
    endDate: "2026-11-01",
    notes: "",
    canceled: false,
    status: "PLANNED",
    documents: "",
    daysNumber: 7,
  },
];

export const appMockedVacations: VacationRecord[] = Array.from({ length: 4 }, (_, batchIndex) =>
  baseVacations.map((vacation, vacationIndex) => {
    const employee = appMockedEmployees[(batchIndex * baseVacations.length + vacationIndex) % appMockedEmployees.length];
    return {
      ...vacation,
      id: vacation.id + batchIndex * 100,
      employee: employee.id,
      employeeDisplayName: employee.displayName,
      employeeCurrentProject: employee.currentProject ?? vacation.employeeCurrentProject,
      startDate: shiftDate(vacation.startDate, batchIndex * 7),
      endDate: shiftDate(vacation.endDate, batchIndex * 7),
    };
  }),
).flat();

const currentPeriodId = 202603;
const nextPeriodId = 202604;

const overtimeSummaryByPeriod = new Map<number, OvertimeSummaryRecord[]>([
  [currentPeriodId, appMockedEmployees.slice(0, 18).map((employee, index) => ({
    employeeId: employee.id,
    items: [
      {
        date: shiftDate("2026-04-01", index),
        projectId: employee.currentProject?.id ?? 301,
        hours: (index % 5) + 1,
      },
      {
        date: shiftDate("2026-04-02", index),
        projectId: employee.currentProject?.id ?? 301,
        hours: ((index + 2) % 4) + 1,
      },
    ],
    commonApprovalStatus: index % 3 === 0 ? "APPROVED_NO_DECLINED" : "NO_DECISIONS",
  }))],
  [nextPeriodId, appMockedEmployees.slice(0, 18).map((employee, index) => ({
    employeeId: employee.id,
    items: [
      {
        date: shiftDate("2026-05-06", index),
        projectId: employee.currentProject?.id ?? 301,
        hours: ((index + 1) % 6) + 1,
      },
    ],
    commonApprovalStatus: index % 4 === 0 ? "DECLINED" : "NO_DECISIONS",
  }))],
]);

function shiftDate(isoDate: string, days: number): string {
  const date = new Date(`${isoDate}T00:00:00.000Z`);
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().slice(0, 10);
}

export async function mockAppRouteAuth(
  page: Page,
  authorities: string[],
): Promise<void> {
  await mockAuthenticatedUser(page, {
    username: "mocked.user",
    employeeId: 101,
    accessibleBas: [401, 402, 403, 404],
    authorities,
  });
}

export async function installUnhandledApiGuard(page: Page): Promise<void> {
  await page.route("**/api/**", async (route) => {
    await jsonResponse(
      route,
      {
        error: "Unhandled mocked API request",
        method: route.request().method(),
        url: route.request().url(),
      },
      501,
    );
  });
}

export async function mockEmployeesDirectoryApi(page: Page): Promise<void> {
  await page.route("**/api/v1/employee", async (route) => {
    await jsonResponse(route, appMockedEmployees);
  });
}

export async function mockEmployeeDetailsApi(page: Page): Promise<void> {
  await page.route("**/api/v1/employee/*", async (route) => {
    const url = new URL(route.request().url());
    const employeeId = Number(url.pathname.split("/").pop());
    const employee = appMockedEmployees.find((item) => item.id === employeeId);
    if (!employee) {
      await jsonResponse(route, { message: "Not found" }, 404);
      return;
    }
    await jsonResponse(route, {
      ...employee,
      telegram: `employee${employee.id}`,
      telegramConfirmedAt: null,
      skills: [],
      position: { id: 701, name: "Engineer" },
      officeLocation: { id: 801, name: "Test Office", mapName: null },
      officeWorkplace: "A-12",
    });
  });
}

export async function mockVacationsManagementApi(page: Page): Promise<void> {
  await mockEmployeesDirectoryApi(page);

  await page.route("**/api/v1/dict/projects", async (route) => {
    await jsonResponse(route, appMockedProjects);
  });

  await page.route("**/api/v1/employee/current_project_roles", async (route) => {
    await jsonResponse(route, appMockedProjectRoles);
  });

  await page.route("**/api/v1/dict/calendar/days_not_included_in_vacations/*", async (route) => {
    await jsonResponse(route, ["2026-01-01", "2026-01-07", "2026-02-23"]);
  });

  await page.route("**/api/v1/vacations/export**", async (route) => {
    await downloadResponse(route);
  });

  await page.route("**/api/v1/vacations**", async (route) => {
    if (route.request().method() !== "GET") {
      await jsonResponse(route, { id: 9999 });
      return;
    }
    await jsonResponse(route, appMockedVacations);
  });
}

export async function mockCurrentOrFutureVacationsApi(page: Page): Promise<void> {
  await page.route("**/api/v1/vacations/*/currentOrFuture", async (route) => {
    const url = new URL(route.request().url());
    const employeeId = Number(url.pathname.split("/").slice(-2)[0]);
    const items = appMockedVacations
      .filter((item) => item.employee === employeeId)
      .slice(0, 3)
      .map((item, index) => ({
        id: item.id,
        year: item.year,
        startDate: item.startDate,
        endDate: item.endDate,
        current: index === 0,
      }));
    await jsonResponse(route, items);
  });
}

export async function mockOvertimesSummaryApi(page: Page): Promise<void> {
  await mockEmployeesDirectoryApi(page);

  await page.route("**/api/v1/dict/projects", async (route) => {
    await jsonResponse(route, appMockedProjects);
  });

  await page.route("**/api/v1/overtimes/closed-periods", async (route) => {
    await jsonResponse(route, []);
  });

  await page.route("**/api/v1/overtimes/summary/*/export", async (route) => {
    await downloadResponse(route);
  });

  await page.route("**/api/v1/overtimes/summary/*", async (route) => {
    const url = new URL(route.request().url());
    const periodId = Number(url.pathname.split("/").pop());
    await jsonResponse(route, overtimeSummaryByPeriod.get(periodId) ?? []);
  });
}

export const appMockedAuthorities = {
  employees: ["view_empl_current_project_role"],
  vacations: ["vacation_view", "vacation_edit"],
  overtimes: ["overtime_view", "overtime_admin"],
};
