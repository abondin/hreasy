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
      currentProjectId: 301,
      currentProjectName: "Retail Terminal Platform",
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
      baId: 401,
      baName: "Northwind Delivery",
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

async function mockResourceAllocationsApi(page: Page): Promise<void> {
  await page.route(/\/api\/v1\/resource-allocations\/\d+$/, async (route) => {
    if (route.request().method() === "PUT") {
      const body = route.request().postDataJSON() as {
        changes: Array<{ employeeId: number; projectId: number; percent: number }>;
      };
      for (const change of body.changes) {
        const current = sheet.allocations.find(value =>
          value.employeeId === change.employeeId && value.projectId === change.projectId,
        );
        if (current) {
          current.percent = change.percent;
        }
      }
      await json(route, 7);
      return;
    }
    await json(route, sheet);
  });
}

test.describe("App Mocked Resource Allocations Page", () => {
  test("edits accessible projects and saves only changed cells without backend", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.resourceAllocations);
    await mockResourceAllocationsApi(page);

    await page.goto(appPath(routes.resourceAllocations), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.resourceAllocationsView)).toBeVisible();
    await expect(page.getByTestId(selectors.resourceAllocationsTable)).toBeVisible();
    await expect(page.getByTestId("resource-allocation-cell-101-302")).toHaveCount(0);
    await expect(page.locator("tbody tr").filter({ hasText: "Alex Morgan" })).toContainText("100%");

    await page.getByTestId(selectors.resourceAllocationsScopeAll).click();
    await expect(page.getByTestId("resource-allocation-cell-101-302")).toBeDisabled();

    const editableCell = page.getByTestId("resource-allocation-cell-102-301");
    await editableCell.fill("75");
    await editableCell.blur();

    const saveRequest = page.waitForRequest(request =>
      request.method() === "PUT" && /\/api\/v1\/resource-allocations\/\d+$/.test(request.url()),
    );
    await page.getByTestId(selectors.resourceAllocationsSave).click();

    expect((await saveRequest).postDataJSON()).toEqual({
      changes: [{ employeeId: 102, projectId: 301, percent: 75 }],
    });
    await expect(editableCell).toHaveValue("75");
  });
});
