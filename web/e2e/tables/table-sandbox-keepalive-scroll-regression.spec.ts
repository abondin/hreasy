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

async function readLayoutChain(table: import("@playwright/test").Locator) {
  return table.evaluate((element) => {
    const wrapper = element.querySelector<HTMLElement>(".v-table__wrapper");
    const tableElement = element.querySelector<HTMLElement>(".v-table");
    const tableArea = element.querySelector<HTMLElement>(".h-reasy-table-base__table-area");
    const baseRoot = element.closest<HTMLElement>(".h-reasy-table-base") ?? element;
    const cardText = element.closest<HTMLElement>(".v-card-text");
    const card = element.closest<HTMLElement>(".v-card");
    const paddedWrapper = card?.parentElement as HTMLElement | null;
    const windowItem = card?.closest<HTMLElement>(".v-window-item");
    const windowRoot = card?.closest<HTMLElement>(".v-window");
    const pageContent = element.closest<HTMLElement>("[style*='height']");

    function describeNode(node: HTMLElement | null) {
      if (!node) {
        return null;
      }

      const rect = node.getBoundingClientRect();
      const style = window.getComputedStyle(node);

      return {
        className: node.className,
        clientHeight: node.clientHeight,
        scrollHeight: node.scrollHeight,
        offsetHeight: node.offsetHeight,
        rectHeight: rect.height,
        overflowY: style.overflowY,
        minHeight: style.minHeight,
        height: style.height,
        maxHeight: style.maxHeight,
        position: style.position,
        display: style.display,
        flex: style.flex,
      };
    }

      return {
      pageContent: describeNode(pageContent),
      windowRoot: describeNode(windowRoot),
      windowItem: describeNode(windowItem),
      paddedWrapper: describeNode(paddedWrapper),
      card: describeNode(card),
      cardText: describeNode(cardText),
      baseRoot: describeNode(baseRoot),
      tableArea: describeNode(tableArea),
      tableElement: describeNode(tableElement),
      wrapper: describeNode(wrapper),
    };
  });
}

test.describe("Table Sandbox KeepAlive Scroll Regression", () => {
  test("keeps the table constrained after returning from another route", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.goto(appPath(routes.tableSandbox), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.tableSandboxView)).toBeVisible();

    const table = page.getByTestId(selectors.tableSandboxDirectoryTable);
    await expect(
      table.locator("tbody tr").filter({ hasText: /Directory Row 1/i }).first(),
    ).toBeVisible({ timeout: 10000 });

    await expectDocumentNotToScroll(page);
    await expectTableToScroll(table);
    console.log("BEFORE_LEAVE_LAYOUT_CHAIN", JSON.stringify(await readLayoutChain(table)));

    await scrollTableDeep(table);

    await page.getByTestId(selectors.tableSandboxGoEcho).click();
    await expect(page.getByTestId(selectors.tableSandboxEchoView)).toBeVisible();

    await page.getByTestId(selectors.tableSandboxEchoBack).click();
    await expect(page.getByTestId(selectors.tableSandboxView)).toBeVisible();

    console.log("AFTER_RETURN_LAYOUT_CHAIN", JSON.stringify(await readLayoutChain(table)));
    await expectDocumentNotToScroll(page);
    await expectTableToScroll(table);
  });
});
