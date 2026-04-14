import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import { readDocumentScrollMetrics, readTableScrollMetrics } from "../support/test-helpers";

async function scrollTable(table: import("@playwright/test").Locator, scrollTop: number) {
  await table.evaluate((element, nextScrollTop) => {
    const wrapper = element.querySelector<HTMLElement>(".v-table__wrapper");
    if (!wrapper) {
      throw new Error("Table wrapper was not found");
    }

    wrapper.scrollTop = nextScrollTop;
    wrapper.dispatchEvent(new Event("scroll", { bubbles: true }));
  }, scrollTop);
}

async function openNavigationDrawer(page: import("@playwright/test").Page) {
  const navButton = page.getByRole("button").filter({ has: page.locator(".mdi-menu") }).first();
  await expect(navButton).toBeVisible();
  await navButton.click();
}

async function navigateViaDrawer(page: import("@playwright/test").Page, href: string) {
  await openNavigationDrawer(page);
  const targetLink = page.locator(`a[href="${href}"]`).first();
  await expect(targetLink).toBeVisible();
  await targetLink.click();
}

test.describe("Vacations Return Height Regression", () => {
  test("keeps vacations table viewport height stable after overtimes roundtrip", async ({ page }) => {
    const credentials = requireCredentials("employee");

    await page.setViewportSize({ width: 1280, height: 720 });
    await loginViaUi(page, credentials);

    await page.goto(appPath(routes.vacations), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();

    const table = page.getByTestId(selectors.vacationsListTable);
    await expect(
      table.locator("tbody tr").filter({ hasText: /\d{4}/ }).first(),
    ).toBeVisible({ timeout: 15000 });

    const initialDocument = await readDocumentScrollMetrics(page);
    const initialTable = await readTableScrollMetrics(table);

    await scrollTable(table, 320);
    await navigateViaDrawer(page, "/overtimes");
    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();
    await navigateViaDrawer(page, "/vacations");

    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();
    await expect(
      table.locator("tbody tr").filter({ hasText: /\d{4}/ }).first(),
    ).toBeVisible({ timeout: 15000 });

    const returnDocument = await readDocumentScrollMetrics(page);
    const returnTable = await readTableScrollMetrics(table);

    expect(returnDocument.scrollHeight).toBeLessThanOrEqual(returnDocument.clientHeight + 1);
    expect(initialDocument.scrollHeight).toBeLessThanOrEqual(initialDocument.clientHeight + 1);
    expect(
      returnTable.clientHeight,
      `Expected vacations table viewport height to stay stable after route roundtrip, initial=${initialTable.clientHeight}, after=${returnTable.clientHeight}`,
    ).toBe(initialTable.clientHeight);
  });
});
