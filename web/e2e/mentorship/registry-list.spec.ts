import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { clickFirstRow, expectVisibleRows } from "../support/test-helpers";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Mentorship Registry List", () => {
  test("filters registry by search", async ({ page }) => {
    const credentials = requireCredentials("admin_juniors", "mentor_author");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.mentorship), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.mentorshipView)).toBeVisible();

    await page.getByTestId(selectors.mentorshipFilterSearch).locator("input").fill("a");
    await expect(page.getByTestId(selectors.mentorshipFilterSearch).locator("input")).toHaveValue("a");
  });

  test("navigates from list row to mentorship details when rows exist", async ({ page }) => {
    const credentials = requireCredentials("admin_juniors", "mentor_author");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.mentorship), { waitUntil: "domcontentloaded" });

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    await expectVisibleRows(rows, "Mentorship registry is empty in current environment");
    await clickFirstRow(rows, "Mentorship registry is empty in current environment");
    await expect(page).toHaveURL(/\/juniors\/\d+/);
    await expect(page.getByTestId(selectors.mentorshipDetailsView)).toBeVisible();
  });
});
