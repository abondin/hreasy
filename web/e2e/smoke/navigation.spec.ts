import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Navigation Smoke", () => {
  test("opens profile, employees, vacations, overtimes, mentorship for authenticated user", async ({ page }) => {
    const credentials = credentialsOrSkip("employee");
    test.skip(!credentials, "Set E2E_EMPLOYEE_USERNAME and E2E_EMPLOYEE_PASSWORD");

    await loginViaUi(page, credentials!);

    await page.goto(appPath(routes.profile), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.profileView)).toBeVisible();

    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();

    await page.goto(appPath(routes.vacations), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();

    await page.goto(appPath(routes.overtimes), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();

    await page.goto(appPath(routes.mentorship), { waitUntil: "domcontentloaded" });
    await expect(page).toHaveURL(/\/(juniors|profile)/);
  });

  test("opens admin employees tabs for admin user", async ({ page }) => {
    const credentials = credentialsOrSkip("admin_employees");
    test.skip(!credentials, "Set E2E_ADMIN_EMPLOYEES_USERNAME and E2E_ADMIN_EMPLOYEES_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.adminEmployees), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.adminTabsView)).toBeVisible();
    await expect(page.getByTestId(selectors.adminTabList)).toBeVisible();
    await expect(page.getByTestId(selectors.adminTabImport)).toBeVisible();
    await expect(page.getByTestId(selectors.adminTabKids)).toBeVisible();
    await expect(page.getByTestId(selectors.adminTabKidsImport)).toBeVisible();
  });
});
