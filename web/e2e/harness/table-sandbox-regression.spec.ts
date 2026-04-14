import { expect, test } from "@playwright/test";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";
import {
  expectDocumentNotToScroll,
  expectDocumentToScroll,
  expectFilterToBeAccessible,
  expectTableToScroll,
  fillAdaptiveFilterInput,
  openAdaptiveFilterOverflow,
  readHeaderCellWidths,
  expectWidthsToStayStable,
} from "../support/test-helpers";

test.describe("Table Sandbox Regression", () => {
  test("renders async rows without resize and keeps scroll inside table", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.goto(appPath(routes.tableSandbox), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.tableSandboxView)).toBeVisible();

    const table = page.getByTestId(selectors.tableSandboxDirectoryTable);
    await expect(table).toBeVisible();

    const firstDataRow = table
      .locator("tbody tr")
      .filter({ hasText: /Directory Row 1/i })
      .first();

    await expect
      .poll(async () => {
        const loadingRows = await table
          .locator("tbody tr")
          .filter({ hasText: /Р вЂ”Р В°Р С–РЎР‚РЎС“Р В·Р С”Р В° Р Т‘Р В°Р Р…Р Р…РЎвЂ№РЎвЂ¦/i })
          .count();
        const dataRows = await table.locator("tbody tr").filter({ hasText: /Directory Row 1/i }).count();
        return loadingRows > 0 || dataRows > 0;
      }, {
        message: "Expected sandbox table to be either loading or already rendering data",
      })
      .toBe(true);

    await expect(
      firstDataRow,
      "Expected sandbox table to render data rows after async load without any manual resize",
    ).toBeVisible({ timeout: 10000 });

    await expect(
      table.locator("tbody tr").filter({ hasText: /Р вЂ”Р В°Р С–РЎР‚РЎС“Р В·Р С”Р В° Р Т‘Р В°Р Р…Р Р…РЎвЂ№РЎвЂ¦/i }),
    ).toHaveCount(0);
    await expectDocumentNotToScroll(page);
    await expectTableToScroll(table);

    await page.getByTestId(selectors.tableSandboxReload).click();
    await expect(firstDataRow).toBeVisible({ timeout: 10000 });
    await expect(
      table.locator("tbody tr").filter({ hasText: /Р вЂ”Р В°Р С–РЎР‚РЎС“Р В·Р С”Р В° Р Т‘Р В°Р Р…Р Р…РЎвЂ№РЎвЂ¦/i }),
    ).toHaveCount(0);
    await expectDocumentNotToScroll(page);
    await expectTableToScroll(table);
  });

  test("survives filter overflow, sorting, banner toggle and tab switch", async ({ page }) => {
    await page.setViewportSize({ width: 1024, height: 600 });
    await page.goto(appPath(routes.tableSandbox), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.tableSandboxView)).toBeVisible();

    const directoryTable = page.getByTestId(selectors.tableSandboxDirectoryTable);
    await expect(
      directoryTable.locator("tbody tr").filter({ hasText: /Directory Row 1/i }).first(),
    ).toBeVisible({ timeout: 10000 });

    await expectFilterToBeAccessible(page, selectors.tableSandboxFilterSearch);
    await expectFilterToBeAccessible(page, selectors.tableSandboxFilterDepartment);
    await expectFilterToBeAccessible(page, selectors.tableSandboxFilterStatus);
    await expectFilterToBeAccessible(page, selectors.tableSandboxFilterProject);

    const widthsBefore = await readHeaderCellWidths(directoryTable);

    await fillAdaptiveFilterInput(page, selectors.tableSandboxFilterSearch, "Directory Row 12");
    await expect(
      directoryTable.locator("tbody tr").filter({ hasText: /Directory Row 12/i }).first(),
    ).toBeVisible();

    await openAdaptiveFilterOverflow(page);
    const statusField = page.getByTestId(selectors.tableSandboxFilterStatus);
    await expect(statusField).toBeVisible();
    await statusField.click();
    await page.getByRole("option", { name: "Draft" }).click();
    await page.keyboard.press("Escape");

    await expect(
      directoryTable.locator("tbody tr").filter({ hasText: /Draft/i }).first(),
    ).toBeVisible();

    await directoryTable.locator("thead th").first().click();
    const widthsAfterSort = await readHeaderCellWidths(directoryTable);
    expectWidthsToStayStable(widthsBefore, widthsAfterSort);

    await page.getByTestId(selectors.tableSandboxToggleBanner).click();
    await expect(page.getByTestId(selectors.tableSandboxBanner)).toBeHidden();
    await expectDocumentNotToScroll(page);
    await expectTableToScroll(directoryTable);

    await page.getByTestId(selectors.tableSandboxTabArchive).click();
    const archiveTable = page.getByTestId(selectors.tableSandboxArchiveTable);
    await expect(archiveTable).toBeVisible();
    await expect(
      archiveTable.locator("tbody tr").filter({ hasText: /Archive Row 1/i }).first(),
    ).toBeVisible();
    await expectDocumentNotToScroll(page);
    await expectTableToScroll(archiveTable);

    await page.getByTestId(selectors.tableSandboxTabDirectory).click();
    await expect(directoryTable).toBeVisible();
    await expect(page.getByTestId(selectors.tableSandboxFilterSearch).locator("input")).toHaveValue(
      "Directory Row 12",
    );
  });

  test("preserves keep-alive state across route leave and return", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.goto(appPath(routes.tableSandbox), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.tableSandboxView)).toBeVisible();
    const table = page.getByTestId(selectors.tableSandboxDirectoryTable);
    await expect(
      table.locator("tbody tr").filter({ hasText: /Directory Row 1/i }).first(),
    ).toBeVisible({ timeout: 10000 });

    await fillAdaptiveFilterInput(page, selectors.tableSandboxFilterSearch, "Directory Row 18");
    await page.getByTestId(selectors.tableSandboxToggleBanner).click();
    await expect(page.getByTestId(selectors.tableSandboxBanner)).toBeHidden();
    await page.getByTestId(selectors.tableSandboxTabArchive).click();
    await expect(page.getByTestId(selectors.tableSandboxArchiveTable)).toBeVisible();

    await page.getByTestId(selectors.tableSandboxGoEcho).click();
    await expect(page.getByTestId(selectors.tableSandboxEchoView)).toBeVisible();
    await expectDocumentToScroll(page);

    await page.getByTestId(selectors.tableSandboxEchoBack).click();
    await expect(page.getByTestId(selectors.tableSandboxView)).toBeVisible();
    await expectDocumentNotToScroll(page);

    await expect(page.getByTestId(selectors.tableSandboxFilterSearch).locator("input")).toHaveValue(
      "Directory Row 18",
    );
    await expect(page.getByTestId(selectors.tableSandboxBanner)).toBeHidden();
    await expect(page.getByTestId(selectors.tableSandboxArchiveTable)).toBeVisible();
    await expectTableToScroll(page.getByTestId(selectors.tableSandboxArchiveTable));

    await page.getByTestId(selectors.tableSandboxTabDirectory).click();
    await expect(table.locator("tbody tr").filter({ hasText: /Directory Row 18/i }).first()).toBeVisible();
  });
});
