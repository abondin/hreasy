import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import { readDocumentScrollMetrics, readTableScrollMetrics } from "../support/test-helpers";

test.describe("Employees Profile Return Scroll Diagnostic", () => {
  test("measures browser and table scroll before and after returning from profile", async ({ page }) => {
    const credentials = requireCredentials("employee");

    await page.setViewportSize({ width: 1280, height: 720 });
    await loginViaUi(page, credentials);

    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();

    const table = page.getByTestId(selectors.employeesTable);
    await expect(
      table.locator("tbody tr").filter({ hasText: /@/ }).first(),
    ).toBeVisible({ timeout: 15000 });

    console.log("EMPLOYEES_INITIAL_DOCUMENT", JSON.stringify(await readDocumentScrollMetrics(page)));
    console.log("EMPLOYEES_INITIAL_TABLE", JSON.stringify(await readTableScrollMetrics(table)));

    await page.goto(appPath(routes.profile), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.profileView)).toBeVisible();
    console.log("PROFILE_DOCUMENT", JSON.stringify(await readDocumentScrollMetrics(page)));

    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();
    await expect(
      table.locator("tbody tr").filter({ hasText: /@/ }).first(),
    ).toBeVisible({ timeout: 15000 });

    console.log("EMPLOYEES_RETURN_DOCUMENT", JSON.stringify(await readDocumentScrollMetrics(page)));
    console.log("EMPLOYEES_RETURN_TABLE", JSON.stringify(await readTableScrollMetrics(table)));
  });
});
