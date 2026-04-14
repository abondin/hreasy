import { expect, test, type Locator } from "@playwright/test";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

async function readTableState(table: Locator) {
  return table.evaluate((element) => {
    const wrapper = element.querySelector<HTMLElement>(".v-table__wrapper");
    const rows = Array.from(element.querySelectorAll("tbody tr"))
      .slice(0, 5)
      .map((row) => row.textContent?.trim() ?? "");

    return {
      textRows: rows,
      rowCount: element.querySelectorAll("tbody tr").length,
      scrollTop: wrapper?.scrollTop ?? null,
      scrollHeight: wrapper?.scrollHeight ?? null,
      clientHeight: wrapper?.clientHeight ?? null,
    };
  });
}

async function scrollTable(table: Locator, top: number) {
  await table.evaluate((element, scrollTop) => {
    const wrapper = element.querySelector<HTMLElement>(".v-table__wrapper");
    if (wrapper) {
      wrapper.scrollTop = scrollTop;
      wrapper.dispatchEvent(new Event("scroll", { bubbles: true }));
    }
  }, top);
}

test.describe("Table Sandbox Virtual Scroll Diagnostic", () => {
  test("keeps visible rows after route leave and return from a deep table scroll", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.goto(appPath(routes.tableSandbox), { waitUntil: "domcontentloaded" });

    const table = page.getByTestId(selectors.tableSandboxDirectoryTable);
    await expect(
      table.locator("tbody tr").filter({ hasText: /Directory Row 1/i }).first(),
    ).toBeVisible({ timeout: 10000 });

    await scrollTable(table, 4200);
    await expect.poll(async () => {
      const state = await readTableState(table);
      return state.scrollTop ?? 0;
    }).toBeGreaterThan(3000);

    console.log("BEFORE_LEAVE_DEEP_SCROLL", JSON.stringify(await readTableState(table)));

    await page.getByTestId(selectors.tableSandboxGoEcho).click();
    await expect(page.getByTestId(selectors.tableSandboxEchoView)).toBeVisible();

    await page.getByTestId(selectors.tableSandboxEchoBack).click();
    await expect(page.getByTestId(selectors.tableSandboxView)).toBeVisible();

    const returnState = await readTableState(table);
    console.log("AFTER_RETURN_DEEP_SCROLL", JSON.stringify(returnState));

    await expect.poll(async () => {
      const state = await readTableState(table);
      return state.textRows.filter(Boolean).length;
    }, {
      message: "Expected visible row content after returning from keep-alive route",
    }).toBeGreaterThan(0);
  });

  test("renders rows after async reload finishes while the keep-alive page is hidden", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.goto(appPath(routes.tableSandbox), { waitUntil: "domcontentloaded" });

    const table = page.getByTestId(selectors.tableSandboxDirectoryTable);
    await expect(
      table.locator("tbody tr").filter({ hasText: /Directory Row 1/i }).first(),
    ).toBeVisible({ timeout: 10000 });

    await page.getByTestId(selectors.tableSandboxReload).click();

    await page.getByTestId(selectors.tableSandboxGoEcho).click();
    await expect(page.getByTestId(selectors.tableSandboxEchoView)).toBeVisible();

    const holdUntil = Date.now() + 500;
    await page.waitForFunction((targetTime) => Date.now() >= targetTime, holdUntil);
    await page.getByTestId(selectors.tableSandboxEchoBack).click();
    await expect(page.getByTestId(selectors.tableSandboxView)).toBeVisible();

    const returnState = await readTableState(table);
    console.log("AFTER_RETURN_HIDDEN_RELOAD", JSON.stringify(returnState));

    await expect(
      table.locator("tbody tr").filter({ hasText: /Directory Row 1/i }).first(),
      "Expected rows to appear after reload finishes while the page is hidden",
    ).toBeVisible({ timeout: 5000 });
  });
});
