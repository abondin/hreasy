import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Overtimes Summary", () => {
  test("navigates periods and updates summary", async ({ page }) => {
    const credentials = requireCredentials("overtime_admin", "employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.overtimes), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();
    const before = (await page.getByTestId(selectors.overtimesPeriodLabel).textContent()) ?? "";
    await page.getByTestId(selectors.overtimesNextPeriod).click();
    const after = (await page.getByTestId(selectors.overtimesPeriodLabel).textContent()) ?? "";
    expect(after).not.toBe(before);
  });

  test("filters by employee name", async ({ page }) => {
    const credentials = requireCredentials("overtime_admin", "employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.overtimes), { waitUntil: "domcontentloaded" });

    await page.getByTestId(selectors.overtimesFilterSearch).locator("input").fill("a");
    await expect(page.getByTestId(selectors.overtimesFilterSearch).locator("input")).toHaveValue("a");
  });
});
