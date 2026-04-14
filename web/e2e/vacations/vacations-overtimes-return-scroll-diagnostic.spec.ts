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

async function readVacationsLayoutChain(table: import("@playwright/test").Locator) {
  return table.evaluate((element) => {
    const wrapper = element.querySelector<HTMLElement>(".v-table__wrapper");
    const tableElement = element.querySelector<HTMLElement>(".v-table");
    const tableArea = element.querySelector<HTMLElement>(".h-reasy-table-base__table-area");
    const baseRoot = element.closest<HTMLElement>(".h-reasy-table-base") ?? element;
    const listTabRoot = element.closest<HTMLElement>(".d-flex.flex-column.h-100.min-h-0");
    const pageCard = element.closest<HTMLElement>(".v-card");
    const pageCardText = element.closest<HTMLElement>(".v-card-text");
    const pageContent = pageCard?.parentElement as HTMLElement | null;

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
        display: style.display,
        flex: style.flex,
      };
    }

      return {
      pageContent: describeNode(pageContent),
      pageCard: describeNode(pageCard),
      pageCardText: describeNode(pageCardText),
      listTabRoot: describeNode(listTabRoot),
      baseRoot: describeNode(baseRoot),
      tableArea: describeNode(tableArea),
      tableElement: describeNode(tableElement),
      wrapper: describeNode(wrapper),
    };
  });
}

test.describe("Vacations Return Scroll Diagnostic", () => {
  test("measures vacations list scroll before and after returning from overtimes", async ({ page }) => {
    const credentials = requireCredentials("employee");

    await page.setViewportSize({ width: 1280, height: 720 });
    await loginViaUi(page, credentials);

    await page.goto(appPath(routes.vacations), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();

    const table = page.getByTestId(selectors.vacationsListTable);
    await expect(table).toBeVisible();
    await expect(
      table.locator("tbody tr").filter({ hasText: /\d{4}/ }).first(),
    ).toBeVisible({ timeout: 15000 });

    console.log("VACATIONS_INITIAL_DOCUMENT", JSON.stringify(await readDocumentScrollMetrics(page)));
    console.log("VACATIONS_INITIAL_TABLE", JSON.stringify(await readTableScrollMetrics(table)));
    console.log("VACATIONS_INITIAL_CHAIN", JSON.stringify(await readVacationsLayoutChain(table)));

    await scrollTable(table, 320);
    console.log("VACATIONS_SCROLLED_TABLE", JSON.stringify(await readTableScrollMetrics(table)));

    await page.goto(appPath(routes.overtimes), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();
    console.log("OVERTIMES_DOCUMENT", JSON.stringify(await readDocumentScrollMetrics(page)));

    await page.goto(appPath(routes.vacations), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();
    await expect(table).toBeVisible();
    await expect(
      table.locator("tbody tr").filter({ hasText: /\d{4}/ }).first(),
    ).toBeVisible({ timeout: 15000 });

    console.log("VACATIONS_RETURN_DOCUMENT", JSON.stringify(await readDocumentScrollMetrics(page)));
    console.log("VACATIONS_RETURN_TABLE", JSON.stringify(await readTableScrollMetrics(table)));
    console.log("VACATIONS_RETURN_CHAIN", JSON.stringify(await readVacationsLayoutChain(table)));
  });
});
