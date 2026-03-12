import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Profile Summary", () => {
  test("renders base employee profile information", async ({ page }) => {
    const credentials = credentialsOrSkip("employee");
    test.skip(!credentials, "Set E2E_EMPLOYEE_USERNAME and E2E_EMPLOYEE_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.profile), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.profileView)).toBeVisible();
    await expect(page.getByTestId(selectors.profileView).getByTestId(selectors.logoutButton).first()).toHaveCount(0);
  });

  test("edits telegram and persists changes", async ({ page }) => {
    const credentials = credentialsOrSkip("employee");
    test.skip(!credentials, "Set E2E_EMPLOYEE_USERNAME and E2E_EMPLOYEE_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.profile), { waitUntil: "domcontentloaded" });

    const openDialog = page.getByTestId("open-telegram-dialog").first();
    await expect(openDialog).toBeVisible();
    await openDialog.click();
    await expect(page.getByTestId("telegram-input")).toBeVisible();
    await expect(page.getByTestId("telegram-cancel")).toBeVisible();
  });
});
