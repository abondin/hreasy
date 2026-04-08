import type { Page, Route } from "@playwright/test";

interface SalaryMockOptions {
  requestCount?: number;
  bonusCount?: number;
}

type Dict = {
  id: number;
  name: string;
};

function jsonResponse(route: Route, body: unknown): Promise<void> {
  return route.fulfill({
    status: 200,
    contentType: "application/json",
    body: JSON.stringify(body),
  });
}

function createDict(id: number, name: string): Dict {
  return { id, name };
}

function createSalaryRequest(id: number, type: 1 | 2, period: number) {
  const employeeId = 10_000 + id;
  const businessAccountId = 200 + (id % 4);
  const projectId = 300 + (id % 5);

  return {
    id,
    employee: createDict(employeeId, `Employee ${id.toString().padStart(2, "0")}`),
    type,
    budgetBusinessAccount: createDict(businessAccountId, `BA ${businessAccountId}`),
    budgetExpectedFundingUntil: "2026-12-31",
    createdAt: `2026-03-${String((id % 28) + 1).padStart(2, "0")}T09:00:00Z`,
    createdBy: createDict(1, "Salary Manager"),
    assessment: null,
    employeeInfo: {
      currentProject: {
        id: projectId,
        name: `Project ${projectId}`,
        role: id % 2 === 0 ? "Developer" : "QA",
      },
      dateOfEmployment: "2024-01-15",
      ba: createDict(businessAccountId, `BA ${businessAccountId}`),
      position: createDict(900 + (id % 3), `Position ${id % 3}`),
      currentSalaryAmount: 100000 + id * 1000,
      previousSalaryIncreaseText: "Previous increase",
      previousSalaryIncreaseDate: "2025-11-01",
    },
    req: {
      increaseAmount: type === 1 ? 15000 + id * 100 : 5000 + id * 50,
      plannedSalaryAmount: type === 1 ? 115000 + id * 1000 : null,
      increaseStartPeriod: period,
      newPosition: null,
      reason: `Reason ${id}`,
      comment: `Comment ${id}`,
    },
    impl: {
      implementedAt: `2026-03-${String((id % 28) + 1).padStart(2, "0")}T12:00:00Z`,
      implementedBy: createDict(2, "Implementation Manager"),
      state: id % 6 === 0 ? 2 : 1,
      newPosition: null,
      increaseAmount: type === 1 ? 12000 + id * 100 : 4500 + id * 50,
      salaryAmount: type === 1 ? 112000 + id * 1000 : null,
      increaseStartPeriod: period,
      rejectReason: null,
      comment: `Implementation comment ${id}`,
      increaseText: type === 1 ? `Promotion note ${id}` : null,
    },
    approvals: [
      {
        id: id * 10,
        requestId: id,
        state: 1,
        comment: `Approval ${id}`,
        createdAt: `2026-03-${String((id % 28) + 1).padStart(2, "0")}T10:00:00Z`,
        createdBy: createDict(3, "Approver"),
      },
    ],
    links: [],
  };
}

export async function mockSalaryRequestsDataset(
  page: Page,
  options: SalaryMockOptions = {},
): Promise<void> {
  const requestCount = options.requestCount ?? 80;
  const bonusCount = options.bonusCount ?? 20;

  await page.route("**/api/v1/business_account", async (route) => {
    await jsonResponse(route, [
      { id: 200, name: "BA 200", active: true },
      { id: 201, name: "BA 201", active: true },
      { id: 202, name: "BA 202", active: true },
      { id: 203, name: "BA 203", active: true },
    ]);
  });

  await page.route("**/api/v1/salaries/requests/periods", async (route) => {
    await jsonResponse(route, []);
  });

  await page.route(/\/api\/v1\/(?:admin\/)?salaries\/requests\/\d+$/, async (route) => {
    const periodMatch = route.request().url().match(/\/requests\/(\d+)$/);
    const period = periodMatch ? Number(periodMatch[1]) : 202604;
    const items = [
      ...Array.from({ length: requestCount }, (_, index) => createSalaryRequest(index + 1, 1, period)),
      ...Array.from({ length: bonusCount }, (_, index) => createSalaryRequest(requestCount + index + 1, 2, period)),
    ];
    await jsonResponse(route, items);
  });
}
