import { expect, test } from "@playwright/test";
import {
  appMockedAuthorities,
  installUnhandledApiGuard,
  mockAppRouteAuth,
  mockVacationsManagementApi,
} from "../support/app-mocked-api";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import {
  expectDocumentNotToScroll,
  expectTableToScroll,
} from "../support/test-helpers";

test.describe("App Mocked Vacations Page", () => {
  test("renders list tab in real app shell and switches between vacation tabs without backend", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.vacations);
    await mockVacationsManagementApi(page);

    await page.goto(appPath(routes.vacations), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();
    await expect(page.getByTestId(selectors.vacationsFilterYear)).toBeVisible();
    await expect(page.getByTestId(selectors.vacationsFilterSearch)).toBeVisible();

    const listTable = page.getByTestId(selectors.vacationsListTable);
    await expect(listTable).toBeVisible();
    await expectTableToScroll(listTable);
    await expectDocumentNotToScroll(page);

    await page.getByTestId(selectors.vacationsTabSummary).click();
    await expect(page.getByTestId(selectors.vacationsSummaryTable)).toBeVisible();

    await page.getByTestId(selectors.vacationsTabTimeline).click();
    await expect(page.getByTestId(selectors.vacationsTabTimeline)).toHaveAttribute("aria-selected", "true");

    await page.getByTestId(selectors.vacationsTabList).click();
    await expect(listTable).toBeVisible();
  });
});
