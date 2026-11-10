import { expect, test } from "@playwright/test";
import { loginViaApi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Overtime Admin Actions", () => {
  test("shows period admin toggle for overtime admin", async ({ page }) => {
    const credentials = requireCredentials("overtime_admin");
    await loginViaApi(page, credentials);
    await page.goto(appPath(routes.overtimes));

    await expect(page.getByTestId(selectors.overtimesPeriodToggle)).toBeVisible();
  });
});
