import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Admin Employees Import", () => {
  test("opens employee import workflow screen", async ({ page }) => {
    const credentials = credentialsOrSkip("admin_employees");
    test.skip(!credentials, "Set E2E_ADMIN_EMPLOYEES_USERNAME and E2E_ADMIN_EMPLOYEES_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.adminEmployeesImport), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.adminEmployeesImportView)).toBeVisible();
    await expect(page.getByTestId(selectors.adminEmployeesImportView).getByText(/Excel/i)).toBeVisible();
  });
});
