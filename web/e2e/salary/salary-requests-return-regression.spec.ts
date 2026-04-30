import { expect, test, type Page, type Route } from "@playwright/test";
import { mockAuthenticatedUser } from "../support/auth-mocks";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

type Dict = {
  id: number;
  name: string;
};

type SalaryRequestItem = ReturnType<typeof createSalaryRequest>;

function dict(id: number, name: string): Dict {
  return { id, name };
}

function jsonResponse(route: Route, body: unknown): Promise<void> {
  return route.fulfill({
    status: 200,
    contentType: "application/json",
    body: JSON.stringify(body),
  });
}

function createSalaryRequest(id: number, employeeName: string, businessAccountName: string, createdAt: string) {
  const employeeId = 10_000 + id;
  const businessAccount = dict(200 + id, businessAccountName);

  return {
    id,
    employee: dict(employeeId, employeeName),
    type: 1 as const,
    budgetBusinessAccount: businessAccount,
    budgetExpectedFundingUntil: "2026-12-31",
    createdAt,
    createdBy: dict(1, "Salary Manager"),
    assessment: null,
    employeeInfo: {
      currentProject: {
        id: 300 + id,
        name: `Project ${id}`,
        role: "Developer",
      },
      dateOfEmployment: "2024-01-15",
      ba: businessAccount,
      position: dict(900 + id, `Position ${id}`),
      currentSalaryAmount: 100000 + id * 1000,
      previousSalaryIncreaseText: null,
      previousSalaryIncreaseDate: null,
    },
    req: {
      increaseAmount: 15000 + id * 100,
      plannedSalaryAmount: 115000 + id * 1000,
      increaseStartPeriod: 202604,
      newPosition: null,
      reason: `Reason ${id}`,
      comment: null,
    },
    impl: undefined as undefined | {
      implementedAt: string;
      implementedBy: Dict;
      state: 1;
      newPosition: null;
      increaseAmount: number;
      salaryAmount: number;
      increaseStartPeriod: number;
      rejectReason: null;
      comment: null;
      increaseText: string;
    },
    approvals: [],
    links: [],
  };
}

async function mockSalaryRequestsWorkflow(page: Page): Promise<SalaryRequestItem[]> {
  const requests = [
    createSalaryRequest(1, "Employee Alpha", "BA Zeta", "2026-03-01T09:00:00Z"),
    createSalaryRequest(2, "Employee Bravo", "BA Alpha", "2026-03-02T09:00:00Z"),
    createSalaryRequest(3, "Employee Charlie", "BA Middle", "2026-03-03T09:00:00Z"),
  ];

  await page.route("**/api/v1/business_account", async (route) => {
    await jsonResponse(route, requests.map((request) => ({
      ...request.budgetBusinessAccount,
      active: true,
    })));
  });

  await page.route("**/api/v1/salaries/requests/periods", async (route) => {
    await jsonResponse(route, []);
  });

  await page.route(/\/api\/v1\/admin\/salaries\/requests\/employee\/\d+$/, async (route) => {
    await jsonResponse(route, requests);
  });

  await page.route(/\/api\/v1\/employee\/\d+$/, async (route) => {
    const employeeId = Number(route.request().url().match(/\/employee\/(\d+)$/)?.[1] ?? 0);
    const request = requests.find((item) => item.employee.id === employeeId);
    await jsonResponse(route, {
      id: employeeId,
      displayName: request?.employee.name ?? "Employee",
      ba: request?.budgetBusinessAccount ?? null,
      currentProject: request?.employeeInfo.currentProject ?? null,
      position: request?.employeeInfo.position ?? null,
      department: dict(1, "Department"),
      email: "employee@example.test",
    });
  });

  await page.route("**/api/v1/dict/positions", async (route) => {
    await jsonResponse(route, [{ id: 901, name: "Position", active: true }]);
  });

  await page.route(/\/api\/v1\/salaries\/requests\/\d+\/\d+$/, async (route) => {
    const requestId = Number(route.request().url().match(/\/requests\/\d+\/(\d+)$/)?.[1] ?? 0);
    await jsonResponse(route, requests.find((item) => item.id === requestId));
  });

  await page.route(/\/api\/v1\/admin\/salaries\/requests\/\d+\/implement$/, async (route) => {
    const requestId = Number(route.request().url().match(/\/requests\/(\d+)\/implement$/)?.[1] ?? 0);
    const request = requests.find((item) => item.id === requestId);
    if (request) {
      request.impl = {
        implementedAt: "2026-04-30T12:00:00Z",
        implementedBy: dict(2, "Implementation Manager"),
        state: 1,
        newPosition: null,
        increaseAmount: request.req.increaseAmount ?? 0,
        salaryAmount: request.req.plannedSalaryAmount ?? 0,
        increaseStartPeriod: request.req.increaseStartPeriod,
        rejectReason: null,
        comment: null,
        increaseText: "Implemented",
      };
    }
    await jsonResponse(route, requestId);
  });

  await page.route(/\/api\/v1\/(?:admin\/)?salaries\/requests\/\d+$/, async (route) => {
    await jsonResponse(route, requests);
  });

  return requests;
}

