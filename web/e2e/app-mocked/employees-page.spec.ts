import { expect, test } from "@playwright/test";
import {
  appMockedAuthorities,
  installUnhandledApiGuard,
  mockAppRouteAuth,
  mockEmployeesDirectoryApi,
} from "../support/app-mocked-api";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import {
  expectDocumentNotToScroll,
  expectFilterToBeAccessible,
  expectTableToScroll,
} from "../support/test-helpers";

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
});
