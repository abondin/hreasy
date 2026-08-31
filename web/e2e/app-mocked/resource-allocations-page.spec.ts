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
    },
    {
      id: 102,
      displayName: "Jordan Lee",
      departmentId: 501,
      departmentName: "Software Development",
      currentProjectId: 301,
      currentProjectName: "Retail Terminal Platform",
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
      active: true,
      editable: false,
    },
    ...Array.from({ length: 8 }, (_, index) => ({
      id: 303 + index,
      name: index === 0 ? "Support Portal" : `Additional Project ${index + 1}`,
      departmentId: 501,
      departmentName: "Software Development",
      baId: 401,
      baName: "Northwind Delivery",
      active: true,
      editable: index === 0,
    })),
  ],
  allocations: [
    { employeeId: 101, projectId: 301, percent: 60, revisionId: 1 },
    { employeeId: 101, projectId: 302, percent: 40, revisionId: 1 },
    { employeeId: 102, projectId: 301, percent: 30, revisionId: 1 },
  ],
};

async function json(route: Route, body: unknown): Promise<void> {
  await route.fulfill({
    contentType: "application/json",
    body: JSON.stringify(body),
  });
}

async function mockResourceAllocationsApi(page: Page, data = sheet): Promise<void> {
  await page.route(/\/api\/v1\/resource-allocations\/\d+$/, async (route) => {
    if (route.request().method() === "PUT") {
      const body = route.request().postDataJSON() as {
        changes: Array<{ employeeId: number; projectId: number; percent: number }>;
      };
      for (const change of body.changes) {
        const current = data.allocations.find(value =>
          value.employeeId === change.employeeId && value.projectId === change.projectId,
        );
        if (current) {
          current.percent = change.percent;
        }
      }
      await json(route, 7);
      return;
    }
    await json(route, data);
  });
}

