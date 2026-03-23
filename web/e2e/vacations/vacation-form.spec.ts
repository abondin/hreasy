import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Vacation Form", () => {
  test("opens create vacation dialog when add is allowed", async ({ page }) => {
    const credentials = requireCredentials("employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.vacations));

    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();
    const addButton = page.getByTestId(selectors.toolbarAdd).first();
    await expect(addButton).toBeVisible();
    await addButton.click();
    await expect(page.locator(".v-dialog")).toBeVisible();
  });
});
