import { expect, test } from "@playwright/test";
import { mockAuthenticatedUser } from "../support/auth-mocks";
import { appPath } from "../support/navigation";
import { mockSalaryRequestsDataset } from "../support/salary-mocks";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import {
  expectTableToScroll,
  expectWidthsToStayStable,
  fillAdaptiveFilterInput,
  readHeaderCellWidths,
  readTableScrollMetrics,
} from "../support/test-helpers";

test.describe("Salary Requests Table Scroll", () => {
  test("keeps internal table scroll and stable widths on small screens", async ({ page }) => {
    await mockAuthenticatedUser(page, {
      authorities: ["admin_salary_request", "report_salary_request"],
    });
    await mockSalaryRequestsDataset(page, { requestCount: 80, bonusCount: 60 });

    await page.setViewportSize({ width: 1024, height: 600 });
    await page.goto(appPath(routes.salaryRequests), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.salaryRequestsView)).toBeVisible();
    const table = page.getByTestId(selectors.salaryRequestsTable);
    await expect(table).toBeVisible();

    const widthsBeforeInteractions = await readHeaderCellWidths(table);
    await expectTableToScroll(table);

    await table.locator("thead th").first().click();
    const widthsAfterSort = await readHeaderCellWidths(table);
    expectWidthsToStayStable(widthsBeforeInteractions, widthsAfterSort);

    await fillAdaptiveFilterInput(page, selectors.salaryRequestsFilterSearch, "Employee 1");

    const widthsAfterFilter = await readHeaderCellWidths(table);
    expectWidthsToStayStable(widthsAfterSort, widthsAfterFilter);
    await expectTableToScroll(table);

    await page
      .getByTestId(selectors.salaryRequestsTypeToggle)
      .getByRole("button", { name: /Бонусы/ })
      .click();
    await expectTableToScroll(table);

    const bonusMetrics = await readTableScrollMetrics(table);
    expect(bonusMetrics.overflowY).toMatch(/auto|scroll/);

    await page.setViewportSize({ width: 1280, height: 720 });
    await expectTableToScroll(table);
  });
});
