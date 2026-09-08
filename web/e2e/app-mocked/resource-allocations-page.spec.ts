import { expect, test, type Page, type Route } from "@playwright/test";
import {
  appMockedAuthorities,
  installUnhandledApiGuard,
  mockAppRouteAuth,
} from "../support/app-mocked-api";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

const sheet = {
  period: 202607,
  employees: [
    {
      id: 101,
      displayName: "Alex Morgan",
      departmentId: 501,
      departmentName: "Software Development",
      currentProjectId: 302,
      currentProjectName: "Billing Gateway",
      currentProjectRole: "Java Backend Developer",
    },
    {
      id: 102,
      displayName: "Jordan Lee",
      departmentId: 501,
      departmentName: "Software Development",
      currentProjectId: 301,
      currentProjectName: "Retail Terminal Platform",
      currentProjectRole: "Business Analyst",
    },
    {
      id: 103,
      displayName: "Taylor Kim",
      departmentId: 501,
      departmentName: "Software Development",
      currentProjectId: 302,
      currentProjectName: "Billing Gateway",
      currentProjectRole: "Java Backend Developer",
    },
  ],
  projects: [
    {
      id: 301,
      name: "Retail Terminal Platform",
      departmentId: 501,
      departmentName: "Software Development",
      baId: 402,
      baName: "Alpine Operations",
      endDate: "2026-06-30",
      active: true,
      editable: true,
    },
    {
      id: 302,
      name: "Billing Gateway",
      departmentId: 501,
      departmentName: "Software Development",
      baId: 401,
      baName: "Northwind Delivery",
      active: false,
      editable: false,
    },
    ...Array.from({ length: 8 }, (_, index) => ({
      id: 303 + index,
      name: index === 0 ? "Support Portal" : `Additional Project ${index + 1}`,
      departmentId: 501,
      departmentName: "Software Development",
      baId: 401,
      baName: "Northwind Delivery",
      active: index !== 7,
      editable: index === 0,
    })),
  ],
  allocations: [
    { employeeId: 101, projectId: 301, percent: 60, revisionId: 1 },
    { employeeId: 101, projectId: 302, percent: 40, revisionId: 1 },
    { employeeId: 102, projectId: 301, percent: 30, revisionId: 1 },
  ],
  previousAllocations: [
    { employeeId: 102, projectId: 303, percent: 15, revisionId: 1 },
  ],
};

async function json(route: Route, body: unknown): Promise<void> {
  await route.fulfill({
    contentType: "application/json",
    body: JSON.stringify(body),
  });
}

async function revealAdaptiveFilter(page: Page, testId: string) {
  const filter = page.getByTestId(testId);
  if (await filter.count() === 0 || !(await filter.isVisible())) {
    await page.getByTestId("adaptive-filter-overflow").click();
  }
  return filter;
}

function projectInput(data: typeof sheet, projectId?: number) {
  const allocatedProjectIds = new Set([
    ...data.allocations.map(allocation => allocation.projectId),
    ...data.previousAllocations.map(allocation => allocation.projectId),
  ]);
  const projects = data.projects.filter(project => project.active || allocatedProjectIds.has(project.id))
    .sort((left, right) => left.name.localeCompare(right.name));
  const selectedProjectId = projectId ?? projects.find(project => project.editable)?.id ?? projects[0]?.id ?? null;
  const otherAllocations = new Map<string, { period: number; employeeId: number; percent: number }>();
  for (const value of [
    ...data.allocations.map(allocation => ({ ...allocation, period: data.period })),
    ...data.previousAllocations.map(allocation => ({ ...allocation, period: 202606 })),
  ]) {
    if (value.projectId === selectedProjectId) continue;
    const key = `${value.period}:${value.employeeId}`;
    const current = otherAllocations.get(key);
    otherAllocations.set(key, {
      period: value.period,
      employeeId: value.employeeId,
      percent: (current?.percent ?? 0) + value.percent,
    });
  }
  return {
    year: 2026,
    selectedProjectId,
    months: Array.from({ length: 12 }, (_, month) => ({ period: 202600 + month, closed: false })),
    employees: data.employees.map(employee => ({
      ...employee,
      dateOfEmployment: "2020-01-01",
      dateOfDismissal: null,
      dismissed: false,
    })),
    projects,
    allocations: [
      ...data.allocations.filter(value => value.projectId === selectedProjectId)
        .map(value => ({ period: data.period, employeeId: value.employeeId, percent: value.percent, revisionId: value.revisionId })),
      ...data.previousAllocations.filter(value => value.projectId === selectedProjectId)
        .map(value => ({ period: 202606, employeeId: value.employeeId, percent: value.percent, revisionId: value.revisionId })),
    ],
    otherAllocations: [...otherAllocations.values()],
  };
}

