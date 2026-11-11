import { expect, test, type Page } from "@playwright/test";
import { mockAuthenticatedUser } from "../support/auth-mocks.js";
import { installUnhandledApiGuard } from "../support/app-mocked-api.js";
import { appPath } from "../support/navigation.js";
import { selectors } from "../support/selectors.js";

async function mockEmployees(page: Page, additionalEmails: string[] = []) {
  let employee = {
    id: 101,
    displayName: "Alex Morgan",
    email: "alex.morgan@example.test",
    additionalEmails,
    active: true,
    currentProjectRole: null,
    hasAvatar: false,
  };
  await installUnhandledApiGuard(page);
  await mockAuthenticatedUser(page, { authorities: ["view_employee_full"] });
  await page.route("**/api/v1/dict/**", route => route.fulfill({ json: [] }));
  await page.route("**/api/v1/business_account", route => route.fulfill({ json: [] }));
  await page.route("**/api/v1/admin/employees?*", route => route.fulfill({ json: [employee, {
    id: 102, displayName: "Jordan Lee", email: "jordan.lee@example.test",
    additionalEmails: ["other.name@example.test"], active: false,
  }] }));
  await page.route("**/api/v1/admin/employees/101", async route => {
    employee = { ...employee, ...route.request().postDataJSON() };
    await route.fulfill({ json: 101 });
  });
  await page.route("**/api/v1/employee", route => route.fulfill({ json: [employee, {
    id: 102, displayName: "Jordan Lee", email: "jordan.lee@example.test", additionalEmails: [],
  }] }));
  await page.route("**/api/v1/employee/101", route => route.fulfill({ json: employee }));
}

test("admin saves and removes additional emails without changing the primary address", async ({ page }) => {
  await mockEmployees(page);
  await page.goto(appPath("/admin/employees/list"));
  const row = page.locator("tbody tr").filter({ hasText: "Alex Morgan" });
  await row.locator("td").first().click();
  const dialog = page.getByTestId(selectors.adminEmployeesEditDialog);
  await expect(dialog.getByRole("textbox", { name: "ФИО", exact: true })).toBeFocused();
  await expect(dialog.getByTestId(selectors.adminEmployeeEmail).locator("input")).toHaveAttribute("readonly", "");
  await dialog.getByTestId(selectors.adminEmployeeAddEmail).click();
  const addressInput = dialog.getByTestId(selectors.adminEmployeeAdditionalEmail).locator("input");
  await expect(addressInput).toBeFocused();
  const save = dialog.getByRole("button", { name: "Изменить", exact: true });
  await addressInput.fill("not-an-email");
  await save.click();
  await expect(dialog.getByText("Укажите корректный email", { exact: true })).toBeVisible();
  await addressInput.fill("ALEX.MORGAN@example.test");
  await save.click();
  await expect(dialog.getByText("Почтовые адреса не должны повторяться", { exact: true })).toBeVisible();
  for (const address of [" JORDAN.LEE@example.test ", "OTHER.NAME@example.test"]) {
    await addressInput.fill(address);
    await save.click();
    await expect(dialog.getByText("Почтовый адрес уже указан у сотрудника: Jordan Lee", { exact: true })).toBeVisible();
  }
  await addressInput.fill("new.name@example.test");
  const saved = page.waitForRequest(request => request.method() === "PUT" && request.url().endsWith("/admin/employees/101"));
  await save.click();
  expect((await saved).postDataJSON()).toMatchObject({
    email: "alex.morgan@example.test", additionalEmails: ["new.name@example.test"],
  });
  await expect(dialog).toBeHidden();
  await expect(row.getByTestId(selectors.employeeAdditionalEmails)).toHaveText("+1");
  await page.getByTestId(selectors.adminEmployeesSearch).locator("input").fill("new.name@example.test");
  await expect(row).toBeVisible();

  await row.locator("td").first().click();
  await expect(dialog.getByTestId(selectors.adminEmployeeAdditionalEmail).locator("input")).toHaveValue("new.name@example.test");
  await dialog.getByRole("button", { name: "Удалить адрес", exact: true }).click();
  const removed = page.waitForRequest(request => request.method() === "PUT" && request.url().endsWith("/admin/employees/101"));
  await save.click();
  expect((await removed).postDataJSON()).toMatchObject({ email: "alex.morgan@example.test", additionalEmails: [] });
  await expect(dialog).toBeHidden();
  await expect(row).toHaveCount(0);
  await page.getByTestId(selectors.adminEmployeesSearch).locator("input").clear();
  await expect(row).toBeVisible();
  await expect(row.getByTestId(selectors.employeeAdditionalEmails)).toHaveCount(0);
  await page.getByTestId(selectors.toolbarAdd).click();
  await dialog.getByRole("textbox", { name: "ФИО", exact: true }).fill("Casey Taylor");
  await dialog.getByTestId(selectors.adminEmployeeEmail).locator("input").fill("OTHER.NAME@example.test");
  await dialog.getByRole("button", { name: "Создать", exact: true }).click();
  await expect(dialog.getByText("Почтовый адрес уже указан у сотрудника: Jordan Lee", { exact: true })).toBeVisible();
});

