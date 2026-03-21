import { expect, test } from "@playwright/test";
import { credentialsOrSkip, loginViaUi } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { routes } from "../support/test-data";
import { selectors } from "../support/selectors";

test.describe("Mentorship Details Reports", () => {
  test("opens details reports block for accessible junior", async ({ page }) => {
    const credentials = credentialsOrSkip("mentor_author") ?? credentialsOrSkip("admin_juniors");
    test.skip(!credentials, "Set E2E_MENTOR_AUTHOR_* or E2E_ADMIN_JUNIORS_* credentials");

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