function tableRows(page: Page) {
  return page.getByTestId(selectors.salaryRequestsTable).locator("tbody tr");
}

async function implementationStateFor(page: Page, employeeName: string): Promise<string> {
  return (await tableRows(page)
    .filter({ hasText: employeeName })
    .getByTestId("salary-requests-impl-state")
    .textContent())?.trim() ?? "";
}

async function firstEmployeeName(page: Page): Promise<string> {
  const names = await tableRows(page).locator("td:first-child").allTextContents();
  return names.map((name) => name.trim()).find((name) => name.length > 0) ?? "";
}

test.describe("Salary requests return from details", () => {
  test.beforeEach(async ({ page }) => {
    await mockAuthenticatedUser(page, {
      authorities: ["admin_salary_request", "report_salary_request"],
    });
    await mockSalaryRequestsWorkflow(page);
  });

  test("refreshes implementation state after implementing a request in details", async ({ page }) => {
    await page.goto(appPath(routes.salaryRequests), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.salaryRequestsView)).toBeVisible();
    await expect(tableRows(page).filter({ hasText: "Employee Alpha" })).toBeVisible();
    await expect.poll(() => implementationStateFor(page, "Employee Alpha")).toBe("");

    await tableRows(page).filter({ hasText: "Employee Alpha" }).locator("td").first().click();
    await expect(page.getByTestId("salary-request-details-view")).toBeVisible();
    await page.getByTestId("salary-request-implement-open").click();
    await expect(page.getByTestId("salary-request-implementation-dialog")).toBeVisible();
    await page.getByTestId("salary-request-implementation-submit").click();
    await expect(page.getByTestId("salary-request-implementation-dialog")).toBeHidden();

    await page.goBack();
    await expect(page.getByTestId(selectors.salaryRequestsView)).toBeVisible();
    await expect.poll(() => implementationStateFor(page, "Employee Alpha")).not.toBe("");
  });

  test("keeps account sorting after opening details and returning back", async ({ page }) => {
    await page.goto(appPath(routes.salaryRequests), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.salaryRequestsView)).toBeVisible();
    const table = page.getByTestId(selectors.salaryRequestsTable);
    await expect(tableRows(page).filter({ hasText: "Employee Alpha" })).toBeVisible();
    await expect(tableRows(page).filter({ hasText: "Employee Bravo" })).toBeVisible();
    await expect(tableRows(page).filter({ hasText: "Employee Charlie" })).toBeVisible();

    await table.locator("thead th").nth(7).click();
    await expect.poll(() => firstEmployeeName(page)).toBe("Employee Bravo");

    await tableRows(page).filter({ hasText: "Employee Bravo" }).locator("td").first().click();
    await expect(page.getByTestId("salary-request-details-view")).toBeVisible();

    await page.goBack();
    await expect(page.getByTestId(selectors.salaryRequestsView)).toBeVisible();
    await expect.poll(() => firstEmployeeName(page)).toBe("Employee Bravo");
  });
});
