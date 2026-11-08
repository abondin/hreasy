import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Overtimes Summary", () => {
  test("changes the displayed period when navigating forward", async ({ page }) => {
    const credentials = requireCredentials("overtime_admin", "employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.overtimes), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();
    const periodLabel = page.getByTestId(selectors.overtimesPeriodLabel).locator("input").first();
    await expect(periodLabel).toBeVisible();
    const before = await periodLabel.inputValue();
    await page.getByTestId(selectors.overtimesNextPeriod).click();
    await expect(periodLabel).not.toHaveValue(before);
  });

});
