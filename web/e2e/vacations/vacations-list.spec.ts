import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Vacations List", () => {
  test("filters vacations by year and search", async ({ page }) => {
    const credentials = credentialsOrSkip("employee");
    test.skip(!credentials, "Set E2E_EMPLOYEE_USERNAME and E2E_EMPLOYEE_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.vacations), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();
    await expect(page.getByTestId(selectors.vacationsFilterYear)).toBeVisible();
    await expect(page.getByTestId(selectors.vacationsFilterSearch)).toBeVisible();
    await page.getByTestId(selectors.vacationsFilterSearch).locator("input").fill("a");
  });

  test("switches between list, summary and timeline tabs", async ({ page }) => {
    const credentials = credentialsOrSkip("employee");
    test.skip(!credentials, "Set E2E_EMPLOYEE_USERNAME and E2E_EMPLOYEE_PASSWORD");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.vacations), { waitUntil: "domcontentloaded" });

    await page.getByTestId(selectors.vacationsTabSummary).click();
    await expect(page.getByTestId(selectors.vacationsTabSummary)).toHaveAttribute("aria-selected", "true");

    await page.getByTestId(selectors.vacationsTabTimeline).click();
    await expect(page.getByTestId(selectors.vacationsTabTimeline)).toHaveAttribute("aria-selected", "true");

    await page.getByTestId(selectors.vacationsTabList).click();
    await expect(page.getByTestId(selectors.vacationsTabList)).toHaveAttribute("aria-selected", "true");
  });
});
