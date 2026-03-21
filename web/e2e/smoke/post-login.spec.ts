import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { selectors } from "../support/selectors";

test.describe("Post Login Smoke", () => {
  test("opens profile after successful login", async ({ page }) => {
    const credentials = credentialsOrSkip("employee");
    test.skip(!credentials, "Set E2E_EMPLOYEE_USERNAME and E2E_EMPLOYEE_PASSWORD");

    await loginViaUi(page, credentials!);

    await expect(page).toHaveURL(/\/profile/);
    await expect(page.getByTestId(selectors.logoutButton).first()).toBeVisible();
    await expect(page.getByTestId(selectors.appTitle)).toBeVisible();
    await expect(page.getByTestId(selectors.loginSubmit)).toHaveCount(0);
  });
});

