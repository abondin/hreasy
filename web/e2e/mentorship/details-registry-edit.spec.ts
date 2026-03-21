import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Mentorship Registry Edit", () => {
  test("opens details and has back navigation", async ({ page }) => {
    const credentials = credentialsOrSkip("admin_juniors") ?? credentialsOrSkip("mentor_author");
    test.skip(!credentials, "Set E2E_ADMIN_JUNIORS_* or E2E_MENTOR_AUTHOR_* credentials");

    await loginViaUi(page, credentials!);
    await page.goto(appPath(routes.mentorship), { waitUntil: "domcontentloaded" });

    const rows = page.locator("tbody tr:visible").filter({ hasText: /\S/ });
    const rowCount = await rows.count();
    test.skip(rowCount === 0, "Mentorship registry is empty in current environment");

    await rows.first().locator("td").first().click();
    await expect(page.getByTestId(selectors.mentorshipDetailsView)).toBeVisible();
    await expect(page.getByTestId(selectors.mentorshipDetailsBack)).toBeVisible();
  });
});