function analytics(data: typeof sheet) {
  const allocations = [
    ...data.allocations.map(value => ({ ...value, period: data.period })),
    ...data.previousAllocations.map(value => ({ ...value, period: 202606 })),
  ];
  const employeeIds = new Set(allocations.map(value => value.employeeId));
  const projectIds = new Set(allocations.map(value => value.projectId));
  return {
    year: 2026,
    employees: data.employees.filter(employee => employeeIds.has(employee.id)),
    projects: data.projects.filter(project => projectIds.has(project.id)),
    workstreams: [],
    allocations,
  };
}

async function mockResourceAllocationsApi(page: Page, data = sheet): Promise<void> {
  await page.route(/\/api\/v1\/resource-allocations\/(?:input\/\d+(?:\/\d+)?|analytics\/\d+|closed-periods\/\d+)(?:\?.*)?$/, async (route) => {
    const url = new URL(route.request().url());
    if (url.pathname.includes("/closed-periods/")) {
      const periods = route.request().method() === "PUT"
        ? (route.request().postDataJSON() as { closedPeriods: number[] }).closedPeriods
        : [];
      await json(route, periods);
      return;
    }
    if (url.pathname.includes("/analytics/")) {
      await json(route, analytics(data));
      return;
    }
    if (url.pathname.includes("/input/")) {
      const projectId = url.searchParams.get("projectId");
      if (route.request().method() === "PUT") {
        const body = route.request().postDataJSON() as {
          changes: Array<{ period: number; employeeId: number; percent: number; expectedRevisionId: number | null }>;
        };
        for (const change of body.changes) {
          const current = data.allocations.find(value => value.employeeId === change.employeeId
            && value.projectId === Number(url.pathname.split("/").at(-1)));
          if (current) {
            current.percent = change.percent;
          }
        }
        await json(route, 7);
        return;
      }
      await json(route, projectInput(data, projectId == null ? undefined : Number(projectId)));
      return;
    }
  });
}

