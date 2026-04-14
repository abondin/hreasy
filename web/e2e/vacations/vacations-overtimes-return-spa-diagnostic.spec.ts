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

async function readVacationsLayoutChain(table: import("@playwright/test").Locator) {
  return table.evaluate((element) => {
    const wrapper = element.querySelector<HTMLElement>(".v-table__wrapper");
    const tableElement = element.querySelector<HTMLElement>(".v-table");
    const tableArea = element.querySelector<HTMLElement>(".h-reasy-table-base__table-area");
    const baseRoot = element.closest<HTMLElement>(".h-reasy-table-base") ?? element;
    const pageCard = element.closest<HTMLElement>(".v-card");
    const pageCardText = element.closest<HTMLElement>(".v-card-text");
    const pageContent = pageCard?.parentElement as HTMLElement | null;
    const cardTextChildren = pageCardText ? Array.from(pageCardText.children) as HTMLElement[] : [];

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
      cardTextChildren: cardTextChildren.map((child) => ({
        className: child.className,
        testId: child.getAttribute("data-testid"),
        text: (child.textContent ?? "").trim().slice(0, 80),
        clientHeight: child.clientHeight,
        scrollHeight: child.scrollHeight,
        offsetHeight: child.offsetHeight,
        rectHeight: child.getBoundingClientRect().height,
        display: window.getComputedStyle(child).display,
        overflowY: window.getComputedStyle(child).overflowY,
        flex: window.getComputedStyle(child).flex,
      })),
      baseRoot: describeNode(baseRoot),
      tableArea: describeNode(tableArea),
      tableElement: describeNode(tableElement),
      wrapper: describeNode(wrapper),
    };
  });
}

test.describe("Vacations Return SPA Scroll Diagnostic", () => {
  test("measures vacations list scroll when navigating to overtimes and back inside the SPA", async ({ page }) => {
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

    console.log("SPA_VACATIONS_INITIAL_DOCUMENT", JSON.stringify(await readDocumentScrollMetrics(page)));
    console.log("SPA_VACATIONS_INITIAL_TABLE", JSON.stringify(await readTableScrollMetrics(table)));
    console.log("SPA_VACATIONS_INITIAL_CHAIN", JSON.stringify(await readVacationsLayoutChain(table)));

    await scrollTable(table, 320);
    console.log("SPA_VACATIONS_SCROLLED_TABLE", JSON.stringify(await readTableScrollMetrics(table)));

    await navigateViaDrawer(page, "/overtimes");
    await expect(page.getByTestId(selectors.overtimesView)).toBeVisible();
    console.log("SPA_OVERTIMES_DOCUMENT", JSON.stringify(await readDocumentScrollMetrics(page)));

    await navigateViaDrawer(page, "/vacations");
    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();
    await expect(table).toBeVisible();
    await expect(
      table.locator("tbody tr").filter({ hasText: /\d{4}/ }).first(),
    ).toBeVisible({ timeout: 15000 });

    console.log("SPA_VACATIONS_RETURN_DOCUMENT", JSON.stringify(await readDocumentScrollMetrics(page)));
    console.log("SPA_VACATIONS_RETURN_TABLE", JSON.stringify(await readTableScrollMetrics(table)));
    console.log("SPA_VACATIONS_RETURN_CHAIN", JSON.stringify(await readVacationsLayoutChain(table)));
  });
});
