import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { openDialogFromRowsOrAddButton } from "../support/test-helpers";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Overtime Item Dialog", () => {
  test("opens employee overtime card from table row", async ({ page }) => {
    const credentials = requireCredentials("overtime_admin", "employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.overtimes), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    await openDialogFromRowsOrAddButton({
      rows,
      dialog: page.getByTestId(selectors.overtimesEmployeeDialog),
      emptyMessage: "Overtimes table is empty in current environment",
    });
    await expect(page.getByTestId(selectors.overtimesEmployeeDialog)).toBeVisible();
  });
});
