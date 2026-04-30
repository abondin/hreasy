import { expect, test, type Page } from "@playwright/test";
import {
  appMockedEmployees,
  appMockedAuthorities,
  installUnhandledApiGuard,
  mockAppRouteAuth,
  mockEmployeesDirectoryApi,
  mockVacationsManagementApi,
} from "../support/app-mocked-api";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import {
  expectDocumentNotToScroll,
  expectFilterToBeAccessible,
  expectTableToScroll,
} from "../support/test-helpers";

async function openNavigationDrawer(page: Page): Promise<void> {
  const navButton = page.getByRole("button").filter({ has: page.locator(".mdi-menu") }).first();
  await expect(navButton).toBeVisible();
  await navButton.click();
}

async function navigateViaDrawer(page: Page, href: string): Promise<void> {
  await openNavigationDrawer(page);
  const targetLink = page.locator(`a[href="${href}"]`).first();
  await expect(targetLink).toBeVisible();
  await targetLink.click();
}

test.describe("App Mocked Employees Page", () => {
  test("renders real app shell and keeps table scroll isolated from browser scroll", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.employees);
    await mockEmployeesDirectoryApi(page);

    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();
    await expect(page.getByTestId(selectors.appTitle)).toBeVisible();
    await expectFilterToBeAccessible(page, selectors.employeesFilterSearch);
    await expectFilterToBeAccessible(page, selectors.employeesFilterProject);
    await expectFilterToBeAccessible(page, selectors.employeesFilterBa);

    const table = page.getByTestId(selectors.employeesTable);
    await expect(table).toBeVisible();
    await expectTableToScroll(table);
    await expectDocumentNotToScroll(page);

    const search = page.getByTestId(selectors.employeesFilterSearch).locator("input").first();
    await search.fill("Jordan");
    await expect(search).toHaveValue("Jordan");
    await expect(page.locator("tbody tr:visible")).toContainText(["Jordan Lee"]);
  });

  test("reloads employee list after returning from another keep-alive page without resetting filters", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, [
      ...appMockedAuthorities.employees,
      ...appMockedAuthorities.vacations,
    ]);
    await mockVacationsManagementApi(page);

    let useRefreshedEmployees = false;
    let employeeListRequests = 0;
    await page.route("**/api/v1/employee", async (route) => {
      employeeListRequests += 1;
      const employees = appMockedEmployees.map((employee) =>
        employee.id === 101 && useRefreshedEmployees
          ? { ...employee, displayName: "Alex Rivera" }
          : employee,
      );
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify(employees),
      });
    });

    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();
    await expect(page.locator("tbody tr:visible")).toContainText(["Alex Morgan"]);

    const search = page.getByTestId(selectors.employeesFilterSearch).locator("input").first();
    await search.fill("Alex");
    await expect(search).toHaveValue("Alex");

    await navigateViaDrawer(page, routes.vacations);
    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();

    useRefreshedEmployees = true;
    const requestsBeforeReturn = employeeListRequests;
    await navigateViaDrawer(page, routes.employees);
    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();

    await expect
      .poll(() => employeeListRequests, {
        timeout: 3000,
        message: "Expected employees list to be requested after keep-alive return",
      })
      .toBeGreaterThan(requestsBeforeReturn);
    await expect(search).toHaveValue("Alex");
    await expect(page.locator("tbody tr:visible")).toContainText(["Alex Rivera"]);
  });
});
