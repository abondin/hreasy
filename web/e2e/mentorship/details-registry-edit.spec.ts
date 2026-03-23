import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { clickFirstRow, expectVisibleRows } from "../support/test-helpers";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Mentorship Registry Edit", () => {
  test("opens details and has back navigation", async ({ page }) => {
    const credentials = requireCredentials("admin_juniors", "mentor_author");
    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.mentorship), { waitUntil: "domcontentloaded" });

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    await expectVisibleRows(rows, "Mentorship registry is empty in current environment");
    await clickFirstRow(rows, "Mentorship registry is empty in current environment");
    await expect(page.getByTestId(selectors.mentorshipDetailsView)).toBeVisible();
    await expect(page.getByTestId(selectors.mentorshipDetailsBack)).toBeVisible();
  });
});
