import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Vacation Form", () => {
  test("opens create vacation dialog when add is allowed", async ({ page }) => {
    const credentials = credentialsOrSkip("employee");
    test.skip(!credentials, "Set E2E_EMPLOYEE_USERNAME and E2E_EMPLOYEE_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.vacations));

    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();
    const addButton = page.getByTestId(selectors.toolbarAdd).first();
    const addCount = await addButton.count();
    if (addCount > 0) {
      await addButton.click();
      await expect(page.locator(".v-dialog")).toBeVisible();
    }
  });
});

