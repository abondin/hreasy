import { expect, test } from "@playwright/test";
import {
  appMockedAuthorities,
  installUnhandledApiGuard,
  mockAppRouteAuth,
  mockMentorshipRegistryApi,
} from "../support/app-mocked-api";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

test.describe("App Mocked Mentorship Registry", () => {
  test("keeps add form selections while moving between fields", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.mentorshipAdmin);
    await mockMentorshipRegistryApi(page);

    await page.goto(appPath(routes.mentorship), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.mentorshipView)).toBeVisible();

    await page.getByTestId(selectors.toolbarAdd).click();

    const junior = page.getByTestId("junior-registry-junior").locator("input");
    await junior.fill("Alex Morgan");
    await page.getByRole("option", { name: "Alex Morgan", exact: true }).click();
    await expect(junior).toHaveValue("Alex Morgan");

    const mentor = page.getByTestId("junior-registry-mentor").locator("input");
    await mentor.focus();

    await expect(page.getByTestId("junior-registry-junior")).toContainText("Alex Morgan");
    await expect(page.getByTestId("junior-registry-budgeting-account")).toContainText("Northwind Delivery");
    await expect(page.getByTestId("junior-registry-role")).toContainText("Backend C#");

    const createRequest = page.waitForRequest((request) =>
      request.method() === "POST" && request.url().endsWith("/api/v1/udr/juniors"),
    );
    await page.locator(".v-card-actions").getByRole("button").last().click();

    const payload = JSON.parse((await createRequest).postData() ?? "{}");
    expect(payload).toEqual({
      juniorEmplId: 101,
      mentorId: null,
      budgetingAccount: 401,
      role: "Backend C#",
    });
  });
});