test.describe("App Mocked Resource Allocations Page", () => {
  test("edits accessible projects and saves only changed cells without backend", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, [...appMockedAuthorities.resourceAllocations, "create_assessment"]);
    await mockResourceAllocationsApi(page);

    await page.goto(appPath(routes.resourceAllocations), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.resourceAllocationsView)).toBeVisible();
    await page.getByLabel("Открыть меню").click();
    const managerNavigation = page.locator(".v-list-group").filter({ hasText: "Менеджерам" });
    await managerNavigation.getByText("Менеджерам", { exact: true }).click();
    await expect(managerNavigation.getByRole("link", { name: "Ассессменты" })).toBeVisible();
    await page.locator(".v-navigation-drawer__scrim").click();
    await expect(page.getByTestId(selectors.resourceAllocationsTable)).toBeVisible();
    await expect(page.getByTestId("resource-allocations-employee-ba")).toBeVisible();
    await expect(page.getByTestId("resource-allocations-project-ba")).toBeVisible();
    const projectSearch = page.getByTestId(selectors.resourceAllocationsProjectSearch).locator("input");
    await projectSearch.fill("Support");
    await expect(page.getByRole("columnheader", { name: "Support Portal" })).toBeVisible();
    await expect(page.getByRole("columnheader", { name: "Retail Terminal Platform" })).toHaveCount(0);
    await projectSearch.fill("");
    await expect.poll(async () => Math.round(await page.getByRole("columnheader", { name: "Сотрудник" })
      .evaluate(element => element.getBoundingClientRect().width))).toBe(320);
    await expect(page.getByTestId("resource-allocation-cell-101-302")).toHaveCount(0);
    const firstRow = page.locator("tbody tr").filter({ hasText: "Alex Morgan" });
    await expect(firstRow).toContainText("100%");
    const firstProjectHeader = page.getByRole("columnheader", { name: "Retail Terminal Platform" });
    const projectXBeforeRowAction = await firstProjectHeader.evaluate(element => element.getBoundingClientRect().x);
    await firstRow.hover();
    await expect(page.getByTestId("resource-allocation-clear-row-101")).toBeVisible();
    await expect.poll(async () => firstProjectHeader.evaluate(element => element.getBoundingClientRect().x))
      .toBe(projectXBeforeRowAction);
    await expect.poll(async () => {
      const total = await firstRow.locator("td").nth(1).boundingBox();
      const project = await firstRow.locator("td").nth(2).boundingBox();
      return total && project ? Math.round(project.x - total.x - total.width) : -1;
    }).toBeGreaterThanOrEqual(0);

    const populatedCell = page.getByTestId("resource-allocation-cell-101-301");
    await populatedCell.hover();
    await page.getByTestId("resource-allocation-clear-cell-101-301").click();
    await expect(populatedCell).toHaveText("");
    const projectWidthBeforeEdit = await firstProjectHeader.evaluate(element => element.getBoundingClientRect().width);
    const cellWidthBeforeEdit = await populatedCell.evaluate(element => element.getBoundingClientRect().width);
    await populatedCell.click();
    await expect.poll(async () => populatedCell.evaluate(element => element.getBoundingClientRect().width))
      .toBe(cellWidthBeforeEdit);
    await expect.poll(async () => firstProjectHeader.evaluate(element => element.getBoundingClientRect().width))
      .toBe(projectWidthBeforeEdit);
    await populatedCell.fill("60");
    await page.keyboard.press("Tab");
    await expect.poll(async () => populatedCell.evaluate(element => element.getBoundingClientRect().width))
      .toBe(cellWidthBeforeEdit);
    await expect.poll(async () => firstProjectHeader.evaluate(element => element.getBoundingClientRect().width))
      .toBe(projectWidthBeforeEdit);
    await expect(page.getByTestId("resource-allocation-cell-101-303")).toBeFocused();
    await page.keyboard.press("Enter");
    await expect(page.getByTestId("resource-allocation-cell-102-303")).toBeFocused();
    await page.keyboard.press("Enter");

    await page.getByTestId(selectors.resourceAllocationsScopeAll).click();
    await expect(page.getByTestId("resource-allocation-cell-101-302")).toBeDisabled();
    await page.getByTestId(selectors.resourceAllocationsOnlyAllocated).click();
    await expect(page.getByTestId("resource-allocation-cell-101-302")).toBeVisible();
    await expect(page.getByTestId("resource-allocation-cell-102-302")).toHaveCount(0);
    await page.getByTestId(selectors.resourceAllocationsOnlyAllocated).click();
    await page.getByTestId(selectors.resourceAllocationsEmployeesMine).click();
    await expect(page.locator("tbody tr").filter({ hasText: "Alex Morgan" })).toHaveCount(0);
    await page.getByTestId(selectors.resourceAllocationsEmployeesAll).click();

    const editableCell = page.getByTestId("resource-allocation-cell-102-301");
    await editableCell.click();
    await editableCell.fill("75");
    await editableCell.blur();

    const saveRequest = page.waitForRequest(request =>
      request.method() === "PUT" && /\/api\/v1\/resource-allocations\/\d+$/.test(request.url()),
    );
    await page.getByTestId(selectors.resourceAllocationsSave).click();

    expect((await saveRequest).postDataJSON()).toEqual({
      changes: [{ employeeId: 102, projectId: 301, percent: 75 }],
    });
    await expect(editableCell).toHaveText("75");

    await page.getByTestId("resource-allocations-employee-ba").click();
    await page.getByRole("option", { name: "Alpine Operations" }).click();
    await page.keyboard.press("Escape");
    await expect(page.locator("tbody tr").filter({ hasText: "Jordan Lee" })).toBeVisible();
    await expect(page.locator("tbody tr").filter({ hasText: "Alex Morgan" })).toHaveCount(0);
  });

  test("keeps a large populated sheet responsive and shows a useful allocated-only empty state", async ({ page }) => {
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

    await page.goto(appPath(routes.resourceAllocations), { waitUntil: "domcontentloaded" });
    const cell = page.getByTestId("resource-allocation-cell-1000-2000");
    await expect(cell).toBeVisible();
    await expect(page.locator(".resource-allocation-input")).toHaveCount(0);

    const activationStartedAt = Date.now();
    await cell.click();
    await expect(page.locator(".resource-allocation-input")).toHaveCount(1);
    expect(Date.now() - activationStartedAt).toBeLessThan(2_000);
    const commitStartedAt = Date.now();
    await cell.fill("50");
    await cell.blur();
    await expect(page.locator(".resource-allocation-input")).toHaveCount(0);
    await expect(cell).toHaveText("50");
    expect(Date.now() - commitStartedAt).toBeLessThan(2_000);

    await cell.hover();
    await page.getByTestId("resource-allocation-clear-cell-1000-2000").click();
    await page.getByTestId("resource-allocations-employee-ba").click();
    const filterStartedAt = Date.now();
    await page.getByRole("option", { name: "Alpine Operations" }).click();
    await page.keyboard.press("Escape");
    await expect(page.getByText("Employee 2", { exact: true })).toHaveCount(0);
    expect(Date.now() - filterStartedAt).toBeLessThan(2_000);
    await page.getByTestId(selectors.resourceAllocationsProjectSearch).locator("input").fill("Missing project");
    await page.getByTestId(selectors.resourceAllocationsOnlyAllocated).click();
    await expect(page.getByTestId("resource-allocations-empty-allocated")).toBeVisible();
    await expect(page.getByTestId(selectors.resourceAllocationsTable)).toHaveCount(0);
  });
});
