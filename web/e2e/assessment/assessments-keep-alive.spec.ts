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

    await expect(assessmentsView).toBeVisible();
    await expect(searchField).toBeVisible();

    const employeeLink = page.locator("tbody a[href^='/assessments/']").first();
    await expect(employeeLink).toBeVisible();
    const employeeName = (await employeeLink.textContent())?.trim() ?? "";
    const searchToken = employeeName.split(/\s+/).slice(0, 2).join(" ");

    await searchField.locator("input").fill(searchToken);
    await expect(employeeLink).toBeVisible();

    await employeeLink.click();

    await expect(page).toHaveURL(new RegExp(`${routes.employeeAssessmentsPrefix}\\d+$`));
    await expect(page.getByTestId(selectors.employeeAssessmentsView)).toBeVisible();

    await page.goBack({ waitUntil: "domcontentloaded" });

    await expect(page).toHaveURL(new RegExp(`${routes.assessments}$`));
    await expect(assessmentsView).toBeVisible();
    await expect(searchField.locator("input")).toHaveValue(searchToken);
    await expect(employeeLink).toBeVisible();
  });
});
