import { expect, test, type Locator, type Page } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import {
  expectWidthsToStayStable,
  fillAdaptiveFilterInput,
  readHeaderCellWidths,
  readTableScrollMetrics,
} from "../support/test-helpers";

async function exerciseTableLayout(
  page: Page,
  table: Locator,
  searchFilterTestId?: string,
  options?: {
    assertWidthStability?: boolean;
  },
): Promise<void> {
  await expect(table).toBeVisible();
  await page.setViewportSize({ width: 1024, height: 600 });

  const metricsAfterResize = await readTableScrollMetrics(table);
  expect(metricsAfterResize.overflowY).toMatch(/auto|scroll|hidden/);
  const widthsBeforeInteractions = await readHeaderCellWidths(table);

  if (searchFilterTestId) {
    await fillAdaptiveFilterInput(page, searchFilterTestId, "a");
  }

  await table.locator("thead th").first().click();
  const widthsAfterInteractions = await readHeaderCellWidths(table);

  expect(widthsAfterInteractions.length).toBe(widthsBeforeInteractions.length);
  widthsAfterInteractions.forEach((width, index) => {
    expect(width, `Expected header width at index ${index} to remain positive`).toBeGreaterThan(32);
  });

  if (options?.assertWidthStability !== false) {
    expectWidthsToStayStable(widthsBeforeInteractions, widthsAfterInteractions, 8);
  }
}

test.describe("Tables Layout Regression", () => {
  test("employees table survives resize, search and sort without width jumps", async ({ page }) => {
    const credentials = requireCredentials("employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();
    await exerciseTableLayout(
      page,
      page.getByTestId(selectors.employeesTable),
      selectors.employeesFilterSearch,
      { assertWidthStability: true },
    );
  });

  test("overtimes table survives resize and filtering", async ({ page }) => {
    const credentials = requireCredentials("overtime_admin", "employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.overtimes), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();
    await exerciseTableLayout(
      page,
      page.getByTestId(selectors.overtimesTable),
      selectors.overtimesFilterSearch,
      { assertWidthStability: false },
    );
  });

  test("vacations list and summary tables survive tab switches and resize", async ({ page }) => {
    const credentials = requireCredentials("employee");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.vacations), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();
    await exerciseTableLayout(
      page,
      page.getByTestId(selectors.vacationsListTable),
      selectors.vacationsFilterSearch,
      { assertWidthStability: false },
    );

    await page.getByTestId(selectors.vacationsTabSummary).click();
    await expect(page.getByTestId(selectors.vacationsTabSummary)).toHaveAttribute("aria-selected", "true");
    await exerciseTableLayout(page, page.getByTestId(selectors.vacationsSummaryTable), undefined, {
      assertWidthStability: false,
    });
  });
});
