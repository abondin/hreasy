import { expect, test } from "@playwright/test";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import { expectDocumentNotToScroll, readTableScrollMetrics } from "../support/test-helpers";

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

async function readWrapperScrollState(table: import("@playwright/test").Locator) {
  return table.evaluate((element) => {
    const wrapper = element.querySelector<HTMLElement>(".v-table__wrapper");
    if (!wrapper) {
      throw new Error("Table wrapper was not found");
    }

    const maxScrollTop = Math.max(0, wrapper.scrollHeight - wrapper.clientHeight);
    return {
      scrollTop: wrapper.scrollTop,
      clientHeight: wrapper.clientHeight,
      scrollHeight: wrapper.scrollHeight,
      maxScrollTop,
    };
  });
}

async function readViewportVisibleBodyRowCount(
  table: import("@playwright/test").Locator,
): Promise<number> {
  return table.locator("tbody tr").evaluateAll((rows) => {
    const tableRoot = rows[0]?.closest(".v-table");
    const wrapper = tableRoot?.querySelector(".v-table__wrapper");
    if (!(wrapper instanceof HTMLElement)) {
      return 0;
    }

    const wrapperRect = wrapper.getBoundingClientRect();

    return rows.filter((row) => {
      if (!(row instanceof HTMLElement)) {
        return false;
      }

      const rect = row.getBoundingClientRect();
      return (
        rect.height > 0 &&
        rect.width > 0 &&
        rect.bottom > wrapperRect.top &&
        rect.top < wrapperRect.bottom
      );
    }).length;
  });
}

test.describe("Table Harness Vacations Roundtrip", () => {
  test("keeps visible body rows after route roundtrip from a scrolled vacations table", async ({
    page,
  }) => {
    await page.setViewportSize({ width: 1685, height: 1099 });
    await page.goto(appPath(routes.tableSandboxVacationsLike), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.tableSandboxVacationsView)).toBeVisible();

    const listTable = page.getByTestId(selectors.tableSandboxVacationsListTable);
    await expect(listTable).toBeVisible();
    await expectDocumentNotToScroll(page);
    const initialMetrics = await readTableScrollMetrics(listTable);
    const initialVisibleRows = await readViewportVisibleBodyRowCount(listTable);
    const initialScrollState = await readWrapperScrollState(listTable);
    const targetScrollTop = Math.max(
      320,
      Math.min(initialScrollState.maxScrollTop, Math.round(initialScrollState.maxScrollTop * 0.2)),
    );

    await scrollTable(listTable, targetScrollTop);
    const afterScrollState = await readWrapperScrollState(listTable);
    expect(afterScrollState.scrollTop).toBeGreaterThan(320);
    await page.getByTestId(selectors.tableSandboxVacationsGoEcho).click();
    await expect(page.getByTestId(selectors.tableSandboxOvertimesView)).toBeVisible();
    await page.getByTestId(selectors.tableSandboxOvertimesGoVacations).click();

    await expect(page.getByTestId(selectors.tableSandboxVacationsView)).toBeVisible();
    await expect(listTable).toBeVisible();
    await expectDocumentNotToScroll(page);
    const returnMetrics = await readTableScrollMetrics(listTable);
    const afterReturnVisibleRows = await readViewportVisibleBodyRowCount(listTable);

    expect(
      returnMetrics.clientHeight,
      `Expected harness vacations table viewport height to stay stable after roundtrip, initial=${initialMetrics.clientHeight}, after=${returnMetrics.clientHeight}`,
    ).toBe(initialMetrics.clientHeight);
    expect(
      afterReturnVisibleRows,
      "Expected harness vacations table to keep several body rows visible in the wrapper viewport immediately after route roundtrip",
    ).toBeGreaterThanOrEqual(Math.min(5, initialVisibleRows));
  });

  test("keeps tabs and active vacations tab stable across route roundtrip", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.goto(appPath(routes.tableSandboxVacationsLike), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.tableSandboxVacationsView)).toBeVisible();
    await expect(page.getByTestId(selectors.tableSandboxVacationsTabs)).toBeVisible();

    await expect(page.getByTestId(selectors.tableSandboxVacationsListTable)).toBeVisible();
    await expectDocumentNotToScroll(page);

    await page.getByTestId(selectors.tableSandboxVacationsTabSummary).click();
    await expect(page.getByTestId(selectors.tableSandboxVacationsSummaryTable)).toBeVisible();
    await expectDocumentNotToScroll(page);

    await page.getByTestId(selectors.tableSandboxVacationsGoEcho).click();
    await expect(page.getByTestId(selectors.tableSandboxOvertimesView)).toBeVisible();
    await page.getByTestId(selectors.tableSandboxOvertimesGoVacations).click();
    await expect(page.getByTestId(selectors.tableSandboxVacationsView)).toBeVisible();
    await expect(page.getByTestId(selectors.tableSandboxVacationsSummaryTable)).toBeVisible();
    await expectDocumentNotToScroll(page);
  });
});
