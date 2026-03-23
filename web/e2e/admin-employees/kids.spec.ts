import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { openDialogFromRowsOrAddButton } from "../support/test-helpers";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Admin Employee Kids", () => {
  test("filters kids table", async ({ page }) => {
    const credentials = requireCredentials("admin_employees");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.adminEmployeesKids), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.adminKidsView)).toBeVisible();
    await page.getByTestId(selectors.adminKidsSearch).locator("input").fill("a");
    await expect(page.getByTestId(selectors.adminKidsSearch).locator("input")).toHaveValue("a");
  });

  test("opens kid edit dialog from row click", async ({ page }) => {
    const credentials = requireCredentials("admin_employees");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.adminEmployeesKids), { waitUntil: "domcontentloaded" });

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    await openDialogFromRowsOrAddButton({
      rows,
      dialog: page.getByTestId(selectors.adminKidsEditDialog),
      addButton: page.getByTestId(selectors.adminKidsAdd),
      emptyMessage: "Admin kids table is empty in current environment",
    });
    await expect(page.getByTestId(selectors.adminKidsEditDialog)).toBeVisible();
  });
});
