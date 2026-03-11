import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";

test.describe("Auth and Routing", () => {
  test("redirects unauthenticated user to login from root", async ({ page }) => {
    await page.goto(appPath("/"));
    await expect(page).toHaveURL(/\/login(?:\?returnPath=\/profile)?$/);
    await expect(page.getByTestId("login-input")).toBeVisible();
    await expect(page.getByTestId("password-input")).toBeVisible();
    await expect(page.getByTestId("login-submit")).toBeVisible();
  });

  test("redirects unauthenticated user to login from protected routes", async ({ page }) => {
    for (const path of [
      routes.profile,
      routes.employees,
      routes.vacations,
      routes.overtimes,
      routes.mentorship,
      routes.adminEmployees,
    ]) {
      await page.goto(appPath(path));
      await expect(page).toHaveURL(/\/login/);
    }
  });

  test("logs in and opens profile (env credentials)", async ({ page }) => {
    const credentials = credentialsOrSkip("employee");
    test.skip(!credentials, "Set E2E_EMPLOYEE_USERNAME and E2E_EMPLOYEE_PASSWORD");

    await loginViaUi(page, credentials!);
    await expect(page).toHaveURL(/\/profile/);
  });
});
