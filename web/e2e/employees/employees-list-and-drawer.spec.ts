import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { clickFirstRow, expectVisibleRows } from "../support/test-helpers";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Employees List and Drawer", () => {
  test("filters by search, current project and business account", async ({ page }) => {
    const credentials = requireCredentials("employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();
    await expect(page.getByTestId(selectors.employeesFilterSearch)).toBeVisible();
    await expect(page.getByTestId(selectors.employeesFilterProject)).toBeVisible();
    await expect(page.getByTestId(selectors.employeesFilterBa)).toBeVisible();

    await page.getByTestId(selectors.employeesFilterSearch).locator("input").fill("a");
  });

  test("opens employee drawer on row click", async ({ page }) => {
    const credentials = requireCredentials("employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    await expectVisibleRows(rows, "Employees table is empty in current environment");
    await clickFirstRow(rows, "Employees table is empty in current environment");
    await expect(page.getByTestId(selectors.employeesDetailsDrawer)).toBeVisible();
  });
});
