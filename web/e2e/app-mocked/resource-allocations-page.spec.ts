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
    const firstEmployeeCell = page.getByTestId("resource-allocation-employee-101");
    const firstRow = page.getByRole("row").filter({ has: firstEmployeeCell });
    await expect(firstRow).toContainText("100%");
    const firstProjectHeader = page.getByRole("columnheader", { name: "Retail Terminal Platform" });
    const projectXBeforeRowAction = await firstProjectHeader.evaluate(element => element.getBoundingClientRect().x);
    await firstRow.hover();
    await expect(page.getByTestId("resource-allocation-clear-row-101")).toBeVisible();
    await expect.poll(async () => firstProjectHeader.evaluate(element => element.getBoundingClientRect().x))
      .toBe(projectXBeforeRowAction);
    await expect.poll(async () => {
      const total = await page.getByTestId("resource-allocation-total-101").boundingBox();
      const project = await page.getByTestId("resource-allocation-cell-101-301").boundingBox();
      return total && project ? Math.round(project.x - total.x - total.width) : -1;
    }).toBeGreaterThanOrEqual(0);

    const populatedCell = page.getByTestId("resource-allocation-cell-101-301");
    await populatedCell.click();
    await page.keyboard.press("Delete");
    await expect(populatedCell).toHaveText("");
    const projectWidthBeforeEdit = await firstProjectHeader.evaluate(element => element.getBoundingClientRect().width);
    const cellWidthBeforeEdit = await populatedCell.evaluate(element => element.getBoundingClientRect().width);
    await populatedCell.dblclick();
    const editor = page.locator("revogr-edit input");
    await expect(editor).toBeVisible();
    await expect.poll(async () => populatedCell.evaluate(element => element.getBoundingClientRect().width))
      .toBe(cellWidthBeforeEdit);
    await expect.poll(async () => firstProjectHeader.evaluate(element => element.getBoundingClientRect().width))
      .toBe(projectWidthBeforeEdit);
    await editor.fill("60");
    await page.keyboard.press("Tab");
    await expect.poll(async () => populatedCell.evaluate(element => element.getBoundingClientRect().width))
      .toBe(cellWidthBeforeEdit);
    await expect.poll(async () => firstProjectHeader.evaluate(element => element.getBoundingClientRect().width))
      .toBe(projectWidthBeforeEdit);
    const allocationGrid = page.getByTestId(selectors.resourceAllocationsTable);
    await expect.poll(() => allocationGrid.evaluate(async (element) => {
      const focused = await (element as HTMLRevoGridElement).getFocused();
      return { employeeId: focused?.model.id, project: focused?.column?.prop };
    })).toEqual({ employeeId: 101, project: "project_303" });
    await page.keyboard.press("Enter");
    await expect(editor).toBeVisible();
    await page.keyboard.press("Enter");
    await expect.poll(() => allocationGrid.evaluate(async (element) => {
      const focused = await (element as HTMLRevoGridElement).getFocused();
      return { employeeId: focused?.model.id, project: focused?.column?.prop };
    })).toEqual({ employeeId: 102, project: "project_303" });

    await page.getByTestId(selectors.resourceAllocationsScopeAll).click();
    await projectSearch.fill("Additional Project 8");
    await expect(page.getByRole("columnheader", { name: "Additional Project 8" })).toHaveCount(0);
    await projectSearch.fill("Billing");
    await expect(page.getByTestId("resource-allocation-cell-101-302"))
      .toHaveClass(/resource-allocation-project-grid-cell--readonly/);
    await page.getByTestId(selectors.resourceAllocationsOnlyAllocated).click();
    await expect(page.getByTestId("resource-allocation-cell-101-302")).toBeVisible();
    await expect(page.getByTestId("resource-allocation-cell-102-302")).toHaveCount(0);
    await page.getByTestId(selectors.resourceAllocationsOnlyAllocated).click();
    await projectSearch.fill("");
    await page.getByTestId(selectors.resourceAllocationsEmployeesMine).click();
    await expect(page.getByTestId("resource-allocation-employee-101")).toHaveCount(0);
    await page.getByTestId(selectors.resourceAllocationsEmployeesAll).click();

    const editableCell = page.getByTestId("resource-allocation-cell-102-301");
    await editableCell.dblclick();
    await editor.fill("75");
    await editor.press("Enter");

    await page.getByRole("button", { name: "Обновить данные" }).click();
    await expect(page.getByTestId("resource-allocations-discard-dialog")).toBeVisible();
    await page.getByTestId("resource-allocations-discard-cancel").click();
    await expect(page.getByTestId("resource-allocations-discard-dialog")).toBeHidden();

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
    await expect(page.getByTestId("resource-allocation-employee-102")).toBeVisible();
    await expect(page.getByTestId("resource-allocation-employee-101")).toHaveCount(0);
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
    const editor = page.locator("revogr-edit input");
    await expect(editor).toHaveCount(0);

    const activationStartedAt = Date.now();
    await cell.dblclick();
    await expect(editor).toHaveCount(1);
    expect(Date.now() - activationStartedAt).toBeLessThan(2_000);
    const commitStartedAt = Date.now();
    await editor.fill("50");
    await editor.press("Enter");
    await expect(editor).toHaveCount(0);
    await expect(cell).toHaveText("50");
    expect(Date.now() - commitStartedAt).toBeLessThan(2_000);

    const fillTarget = page.getByTestId("resource-allocation-cell-1001-2000");
    await cell.click();
    const fillHandle = page.locator(".autofill-handle");
    await fillHandle.hover();
    await expect.poll(() => page.getByTestId(selectors.resourceAllocationsTable)
      .evaluate(async element => (await (element as HTMLRevoGridElement).getSelectedRange())?.x1)).toBe(0);
    const handleBox = await fillHandle.boundingBox();
    const targetBox = await fillTarget.boundingBox();
    expect(handleBox).not.toBeNull();
    expect(targetBox).not.toBeNull();
    await page.mouse.move(handleBox!.x + handleBox!.width / 2, handleBox!.y + handleBox!.height / 2);
    await page.mouse.down();
    await page.mouse.move(targetBox!.x + targetBox!.width / 2, targetBox!.y + targetBox!.height / 2, { steps: 10 });
    await expect(page.locator(".temp-bg-range")).toBeVisible();
    await page.mouse.up();
    await expect(fillTarget).toHaveText("50");

    await cell.click();
    await page.keyboard.press("Delete");
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
