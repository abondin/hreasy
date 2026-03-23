import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { openDialogFromRowsOrAddButton } from "../support/test-helpers";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Admin Employees List and Edit", () => {
  test("filters employees in admin table", async ({ page }) => {
    const credentials = requireCredentials("admin_employees");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.adminEmployees), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.adminEmployeesListView)).toBeVisible();
    await page.getByTestId(selectors.adminEmployeesSearch).locator("input").fill("a");
    await expect(page.getByTestId(selectors.adminEmployeesSearch).locator("input")).toHaveValue("a");
  });

  test("opens employee edit dialog from row click", async ({ page }) => {
    const credentials = requireCredentials("admin_employees");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.adminEmployees), { waitUntil: "domcontentloaded" });

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    await openDialogFromRowsOrAddButton({
      rows,
      dialog: page.getByTestId(selectors.adminEmployeesEditDialog),
      addButton: page.getByTestId(selectors.adminEmployeesAdd),
      emptyMessage: "Admin employees table is empty in current environment",
    });
    await expect(page.getByTestId(selectors.adminEmployeesEditDialog)).toBeVisible();
  });
});