test("an existing API duplicate does not prevent editing unrelated employee fields", async ({ page }) => {
  await mockEmployees(page, ["jordan.lee@example.test"]);
  await page.goto(appPath("/admin/employees/list"));
  await page.locator("tbody tr").filter({ hasText: "Alex Morgan" }).locator("td").first().click();
  const dialog = page.getByTestId(selectors.adminEmployeesEditDialog);
  await dialog.getByRole("textbox", { name: "ФИО", exact: true }).fill("Alex Updated");
  const saved = page.waitForRequest(request => request.method() === "PUT" && request.url().endsWith("/admin/employees/101"));
  await dialog.getByRole("button", { name: "Изменить", exact: true }).click();
  expect((await saved).postDataJSON()).toMatchObject({ displayName: "Alex Updated", additionalEmails: ["jordan.lee@example.test"] });
  await expect(dialog).toBeHidden();
});

test("directory searches additional emails and copies each address without opening the employee", async ({ page }) => {
  await page.addInitScript(() => {
    Object.defineProperty(navigator, "clipboard", { value: {
      writeText: async (value: string) => { sessionStorage.setItem("copied-email", value); },
    } });
  });
  await page.setViewportSize({ width: 1280, height: 720 });
  await mockEmployees(page, ["new.name@example.test", "second@example.test"]);
  await page.goto(appPath("/employees"));
  await page.getByTestId(selectors.employeesFilterSearch).locator("input").fill("NEW.NAME");
  const row = page.getByTestId(selectors.employeesTable).locator("tbody tr").filter({ hasText: "Alex Morgan" });
  await expect(row).toBeVisible();
  await expect(page.getByTestId(selectors.employeesTable).getByText("Jordan Lee")).toHaveCount(0);
  for (const width of [1280, 390]) {
    await page.setViewportSize({ width, height: 720 });
    await row.getByTestId(selectors.employeeAdditionalEmails).click();
    const list = page.getByTestId(selectors.employeeEmailsList);
    await expect(list).toBeVisible();
    const bounds = await list.boundingBox();
    expect(bounds!.x).toBeGreaterThanOrEqual(0);
    expect(bounds!.x + bounds!.width).toBeLessThanOrEqual(width);
    for (const address of ["alex.morgan@example.test", "new.name@example.test", "second@example.test"]) {
      await list.getByRole("button", { name: `Скопировать в буфер обмена: ${address}`, exact: true }).click();
      await expect.poll(() => page.evaluate(() => sessionStorage.getItem("copied-email"))).toBe(address);
    }
    await expect(page.getByRole("dialog")).toHaveCount(0);
    await page.keyboard.press("Escape");
    await expect(list).toBeHidden();
  }
});
