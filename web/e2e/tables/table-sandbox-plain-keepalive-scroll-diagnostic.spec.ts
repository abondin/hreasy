import { expect, test } from "@playwright/test";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import { expectDocumentNotToScroll, expectTableToScroll } from "../support/test-helpers";

async function scrollTableDeep(table: import("@playwright/test").Locator): Promise<void> {
  await table.evaluate((element) => {
    const wrapper = element.querySelector<HTMLElement>(".v-table__wrapper");
    if (!wrapper) {
      throw new Error("Table wrapper was not found");
    }

    wrapper.scrollTop = 4200;
    wrapper.dispatchEvent(new Event("scroll", { bubbles: true }));
  });
}

test.describe("Table Sandbox Plain KeepAlive Diagnostic", () => {
  test("keeps internal table scroll after route leave and return without tabs", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.goto(appPath(routes.tableSandboxPlain), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.tableSandboxPlainView)).toBeVisible();

    const table = page.getByTestId(selectors.tableSandboxPlainTable);
    await expect(
      table.locator("tbody tr").filter({ hasText: /Plain Row 1/i }).first(),
    ).toBeVisible({ timeout: 10000 });

    await expectDocumentNotToScroll(page);
    await expectTableToScroll(table);

    await scrollTableDeep(table);
    await page.getByTestId(selectors.tableSandboxPlainGoEcho).click();
    await expect(page.getByTestId(selectors.tableSandboxEchoView)).toBeVisible();

    await page.goto(appPath(routes.tableSandboxPlain), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.tableSandboxPlainView)).toBeVisible();

    await expectDocumentNotToScroll(page);
    await expectTableToScroll(table);
  });
});
