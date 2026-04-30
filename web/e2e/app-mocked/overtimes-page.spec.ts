import { expect, test } from "@playwright/test";
import {
  appMockedAuthorities,
  installUnhandledApiGuard,
  mockAppRouteAuth,
  mockOvertimesSummaryApi,
} from "../support/app-mocked-api";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import {
  expectDocumentNotToScroll,
  expectTableToScroll,
} from "../support/test-helpers";

test.describe("App Mocked Overtimes Page", () => {
  test("renders real page layout and updates summary when period changes", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.overtimes);
    await mockOvertimesSummaryApi(page);

    await page.goto(appPath(routes.overtimes), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();
    const periodLabel = page.getByTestId(selectors.overtimesPeriodLabel).locator("input").first();
    await expect(periodLabel).toBeVisible();

    const before = await periodLabel.inputValue();
    const table = page.getByTestId(selectors.overtimesTable);
    await expect(table).toBeVisible();
    await expectTableToScroll(table);
    await expectDocumentNotToScroll(page);

    await page.getByTestId(selectors.overtimesFilterSearch).locator("input").fill("Alex");
    await expect(page.locator("tbody tr:visible")).toContainText(["Alex Morgan"]);

    await page.getByTestId(selectors.overtimesNextPeriod).click();
    await expect(periodLabel).not.toHaveValue(before);
  });
});
