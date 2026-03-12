import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Overtime Item Dialog", () => {
  test("opens employee overtime card from table row", async ({ page }) => {
    const credentials = credentialsOrSkip("overtime_admin") ?? credentialsOrSkip("employee");
    test.skip(!credentials, "Set E2E_OVERTIME_ADMIN_* or E2E_EMPLOYEE_* credentials");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.overtimes), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    const rowCount = await rows.count();
    test.skip(rowCount === 0, "Overtimes table is empty in current environment");

    const attempts = Math.min(rowCount, 3);
    for (let index = 0; index < attempts; index += 1) {
      await rows.nth(index).locator("td").first().click();
      if (await page.getByTestId(selectors.overtimesEmployeeDialog).isVisible().catch(() => false)) {
        break;
      }
    }
    await expect(page.getByTestId(selectors.overtimesEmployeeDialog)).toBeVisible();
  });
});
