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
  test("keeps a multiline error fully visible above the table", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.mentorshipAdmin);
    await mockMentorshipRegistryApi(page);
    const message = "Registry request failed. ".repeat(30);
    await page.route("**/api/v1/udr/juniors", (route) => route.fulfill({
      status: 422,
      contentType: "application/json",
      body: JSON.stringify({ message }),
    }));
    await page.goto(appPath(routes.mentorship));
    const alert = page.getByRole("alert");
    await expect(alert).toBeVisible();
    await expect(alert).toContainText(message.trim());
    await expect.poll(() => alert.evaluate((element) => {
      const content = element.querySelector(".v-alert__content")!;
      return content.getBoundingClientRect().bottom <= element.getBoundingClientRect().bottom;
    })).toBe(true);
  });

  test("keeps add form selections while moving between fields", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.mentorshipAdmin);
    await mockMentorshipRegistryApi(page);

    await page.goto(appPath(routes.mentorship), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.mentorshipView)).toBeVisible();

    await page.getByTestId(selectors.toolbarAdd).click();

    const junior = page.getByTestId("junior-registry-junior").locator('input[role="combobox"]');
    await junior.fill("Alex Morgan");
    await page.getByRole("option", { name: "Alex Morgan", exact: true }).click();
    await expect(junior).toHaveValue("Alex Morgan");

    const mentor = page.getByTestId("junior-registry-mentor").locator('input[role="combobox"]');
    await mentor.focus();

    await expect(page.getByTestId("junior-registry-junior")).toContainText("Alex Morgan");
    await expect(page.getByTestId("junior-registry-budgeting-account")).toContainText("Northwind Delivery");
    await expect(page.getByTestId("junior-registry-role")).toContainText("Backend C#");

    const createRequest = page.waitForRequest((request) =>
      request.method() === "POST" && request.url().endsWith("/api/v1/udr/juniors"),
    );
    const role = page.getByTestId("junior-registry-role").locator('input[role="combobox"]');
    await role.click();
    await page.getByRole("option", { name: "Backend C#", exact: true }).click();
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