test.describe("App Mocked Resource Allocations Page", () => {
  test("rebases the draft from a backend conflict response", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.resourceAllocations);
    await page.route(/\/api\/v1\/resource-allocations\/(?:input\/\d+(?:\/\d+)?|\d+)(?:\?.*)?$/, async (route) => {
      if (new URL(route.request().url()).pathname.includes("/input/")) {
        if (route.request().method() !== "PUT") {
          await json(route, projectInput(sheet));
          return;
        }
        await route.fulfill({
          status: 409,
          contentType: "application/json",
          body: JSON.stringify({
            code: "errors.resource_allocation.conflict",
            message: "Аллокации были изменены другим пользователем",
            args: {
              allocations: [
                { period: 202607, employeeId: 101, percent: 60, revisionId: 1 },
                { period: 202607, employeeId: 102, percent: 90, revisionId: 8 },
              ],
              changes: [],
              conflicts: [{ period: 202607, employeeId: 102 }],
            },
          }),
        });
        return;
      }
      await json(route, sheet);
    });

    const inputResponse = page.waitForResponse(response => response.url().includes("/resource-allocations/")
      && response.url().includes("/input"));
    await page.goto(appPath(routes.resourceAllocations), { waitUntil: "domcontentloaded" });
    expect((await inputResponse).status()).toBe(200);
    const cell = page.getByTestId("resource-allocation-input-102-202607");
    await cell.dblclick();
    await page.locator("revogr-edit input").fill("75");
    await page.locator("revogr-edit input").press("Enter");
    await page.getByTestId(selectors.resourceAllocationsSave).click();

    await expect(cell).toHaveText("90%");
    const conflictAlert = page.getByTestId("resource-allocations-conflict");
    await expect(conflictAlert).toContainText("Не все изменения сохранены");
    await expect(conflictAlert).toContainText("уже изменил другой пользователь");
    await expect.poll(async () => (await conflictAlert.boundingBox())?.height ?? 0).toBeLessThan(160);
    await expect(cell).toHaveAttribute("data-conflict", "");
    await expect(cell).toHaveCSS("background-color", "rgba(176, 0, 32, 0.16)");
    await expect(page.getByTestId(selectors.resourceAllocationsSave)).toBeDisabled();
  });

  test("edits accessible projects and saves only changed cells without backend", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, [
      ...appMockedAuthorities.resourceAllocations,
      ...appMockedAuthorities.employees,
      "create_assessment",
      "project_admin_area",
    ]);
    await mockResourceAllocationsApi(page);

    await page.goto(appPath(routes.resourceAllocations), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.resourceAllocationsView)).toBeVisible();
    await expect(page).toHaveURL(/\/management\/resource-allocations\/input\?year=2026&projectId=301$/);
    await expect(page.getByTestId("resource-allocations-tab-input")).toHaveAttribute("aria-selected", "true");
    const projectFilter = await revealAdaptiveFilter(page, "resource-allocations-input-project");
    await expect(projectFilter).toContainText("Retail Terminal Platform");
    await projectFilter.click();
    await expect(page.getByText("Закрыт: 30.06.2026", { exact: true })).toBeVisible();
    await page.keyboard.press("Escape");
    const inputCell = page.getByTestId("resource-allocation-input-102-202607");
    await expect(page.getByTestId("resource-allocation-input-101-202607")).toContainText("+ 40%");
    await expect(inputCell).toBeVisible();
    await expect(page.getByTestId("resource-allocation-input-employee-101")).toContainText("Alex Morgan");
    await expect(page.getByTestId("resource-allocation-input-employee-101")).toContainText("Java Backend Developer");
    const inputGrid = page.getByTestId("resource-allocations-input-grid");
    await expect.poll(async () => (await inputGrid.boundingBox())?.height ?? 0).toBeGreaterThan(350);
    const addEmployee = page.getByTestId("resource-allocations-add-employee");
    await addEmployee.locator("input").fill("Billing Gateway");
    const employeeOption = page.getByRole("option").filter({ hasText: "Taylor Kim" });
    await expect(employeeOption).toContainText("Текущий проект: Billing Gateway");
    await expect(employeeOption).toContainText("Роль: Java Backend Developer");
    await addEmployee.locator("input").fill("");
    await addEmployee.locator("input").pressSequentially("Taylor Kim");
    await expect(addEmployee.locator("input")).toHaveValue("Taylor Kim");
    await employeeOption.click();
    await expect(page.getByTestId("resource-allocation-input-employee-103")).toBeVisible();
    await inputCell.dblclick();
    await page.locator("revogr-edit input").fill("75");
    await page.locator("revogr-edit input").press("Enter");
    const saveRequest = page.waitForRequest(request => request.method() === "PUT"
      && /\/api\/v1\/resource-allocations\/input\/\d+\/\d+$/.test(request.url()));
    await page.getByTestId(selectors.resourceAllocationsSave).click();
    expect((await saveRequest).postDataJSON()).toEqual({
      changes: [{ period: 202607, employeeId: 102, percent: 75, expectedRevisionId: 1 }],
    });
    await page.getByTestId("resource-allocations-tab-analytics").click();
    await expect(page).toHaveURL(/\/management\/resource-allocations\/analytics$/);
    await expect(page.getByTestId("resource-allocations-tab-analytics")).toHaveAttribute("aria-selected", "true");
    await page.getByLabel("Открыть меню").click();
    const managerNavigation = page.locator(".v-list-group").filter({ hasText: "Менеджерам" });
    await managerNavigation.getByText("Менеджерам", { exact: true }).click();
    await expect(managerNavigation.getByRole("link", { name: "Ассессменты" })).toBeVisible();
    await expect(managerNavigation.getByRole("link", { name: "Все проекты" })).toBeVisible();
    await page.locator(".v-navigation-drawer__scrim").click();
    await expect(page.getByTestId(selectors.resourceAllocationsTable)).toBeVisible();
    await expect(page.getByTestId("resource-allocation-group-ba:402-label")).toContainText("Alpine Operations");
    await expect(page.getByTestId("resource-allocation-group-ba:402,project:301-label")).toContainText("Retail Terminal Platform");
    await expect(page.getByTestId("resource-allocation-group-ba:402,project:301-202607")).toHaveText("135");
    await expect(page.getByTestId("resource-allocation-analytics-row-101:301:project")).toContainText("Alex Morgan");
    await expect(page.getByTestId("resource-allocation-analytics-cell-101:301:project-202607")).toHaveText("60");
    await expect(page.getByTestId("resource-allocation-analytics-cell-102:303:project-202606")).toHaveText("15");
    await expect(page.getByTestId("resource-allocation-analytics-cell-102:303:project-202600"))
      .toHaveClass(/resource-allocation-terminal-cell/);
    const terminalBackground = await page
      .getByTestId("resource-allocation-analytics-cell-101:301:project-202607")
      .locator("..")
      .evaluate((cell) => getComputedStyle(cell).backgroundColor);
    await expect(page.getByTestId("resource-allocation-analytics-cell-102:303:project-202600").locator(".."))
      .toHaveCSS("background-color", terminalBackground);

    const analyticsSearch = (await revealAdaptiveFilter(page, "resource-allocations-analytics-search")).locator("input");
    await analyticsSearch.fill("Billing Gateway");
    await expect(page.getByTestId("resource-allocation-analytics-row-101:302:project")).toBeVisible();
    await expect(page.getByTestId("resource-allocation-analytics-row-101:301:project")).toHaveCount(0);
    await analyticsSearch.fill("Alex Morgan");
    await expect(page.getByTestId("resource-allocation-analytics-row-101:301:project")).toBeVisible();
    await expect(page.getByTestId("resource-allocation-analytics-row-101:302:project")).toBeVisible();
    await expect(page.getByTestId("resource-allocation-analytics-row-102:301:project")).toHaveCount(0);
    await analyticsSearch.fill("Java Backend");
    await expect(page.getByTestId("resource-allocation-analytics-row-101:301:project")).toContainText("Java Backend Developer");
    await expect(page.getByTestId("resource-allocation-analytics-row-102:301:project")).toHaveCount(0);
    await analyticsSearch.fill("");

    const businessAccountFilter = await revealAdaptiveFilter(page, "resource-allocations-business-accounts");
    await businessAccountFilter.click();
    await page.getByRole("option", { name: "Northwind Delivery" }).click();
    await page.keyboard.press("Escape");
    const analyticsProjectFilter = await revealAdaptiveFilter(page, "resource-allocations-projects");
    await analyticsProjectFilter.click();
    await expect(page.getByRole("option", { name: "Support Portal" })).toBeVisible();
    await expect(page.getByRole("option", { name: "Retail Terminal Platform" })).toHaveCount(0);
    await page.getByRole("option", { name: "Support Portal" }).click();
    await page.keyboard.press("Escape");
    await expect(page.getByTestId("resource-allocation-analytics-row-102:303:project")).toBeVisible();
    await expect(page.getByTestId("resource-allocation-analytics-row-101:302:project")).toHaveCount(0);

    await page.getByTestId("resource-allocation-analytics-cell-102:303:project-202606").dblclick();
    await expect(page.locator("revogr-edit input")).toHaveCount(0);

    await page.getByTestId("resource-allocations-analytics-mode").getByRole("button", { name: "Сотрудники" }).click();
    const employeeGroup = page.getByTestId("resource-allocation-group-employee:102-label");
    await expect(employeeGroup).toContainText("Jordan Lee");
    await expect(employeeGroup).toContainText("Support Portal");
    await employeeGroup.click();
    await expect(page.getByTestId("resource-allocation-analytics-row-102:303:project")).toHaveText("Support Portal");
  });

  test("keeps a large populated annual hierarchy responsive", async ({ page }) => {
    const largeSheet = {
      ...sheet,
      employees: Array.from({ length: 500 }, (_, index) => ({
        ...sheet.employees[0],
        id: 1_000 + index,
        displayName: `Employee ${index + 1}`,
        currentProjectId: 2_000 + (index % 2),
        currentProjectName: `Project ${(index % 2) + 1}`,
      })),
      projects: Array.from({ length: 80 }, (_, index) => ({
        ...sheet.projects[0],
        id: 2_000 + index,
        name: `Project ${index + 1}`,
        baId: index % 2 === 0 ? 402 : 401,
        baName: index % 2 === 0 ? "Alpine Operations" : "Northwind Delivery",
      })),
      allocations: Array.from({ length: 5_000 }, (_, index) => ({
        employeeId: 1_000 + Math.floor(index / 10),
        projectId: 2_000 + (index % 10),
        percent: 10,
        revisionId: 1,
      })),
    };
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.resourceAllocations);
    await mockResourceAllocationsApi(page, largeSheet);

    await page.goto(appPath(`${routes.resourceAllocations}/analytics`), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId("resource-allocations-tab-analytics")).toHaveAttribute("aria-selected", "true");
    const cell = page.getByTestId("resource-allocation-analytics-cell-1000:2000:project-202607");
    await expect(cell).toBeVisible();
    const editor = page.locator("revogr-edit input");
    await expect(editor).toHaveCount(0);

    await cell.dblclick();
    await expect(editor).toHaveCount(0);
    const businessAccountFilter = await revealAdaptiveFilter(page, "resource-allocations-business-accounts");
    await businessAccountFilter.click();
    const filterStartedAt = Date.now();
    await page.getByRole("option", { name: "Northwind Delivery" }).click();
    await page.keyboard.press("Escape");
    await expect(page.getByTestId(selectors.resourceAllocationsTable)).toBeVisible();
    expect(Date.now() - filterStartedAt).toBeLessThan(2_000);
    const projectFilter = await revealAdaptiveFilter(page, "resource-allocations-projects");
    await projectFilter.click();
    await projectFilter.locator("input").fill("Project 10");
    await page.getByRole("option", { name: "Project 10", exact: true }).click();
    await page.keyboard.press("Escape");
    await expect(page.getByTestId("resource-allocation-analytics-row-1000:2009:project")).toBeVisible();
  });
});
