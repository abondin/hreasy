import { expect, test } from "@playwright/test";
import {
  expectLoginPageOrAuthenticatedHome,
  expectProtectedRouteRedirectOrAccess,
  loginViaUi,
  requireCredentials,
} from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";

test.describe("Auth and Routing", () => {
  test("redirects unauthenticated user to login from root", async ({ page }) => {
    await page.goto(appPath("/"), { waitUntil: "domcontentloaded" });
    await expectLoginPageOrAuthenticatedHome(page);
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
      await page.goto(appPath(path), { waitUntil: "domcontentloaded" });
      await expectProtectedRouteRedirectOrAccess(page, path);
    }
  });

  test("logs in and opens profile (env credentials)", async ({ page }) => {
    const credentials = requireCredentials("employee");
    await loginViaUi(page, credentials);
    await expect(page).toHaveURL(/\/profile/);
  });
});
