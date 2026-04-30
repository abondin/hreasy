import { expect, test, type Locator, type Page } from "@playwright/test";
import {
  appMockedAuthorities,
  installUnhandledApiGuard,
  mockAppRouteAuth,
  mockOvertimesSummaryApi,
  mockVacationsManagementApi,
} from "../support/app-mocked-api";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import {
  readDocumentScrollMetrics,
  readTableScrollMetrics,
} from "../support/test-helpers";

async function scrollTable(table: Locator, scrollTop: number): Promise<void> {
  await table.evaluate((element, nextScrollTop) => {
    const wrapper = element.querySelector<HTMLElement>(".v-table__wrapper");
    if (!wrapper) {
      throw new Error("Table wrapper was not found");
    }
    wrapper.scrollTop = nextScrollTop;
    wrapper.dispatchEvent(new Event("scroll", { bubbles: true }));
  }, scrollTop);
}

async function openNavigationDrawer(page: Page): Promise<void> {
  const navButton = page.getByRole("button").filter({ has: page.locator(".mdi-menu") }).first();
  await expect(navButton).toBeVisible();
  await navButton.click();
}

async function navigateViaDrawer(page: Page, href: string): Promise<void> {
  await openNavigationDrawer(page);
  const targetLink = page.locator(`a[href="${href}"]`).first();
  await expect(targetLink).toBeVisible();
  await targetLink.click();
}

test.describe("App Mocked Vacations Overtimes Roundtrip", () => {
  test("keeps vacations table height stable after overtimes roundtrip in real app shell", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await installUnhandledApiGuard(page);
    await mockAppRouteAuth(page, [
      ...appMockedAuthorities.vacations,
      ...appMockedAuthorities.overtimes,
    ]);
    await mockVacationsManagementApi(page);
    await mockOvertimesSummaryApi(page);

    await page.goto(appPath(routes.vacations), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();

    const table = page.getByTestId(selectors.vacationsListTable);
    await expect(table).toBeVisible();

    const initialDocument = await readDocumentScrollMetrics(page);
    const initialTable = await readTableScrollMetrics(table);

    await scrollTable(table, 320);
    await navigateViaDrawer(page, "/overtimes");
    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();

    await navigateViaDrawer(page, "/vacations");
    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();
    await expect(table).toBeVisible();

    const returnDocument = await readDocumentScrollMetrics(page);
    const returnTable = await readTableScrollMetrics(table);

    expect(initialDocument.scrollHeight).toBeLessThanOrEqual(initialDocument.clientHeight + 1);
    expect(returnDocument.scrollHeight).toBeLessThanOrEqual(returnDocument.clientHeight + 1);
    expect(returnTable.clientHeight).toBe(initialTable.clientHeight);
    expect(returnTable.scrollHeight).toBeGreaterThan(returnTable.clientHeight + 4);
  });
});
