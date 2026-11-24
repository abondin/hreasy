import { expect, test } from "@playwright/test";
import { loginViaApi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { clickFirstRow } from "../support/test-helpers";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Mentorship Registry List", () => {
  test("navigates from list row to mentorship details when rows exist", async ({ page }) => {
    const credentials = requireCredentials("admin_juniors", "mentor_author");
    await loginViaApi(page, credentials);
    await page.goto(appPath(routes.mentorship), { waitUntil: "domcontentloaded" });

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    await clickFirstRow(rows, "Mentorship registry is empty in current environment");
    await expect(page).toHaveURL(/\/juniors\/\d+/);
    await expect(page.getByTestId(selectors.mentorshipDetailsView)).toBeVisible();
  });
});
