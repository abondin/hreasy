import { expect, test } from "@playwright/test";
import {
  appMockedAuthorities,
  installUnhandledApiGuard,
  mockAppRouteAuth,
  mockEmployeesDirectoryApi,
  mockNotificationsApi,
} from "../support/app-mocked-api";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

function notifications(count: number) {
  return Array.from({ length: count }, (_, index) => {
    const number = String(index + 1).padStart(2, "0");
    return {
      id: 9000 + index + 1,
      clientUuid: `notification-${number}`,
      title: `Project overtime request ${number}`,
      category: "overtime",
      markdownText: `Request ${number}: Alex Morgan reported ${index % 8 + 1} overtime hours for period 202603.`,
      context: JSON.stringify({ eventType: "overtime.item_created", period: 202603 }),
      createdAt: `2026-06-02T09:${number}:00Z`,
      acknowledgedAt: null,
      archivedAt: null,
    };
  });
}

test.describe("App Mocked Notifications Menu", () => {
  test("keeps failed messages unread and allows dismissing the error", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.employees);
    await mockEmployeesDirectoryApi(page);
    await mockNotificationsApi(page, notifications(50));
    await page.route("**/api/v1/notifications/my/acknowledge", route => route.fulfill({
      status: 500, contentType: "application/json", body: JSON.stringify({ message: "Test failure" }),
    }));
    await page.goto(appPath(routes.employees));
    await page.getByTestId("notifications-menu-button").click();
    const bulk = page.getByTestId("notifications-acknowledge-all");
    const alert = page.locator(".notifications-menu .v-alert");
    await bulk.click();
    await expect(alert).toContainText("Не удалось отметить уведомления как прочитанные");
    await expect.poll(() => alert.evaluate(el => el.clientHeight)).toBeGreaterThan(40);
    await expect(page.locator(".notifications-menu__item")).toHaveCount(50);
    await alert.getByRole("button").click();
    await expect(alert).toHaveCount(0);
    await bulk.click();
    await expect(alert).toBeVisible();
    await page.getByTestId("notifications-categories").getByText("Овертаймы · 50").click();
    await expect(alert).toHaveCount(0);
    await bulk.click();
    await expect(alert).toBeVisible();
    await page.keyboard.press("Escape");
    await page.getByTestId("notifications-menu-button").click();
    await expect(alert).toHaveCount(0);
  });

  test("filters categories and acknowledges only the selected category, then all", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.employees);
    await mockEmployeesDirectoryApi(page);
    const items = notifications(3);
    items[2]!.category = "upcoming_vacation";
    await mockNotificationsApi(page, items);
    await page.goto(appPath(routes.employees));
    await page.getByTestId("notifications-menu-button").click();
    const categories = page.getByTestId("notifications-categories");
    const list = page.getByTestId("notifications-menu-list");
    await expect(list.locator(".notifications-menu__item")).toHaveCount(3);
    await expect.poll(() => page.locator(".notifications-menu").evaluate(el => el.clientWidth)).toBe(640);
    await categories.getByText("Овертаймы · 2").click();
    await expect(list.locator(".notifications-menu__item")).toHaveCount(2);
    const bulk = page.getByTestId("notifications-acknowledge-all");
    const request = page.waitForRequest("**/api/v1/notifications/my/acknowledge");
    await bulk.click();
    expect((await request).postDataJSON()).toEqual([9001, 9002]);
    await expect(page.getByText("Нет новых уведомлений")).toBeVisible();
    await categories.getByText("Все · 1", { exact: true }).click();
    await expect(list.locator(".notifications-menu__item")).toHaveCount(1);
    await page.setViewportSize({ width: 390, height: 844 });
    await expect.poll(() => page.locator(".notifications-menu").evaluate(el => el.getBoundingClientRect().right)).toBeLessThanOrEqual(390);
    await bulk.click();
    await expect(page.getByText("Нет новых уведомлений")).toBeVisible();
    await expect(bulk).toBeDisabled();
  });

  test("shows a scrollable unread list and acknowledges one item", async ({ page }) => {
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, appMockedAuthorities.employees);
    await mockEmployeesDirectoryApi(page);
    await mockNotificationsApi(page, notifications(50));

    let acknowledgeRequests = 0;
    page.on("request", (request) => {
      if (request.method() === "POST" && /\/api\/v1\/notifications\/\d+\/acknowledge$/.test(request.url())) {
        acknowledgeRequests += 1;
      }
    });

    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();

    await page.getByTestId("notifications-menu-button").click();

    await expect(page.getByText("Project overtime request 01")).toBeVisible();
    await expect(page.getByText("Request 01: Alex Morgan reported 1 overtime hours for period 202603.")).toBeVisible();
    await expect(page.getByRole("link", { name: "Open" })).toHaveCount(0);

    const list = page.getByTestId("notifications-menu-list");
    await expect(list.locator(".notifications-menu__item")).toHaveCount(50);
    await expect.poll(
      () => list.evaluate((element) => element.scrollHeight > element.clientHeight),
      { message: "Expected notifications list to scroll with 50 items" },
    ).toBe(true);

    await list.evaluate((element) => {
      element.scrollTop = element.scrollHeight;
    });
    await expect(page.getByText("Project overtime request 50")).toBeVisible();

    await list.locator(".notifications-menu__item").last()
      .getByTestId("notification-acknowledge-button")
      .click();

    await expect
      .poll(() => acknowledgeRequests, {
        timeout: 3000,
        message: "Expected acknowledge endpoint to be called",
      })
      .toBe(1);
    await expect(list.locator(".notifications-menu__item")).toHaveCount(49);
    await expect(page.getByText("Project overtime request 50")).toHaveCount(0);
  });
});
