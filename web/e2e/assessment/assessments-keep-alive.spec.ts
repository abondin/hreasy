import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

test.describe("Assessments keep-alive", () => {
  test("keeps search filter after navigating to employee assessments and back", async ({ page }) => {
    const credentials = requireCredentials("employee");

    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.assessments), { waitUntil: "domcontentloaded" });

    const assessmentsView = page.getByTestId(selectors.assessmentsView);
    const searchField = page.getByTestId(selectors.assessmentsFilterSearch);
    const employeeLink = page.getByRole("link", { name: "Бондин Александр Валерьевич" }).first();

    await expect(assessmentsView).toBeVisible();
    await expect(searchField).toBeVisible();

    await searchField.locator("input").fill("Бондин Александр");
    await expect(employeeLink).toBeVisible();

    await employeeLink.click();

    await expect(page).toHaveURL(new RegExp(`${routes.employeeAssessmentsPrefix}\\d+$`));
    await expect(page.getByTestId(selectors.employeeAssessmentsView)).toBeVisible();

    await page.goBack({ waitUntil: "domcontentloaded" });

    await expect(page).toHaveURL(new RegExp(`${routes.assessments}$`));
    await expect(assessmentsView).toBeVisible();
    await expect(searchField.locator("input")).toHaveValue("Бондин Александр");
    await expect(employeeLink).toBeVisible();
  });
});
