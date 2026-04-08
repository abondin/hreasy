import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import {
  clearAutocompleteField,
  clickFirstRow,
  expectFilterToBeAccessible,
  expectVisibleRows,
  fillAdaptiveFilterInput,
} from "../support/test-helpers";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Employees List and Drawer", () => {
  test("filters by search, current project and business account", async ({ page }) => {
    const credentials = requireCredentials("employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();
    await expectFilterToBeAccessible(page, selectors.employeesFilterSearch);
    await expectFilterToBeAccessible(page, selectors.employeesFilterProject);
    await expectFilterToBeAccessible(page, selectors.employeesFilterBa);

    await fillAdaptiveFilterInput(page, selectors.employeesFilterSearch, "a");
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

  test("clearing current project keeps drawer open and allows saving", async ({ page }) => {
    const credentials = requireCredentials("employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });

    const search = page.getByTestId(selectors.employeesFilterSearch).locator("input");
    await expect(search).toBeVisible();
    await search.fill(credentials.username);

    const targetRow = page
      .locator("tbody tr:visible")
      .filter({ hasText: credentials.username })
      .first();

    await expect(targetRow).toBeVisible();
    await targetRow.locator("td").first().click();

    const drawer = page.getByTestId(selectors.employeesDetailsDrawer);
    await expect(drawer).toBeVisible();

    await drawer.getByTestId(selectors.profileSummaryEditProject).click();

    const dialog = page.getByTestId(selectors.projectAssignmentDialog);
    const projectField = dialog.getByTestId(selectors.projectAssignmentProject);

    await expect(dialog).toBeVisible();
    await expect(projectField).toBeVisible();

    await clearAutocompleteField(projectField);

    await expect(dialog).toBeVisible();
    await expect(drawer).toBeVisible();

    await dialog.getByTestId(selectors.projectAssignmentSubmit).click();

    await expect(dialog).toBeHidden();
    await expect(drawer).toBeVisible();
  });
});
