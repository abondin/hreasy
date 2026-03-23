import { expect, type Locator } from "@playwright/test";

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
