import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { selectors } from "../support/selectors";

test.describe("Post Login Smoke", () => {
  test("opens profile after successful login", async ({ page }) => {
    const credentials = requireCredentials("employee");
    await loginViaUi(page, credentials);

    await expect(page).toHaveURL(/\/profile/);
    await expect(page.getByTestId(selectors.logoutButton).first()).toBeVisible();
    await expect(page.getByTestId(selectors.appTitle)).toBeVisible();
    await expect(page.getByTestId(selectors.loginSubmit)).toHaveCount(0);
  });
});
