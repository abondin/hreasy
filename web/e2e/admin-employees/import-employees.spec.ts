import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Admin Employees Import", () => {
  test("opens employee import workflow screen", async ({ page }) => {
    const credentials = requireCredentials("admin_employees");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.adminEmployeesImport), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.adminEmployeesImportView)).toBeVisible();
    await expect(page.getByTestId(selectors.adminEmployeesImportView).getByText(/Excel/i)).toBeVisible();
  });
});
