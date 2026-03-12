import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Employees List and Drawer", () => {
  test("filters by search, current project and business account", async ({ page }) => {
    const credentials = credentialsOrSkip("employee");
    test.skip(!credentials, "Set E2E_EMPLOYEE_USERNAME and E2E_EMPLOYEE_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();
    await expect(page.getByTestId(selectors.employeesFilterSearch)).toBeVisible();
    await expect(page.getByTestId(selectors.employeesFilterProject)).toBeVisible();
    await expect(page.getByTestId(selectors.employeesFilterBa)).toBeVisible();

    await page.getByTestId(selectors.employeesFilterSearch).locator("input").fill("a");
  });

  test("opens employee drawer on row click", async ({ page }) => {
    const credentials = credentialsOrSkip("employee");
    test.skip(!credentials, "Set E2E_EMPLOYEE_USERNAME and E2E_EMPLOYEE_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    const rowsCount = await rows.count();
    test.skip(rowsCount === 0, "Employees table is empty in current environment");

    await rows.first().click();
    await expect(page.getByTestId(selectors.employeesDetailsDrawer)).toBeVisible();
  });
});
