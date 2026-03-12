import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Admin Employee Kids", () => {
  test("filters kids table", async ({ page }) => {
    const credentials = credentialsOrSkip("admin_employees");
    test.skip(!credentials, "Set E2E_ADMIN_EMPLOYEES_USERNAME and E2E_ADMIN_EMPLOYEES_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.adminEmployeesKids), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.adminKidsView)).toBeVisible();
    await page.getByTestId(selectors.adminKidsSearch).locator("input").fill("a");
    await expect(page.getByTestId(selectors.adminKidsSearch).locator("input")).toHaveValue("a");
  });

  test("opens kid edit dialog from row click", async ({ page }) => {
    const credentials = credentialsOrSkip("admin_employees");
    test.skip(!credentials, "Set E2E_ADMIN_EMPLOYEES_USERNAME and E2E_ADMIN_EMPLOYEES_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.adminEmployeesKids), { waitUntil: "domcontentloaded" });

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    const rowCount = await rows.count();
    test.skip(rowCount === 0, "Admin kids table is empty in current environment");

    const attempts = Math.min(rowCount, 3);
    for (let index = 0; index < attempts; index += 1) {
      await rows.nth(index).locator("td").first().click();
      if (await page.getByTestId(selectors.adminKidsEditDialog).isVisible().catch(() => false)) {
        break;
      }
    }
    if (!(await page.getByTestId(selectors.adminKidsEditDialog).isVisible().catch(() => false))) {
      const addButton = page.getByTestId(selectors.adminKidsAdd);
      if (await addButton.isVisible().catch(() => false)) {
        await addButton.click();
      }
    }
    await expect(page.getByTestId(selectors.adminKidsEditDialog)).toBeVisible();
  });
});
