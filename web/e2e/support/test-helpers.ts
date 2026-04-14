import { expect, type Locator, type Page } from "@playwright/test";

export async function expectVisibleRows(rows: Locator, message: string): Promise<number> {
  const rowCount = await rows.count();
  expect(rowCount, message).toBeGreaterThan(0);
  return rowCount;
}

export async function clickFirstRow(rows: Locator, emptyMessage: string): Promise<void> {
  await expectVisibleRows(rows, emptyMessage);
  await rows.first().locator("td").first().click();
}

interface OpenDialogFromRowsOptions {
  rows: Locator;
  dialog: Locator;
  emptyMessage: string;
  attempts?: number;
  addButton?: Locator;
}

export async function openDialogFromRowsOrAddButton({
  rows,
  dialog,
  emptyMessage,
  attempts = 3,
  addButton,
}: OpenDialogFromRowsOptions): Promise<void> {
  const rowCount = await expectVisibleRows(rows, emptyMessage);
  const maxAttempts = Math.min(rowCount, attempts);

  for (let index = 0; index < maxAttempts; index += 1) {
    await rows.nth(index).locator("td").first().click();
    if (await dialog.isVisible().catch(() => false)) {
      await expect(dialog).toBeVisible();
      return;
    }
  }

  if (addButton) {
    await expect(addButton).toBeVisible();
    await addButton.click();
  }

  await expect(dialog).toBeVisible();
}

export interface TableScrollMetrics {
  clientHeight: number;
  scrollHeight: number;
  scrollTop: number;
  overflowY: string;
}

export interface DocumentScrollMetrics {
  clientHeight: number;
  scrollHeight: number;
  scrollTop: number;
  overflowY: string;
}

export async function getTableScrollContainer(table: Locator): Promise<Locator> {
  const wrapper = table.locator(".v-table__wrapper").first();
  await expect(wrapper).toBeVisible();
  return wrapper;
}

export async function readTableScrollMetrics(table: Locator): Promise<TableScrollMetrics> {
  const wrapper = await getTableScrollContainer(table);
  return wrapper.evaluate((element) => {
    if (!(element instanceof HTMLElement)) {
      throw new Error("Table scroll wrapper is not an HTMLElement");
    }

    const { clientHeight, scrollHeight, scrollTop } = element;
    const overflowY = window.getComputedStyle(element).overflowY;

    return {
      clientHeight,
      scrollHeight,
      scrollTop,
      overflowY,
    };
  });
}

export async function expectTableToScroll(table: Locator): Promise<TableScrollMetrics> {
  const wrapper = await getTableScrollContainer(table);
  const before = await readTableScrollMetrics(table);

  expect(
    before.scrollHeight,
    `Expected table to overflow vertically, but got clientHeight=${before.clientHeight}, scrollHeight=${before.scrollHeight}, overflowY=${before.overflowY}`,
  ).toBeGreaterThan(before.clientHeight + 4);

  const { beforeScrollTop, afterScrollTop } = await wrapper.evaluate((element) => {
    if (!(element instanceof HTMLElement)) {
      throw new Error("Table scroll wrapper is not an HTMLElement");
    }

    element.scrollTop = 0;
    const beforeScrollTop = element.scrollTop;
    const nextScrollTop = Math.max(64, Math.round(element.clientHeight / 2));
    element.scrollTop = nextScrollTop;
    return {
      beforeScrollTop,
      afterScrollTop: element.scrollTop,
    };
  });

  expect(afterScrollTop, "Expected table scrollTop to increase after scrolling").toBeGreaterThan(
    beforeScrollTop,
  );

  return readTableScrollMetrics(table);
}

export async function readDocumentScrollMetrics(page: Page): Promise<DocumentScrollMetrics> {
  return page.evaluate(() => {
    const element = document.scrollingElement;
    if (!(element instanceof HTMLElement)) {
      throw new Error("Document scrolling element is not an HTMLElement");
    }

    return {
      clientHeight: element.clientHeight,
      scrollHeight: element.scrollHeight,
      scrollTop: element.scrollTop,
      overflowY: window.getComputedStyle(element).overflowY,
    };
  });
}

export async function expectDocumentNotToScroll(page: Page): Promise<DocumentScrollMetrics> {
  const metrics = await readDocumentScrollMetrics(page);
  expect(
    metrics.scrollHeight,
    `Expected page to fit viewport without browser scroll, but got clientHeight=${metrics.clientHeight}, scrollHeight=${metrics.scrollHeight}, overflowY=${metrics.overflowY}`,
  ).toBeLessThanOrEqual(metrics.clientHeight + 4);
  return metrics;
}

export async function expectDocumentToScroll(page: Page): Promise<DocumentScrollMetrics> {
  const metrics = await readDocumentScrollMetrics(page);
  expect(
    metrics.scrollHeight,
    `Expected browser page scroll, but got clientHeight=${metrics.clientHeight}, scrollHeight=${metrics.scrollHeight}, overflowY=${metrics.overflowY}`,
  ).toBeGreaterThan(metrics.clientHeight + 4);
  return metrics;
}

export async function readHeaderCellWidths(table: Locator, count = 4): Promise<number[]> {
  const widths = await table.locator("thead th").evaluateAll(
    (elements, maxCount) =>
      elements
        .slice(0, maxCount as number)
        .map((element) => Math.round(element.getBoundingClientRect().width)),
    count,
  );

  expect(widths.length, "Expected at least one visible table header cell").toBeGreaterThan(0);
  return widths;
}

export function expectWidthsToStayStable(
  before: number[],
  after: number[],
  tolerance = 6,
): void {
  expect(after.length).toBe(before.length);

  before.forEach((width, index) => {
    expect(
      Math.abs(width - after[index]),
      `Expected header width at index ${index} to stay within ${tolerance}px, before=${width}, after=${after[index]}`,
    ).toBeLessThanOrEqual(tolerance);
  });
}

export async function fillAdaptiveFilterInput(
  page: Page,
  testId: string,
  value: string,
): Promise<void> {
  const directInput = page.getByTestId(testId).locator("input").first();
  if (await directInput.isVisible().catch(() => false)) {
    await directInput.fill(value);
    await expect(directInput).toHaveValue(value);
    return;
  }

  await openAdaptiveFilterOverflow(page);

  const overflowInput = page.getByTestId(testId).locator("input").first();
  await expect(overflowInput).toBeVisible();
  await overflowInput.fill(value);
  await expect(overflowInput).toHaveValue(value);
  await page.keyboard.press("Escape");
}

export async function openAdaptiveFilterOverflow(page: Page): Promise<void> {
  const overflowButton = page.getByTestId("adaptive-filter-overflow").first();
  await expect(overflowButton).toBeVisible();
  await overflowButton.click();
}

export async function expectFilterToBeAccessible(page: Page, testId: string): Promise<void> {
  const directControl = page.getByTestId(testId).first();
  if (await directControl.isVisible().catch(() => false)) {
    await expect(directControl).toBeVisible();
    return;
  }

  await openAdaptiveFilterOverflow(page);
  await expect(page.getByTestId(testId).first()).toBeVisible();
  await page.keyboard.press("Escape");
}

export async function clearAutocompleteField(field: Locator): Promise<void> {
  await expect(field).toBeVisible();
  await field.click();

  const clearControl = field.locator(".v-field__clearable").first();
  if (await clearControl.isVisible().catch(() => false)) {
    await clearControl.click();
    return;
  }

  const input = field.locator("input").first();
  await input.focus();
  await input.press(process.platform === "win32" ? "Control+A" : "Meta+A");
  await input.press("Delete");
  await input.press("Backspace");
}
