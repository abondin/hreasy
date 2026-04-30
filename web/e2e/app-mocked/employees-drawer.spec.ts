import { expect, test } from "@playwright/test";
import {
  appMockedAuthorities,
  installUnhandledApiGuard,
  mockAppRouteAuth,
  mockCurrentOrFutureVacationsApi,
  mockEmployeeDetailsApi,
  mockEmployeesDirectoryApi,
} from "../support/app-mocked-api";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

test.describe("App Mocked Employees Drawer", () => {
  test("opens employee drawer from real employees page without backend", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.employees);
    await mockEmployeesDirectoryApi(page);
    await mockEmployeeDetailsApi(page);
    await mockCurrentOrFutureVacationsApi(page);

    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();

    const targetRow = page.locator("tbody tr:visible").filter({ hasText: "Alex Morgan" }).first();
    await expect(targetRow).toBeVisible();
    await targetRow.locator("td").first().click();

    const drawer = page.getByTestId(selectors.employeesDetailsDrawer);
    await expect(drawer).toBeVisible();
    await expect(drawer).toContainText("Alex Morgan");
    await expect(drawer).toContainText("02.03.2026");
  });
});
