import { existsSync } from "node:fs";
import { resolve } from "node:path";
import { expect, test, type Page } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

const importFile = resolve(process.cwd(), "../platform/src/test/resources/excel/employees-to-import.xlsx");
const importedEmployeeName = "Новый Сотрудник Сотрудникович";

const expectedDefaultColumns = {
  displayName: "B",
  externalErpId: "H",
  email: "P",
  phone: "AC",
  department: "AK",
  position: "AL",
  dateOfEmployment: "AM",
  dateOfDismissal: "AN",
  birthday: "AP",
  sex: "AQ",
  documentSeries: "AS",
  documentNumber: "AT",
  documentIssuedDate: "AU",
  documentIssuedBy: "AV",
  registrationAddress: "AX",
  organization: "BF",
} as const;

async function abortActiveImportProcess(page: Page): Promise<void> {
  const response = await page.request.post("/api/v1/admin/employees/import");
  if (!response.ok()) {
    return;
  }
  const workflow = await response.json() as { id?: number };
  if (typeof workflow.id === "number") {
    await page.request.post(`/api/v1/admin/employees/import/${workflow.id}/abort`);
  }
}

async function expectInputValue(page: Page, testId: string, value: string): Promise<void> {
  await expect(page.getByTestId(testId).locator("input")).toHaveValue(value);
}

test.describe("Admin Employees Import Full", () => {
  test("imports employee from Excel through real backend", async ({ page }) => {
    expect(existsSync(importFile), `Import file was not found: ${importFile}`).toBeTruthy();

    const credentials = requireCredentials("admin_employees");
    await loginViaUi(page, credentials);
    await abortActiveImportProcess(page);

    await page.goto(appPath(routes.adminEmployeesImport), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.adminEmployeesImportView)).toBeVisible();

    await page.locator("input[type='file']").setInputFiles(importFile);
    await expect(page.locator("[data-testid$='-close']")).toBeVisible();
    await page.locator("[data-testid$='-close']").click();

    await expect(page.getByTestId("import-config-sheet-number")).toBeVisible();
    await expectInputValue(page, "import-config-sheet-number", "1");
    await expectInputValue(page, "import-config-table-start-row", "11");
    for (const [column, value] of Object.entries(expectedDefaultColumns)) {
      await expectInputValue(page, `import-config-column-${column}`, value);
    }

    await page.getByTestId("import-workflow-action").click();
    const previewTable = page.getByTestId("import-preview-table");
    await expect(previewTable).toBeVisible();
    await page.getByTestId("import-preview-hide-unchanged").locator("input").setChecked(false);
    await expect(previewTable.locator("tbody tr").filter({ hasText: importedEmployeeName })).toBeVisible();

    await page.getByTestId("import-workflow-action").click();
    await expect(page.getByTestId("import-workflow-confirm-commit")).toBeVisible();
    await page.getByTestId("import-workflow-confirm-commit").click();

    await expect(page.getByTestId("import-workflow-action")).toBeVisible();
    await page.getByTestId("import-workflow-action").click();
    await expect(page.getByTestId(selectors.adminEmployeesListView)).toBeVisible();

    const search = page.getByTestId(selectors.adminEmployeesSearch).locator("input");
    await search.fill(importedEmployeeName);
    await expect(page.locator("tbody tr:visible").filter({ hasText: importedEmployeeName })).toBeVisible();
  });
});
