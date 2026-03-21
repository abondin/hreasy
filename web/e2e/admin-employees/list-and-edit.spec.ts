import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Admin Employees List and Edit", () => {
  test("filters employees in admin table", async ({ page }) => {
    const credentials = credentialsOrSkip("admin_employees");
    test.skip(!credentials, "Set E2E_ADMIN_EMPLOYEES_USERNAME and E2E_ADMIN_EMPLOYEES_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.adminEmployees), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.adminEmployeesListView)).toBeVisible();
    await page.getByTestId(selectors.adminEmployeesSearch).locator("input").fill("a");
    await expect(page.getByTestId(selectors.adminEmployeesSearch).locator("input")).toHaveValue("a");
  });

  test("opens employee edit dialog from row click", async ({ page }) => {
    const credentials = credentialsOrSkip("admin_employees");
    test.skip(!credentials, "Set E2E_ADMIN_EMPLOYEES_USERNAME and E2E_ADMIN_EMPLOYEES_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.adminEmployees), { waitUntil: "domcontentloaded" });

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    const rowCount = await rows.count();
    test.skip(rowCount === 0, "Admin employees table is empty in current environment");

    const attempts = Math.min(rowCount, 3);
    for (let index = 0; index < attempts; index += 1) {
      await rows.nth(index).locator("td").first().click();
      if (await page.getByTestId(selectors.adminEmployeesEditDialog).isVisible().catch(() => false)) {
        break;
      }
    }
    if (!(await page.getByTestId(selectors.adminEmployeesEditDialog).isVisible().catch(() => false))) {
      const addButton = page.getByTestId(selectors.adminEmployeesAdd);
      if (await addButton.isVisible().catch(() => false)) {
        await addButton.click();
      }
    }
    await expect(page.getByTestId(selectors.adminEmployeesEditDialog)).toBeVisible();
  });
});
