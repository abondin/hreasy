import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

test.describe("Vacations tabs regression", () => {
  test("keeps tabs visible after switching summary -> list", async ({ page }) => {
    const credentials = requireCredentials("employee");

    await loginViaUi(page, credentials);
    await page.goto(appPath(routes.vacations), { waitUntil: "domcontentloaded" });

    const tabs = page.getByTestId(selectors.vacationsTabs);
    const listTab = page.getByTestId(selectors.vacationsTabList);
    const summaryTab = page.getByTestId(selectors.vacationsTabSummary);
    const timelineTab = page.getByTestId(selectors.vacationsTabTimeline);

    await expect(page.getByTestId(selectors.vacationsView)).toBeVisible();
    await expect(tabs).toBeVisible();
    await expect(listTab).toBeVisible();
    await expect(summaryTab).toBeVisible();
    await expect(timelineTab).toBeVisible();
    await expectNoBrowserScroll(page);

    await summaryTab.click();
    await expect(summaryTab).toHaveAttribute("aria-selected", "true");
    await expect(tabs).toBeVisible();
    await expectNoBrowserScroll(page);

    await listTab.click();
    await expect(listTab).toHaveAttribute("aria-selected", "true");
    await expect(tabs).toBeVisible();
    await expect(listTab).toBeVisible();
    await expect(summaryTab).toBeVisible();
    await expect(timelineTab).toBeVisible();
    await expectNoBrowserScroll(page);
  });
});

async function expectNoBrowserScroll(page: Parameters<typeof test>[0]["page"]) {
  let lastMetrics: {
    htmlScrollHeight: number;
    htmlClientHeight: number;
    bodyScrollHeight: number;
    bodyClientHeight: number;
  } | null = null;

  await expect.poll(async () => {
    const metrics = await page.evaluate(() => ({
      htmlScrollHeight: document.documentElement.scrollHeight,
      htmlClientHeight: document.documentElement.clientHeight,
      bodyScrollHeight: document.body.scrollHeight,
      bodyClientHeight: document.body.clientHeight,
    }));
    lastMetrics = metrics;

    return (
      metrics.htmlScrollHeight <= metrics.htmlClientHeight + 1
      && metrics.bodyScrollHeight <= metrics.bodyClientHeight + 1
    );
  }, {
    message: `Expected vacations page to avoid browser-level vertical scroll. Last metrics: ${JSON.stringify(lastMetrics)}`,
  }).toBe(true);

  const metrics = await page.evaluate(() => ({
    htmlScrollHeight: document.documentElement.scrollHeight,
    htmlClientHeight: document.documentElement.clientHeight,
    bodyScrollHeight: document.body.scrollHeight,
    bodyClientHeight: document.body.clientHeight,
  }));

  expect(metrics.htmlScrollHeight).toBeLessThanOrEqual(metrics.htmlClientHeight + 1);
  expect(metrics.bodyScrollHeight).toBeLessThanOrEqual(metrics.bodyClientHeight + 1);
}
