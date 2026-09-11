import { expect, test } from "@playwright/test";
import {
  appMockedAuthorities,
  installUnhandledApiGuard,
  mockAppRouteAuth,
  mockCurrentOrFutureVacationsApi,
  mockEmployeeDetailsApi,
  mockEmployeesDirectoryApi,
  mockOvertimesSummaryApi,
} from "../support/app-mocked-api";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

test.describe("App Mocked Employees Drawer", () => {
  test("closes the drawer normally and allows menu navigation with a filtered employee selected", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 1000 });
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, [
      ...appMockedAuthorities.employees,
      ...appMockedAuthorities.overtimes,
    ]);
    await mockOvertimesSummaryApi(page);
    await mockEmployeeDetailsApi(page);
    await mockCurrentOrFutureVacationsApi(page);
    await page.goto(appPath(routes.employees));

    const search = page.getByTestId(selectors.employeesFilterSearch).locator("input");
    await search.fill("Alex");
    const row = page.locator("tbody tr:visible").filter({ hasText: "Alex Morgan" }).first();
    const drawer = page.getByTestId(selectors.employeesDetailsDrawer);
    await row.locator("td").first().click();
    await expect(drawer).toHaveClass(/v-navigation-drawer--active/);
    await expect(page).toHaveURL(/\/employees\?employeeId=101$/);

    await page.keyboard.press("Escape");
    await expect(page).toHaveURL(/\/employees$/);
    await expect(drawer).not.toHaveClass(/v-navigation-drawer--active/);
    await expect(search).toHaveValue("Alex");

    await row.locator("td").first().click();
    await expect(drawer).toHaveClass(/v-navigation-drawer--active/);
    await page.getByRole("button", { name: "Открыть меню", exact: true }).click();
    await page.locator(`a[href="${appPath(routes.overtimes)}"]`).click();
    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();
    await expect(page).toHaveURL(/\/overtimes$/);
  });

  test("opens employee drawer from real employees page without backend", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, [
      ...appMockedAuthorities.employees,
      "view_employee_full",
    ]);
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
    await expect(drawer.getByTestId("employee-details-birthday")).toContainText("14.09");
    await expect(drawer).toContainText("02.03.2026");
  });

  test("hides birthday without full employee data permission", async ({ page }) => {
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
    await expect(drawer.getByTestId("employee-details-birthday")).toHaveCount(0);
    await expect(drawer).not.toContainText("14.09");
  });
});
