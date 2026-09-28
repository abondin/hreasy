import { expect, test } from "@playwright/test";
import {
  appMockedAuthorities,
  appMockedEmployees,
  installUnhandledApiGuard,
  mockAppRouteAuth,
  mockCurrentOrFutureVacationsApi,
  mockEmployeeDetailsApi,
} from "../support/app-mocked-api";
import { appPath } from "../support/navigation";

test("shows current project employees and opens the shared employee details dialog", async ({ page }) => {
  await installUnhandledApiGuard(page);
  await mockAppRouteAuth(page, [...appMockedAuthorities.employees, "project_admin_area", "update_current_project_global"]);
  await mockEmployeeDetailsApi(page);
  await mockCurrentOrFutureVacationsApi(page);
  const directory = structuredClone(appMockedEmployees.slice(0, 4));
  await page.route("**/api/v1/employee", route => route.fulfill({ json: directory }));
  await page.route("**/api/v1/employee/101", route => route.fulfill({ json: directory[0] }));
  await page.route("**/api/v1/employee/101/currentProject/transferRequests/active", route =>
    route.fulfill({ json: null }),
  );
  await page.route("**/api/v1/employee/current_project_roles", route => route.fulfill({ json: [] }));
  await page.route("**/api/v1/employee/101/currentProject", async route => {
    const assignment = route.request().postDataJSON();
    directory[0]!.currentProject = { ...assignment, name: assignment.id === 301 ? "Retail Terminal Platform" : "Billing Gateway" };
    await route.fulfill({ json: 101 });
  });
  await page.route("**/api/v1/admin/projects/301", route => route.fulfill({
    json: { id: 301, name: "Retail Terminal Platform", active: true, workstreams: [] },
  }));
  await page.route(/\/api\/v1\/(?:dict\/(?:departments|projects)|business_account|admin\/managers\/object\/project\/301)$/, route =>
    route.fulfill({ json: [] }),
  );
  await page.route("**/api/v1/dict/projects", route => route.fulfill({ json: [
    { id: 301, name: "Retail Terminal Platform", active: true },
    { id: 303, name: "Billing Gateway", active: true },
  ] }));
  await page.route("**/api/v1/admin/managers/object/project/301", route => route.fulfill({ json: [
    { id: 1, employee: { id: 101, name: "Alex Morgan", active: true }, responsibilityType: "technical" },
    { id: 2, employee: { id: 999, name: "Former Manager", active: false }, responsibilityType: "organization" },
  ] }));

  await page.goto(appPath("/admin/projects/301"));
  const managers = page.getByTestId("admin-project-managers");
  await expect(managers.getByRole("row").filter({ hasText: "Former Manager" })).toContainText("Уволен");
  await expect(managers.getByRole("row").filter({ hasText: "Alex Morgan" })).not.toContainText("Уволен");
  const employees = page.getByTestId("admin-project-employees");
  await expect(employees.getByRole("row").filter({ hasText: /Alex Morgan|Riley Brooks/ })).toHaveCount(2);
  await expect(employees).toContainText("Alex Morgan");
  await expect(employees).toContainText("Riley Brooks");
  await expect(employees).not.toContainText("Jordan Lee");
  await expect(employees).not.toContainText("Casey Taylor");
  await expect(employees).toContainText("Backend C#");
  await expect(employees).toContainText("Software Development");
  await expect(employees).toContainText(appMockedEmployees[0]!.email);

  const search = page.getByTestId("admin-project-employees-search").locator("input");
  await search.fill("Morgan Backend");
  await expect(employees).toContainText("Alex Morgan");
  await expect(employees).not.toContainText("Riley Brooks");
  await search.fill("Jordan");
  await expect(employees.getByRole("row").filter({ hasText: "Jordan Lee" })).toHaveCount(0);
  await search.fill("Morgan");

  await employees.getByRole("row").filter({ hasText: "Alex Morgan" }).click();
  const dialog = page.getByTestId("employee-details-dialog");
  await expect(dialog).toBeVisible();
  await expect(dialog).toContainText("Alex Morgan");
  await expect(dialog).toContainText(appMockedEmployees[0]!.email);
  await dialog.getByTestId("profile-summary-edit-project").click();
  await page.getByTestId("project-assignment-role").locator('input[type="text"]').fill("Lead Engineer");
  await page.getByTestId("project-assignment-role").locator('input[type="text"]').press("Tab");
  await page.getByTestId("project-assignment-submit").click();
  await expect(employees).toContainText("Lead Engineer");
  await expect(search).toHaveValue("Morgan");
  await dialog.getByTestId("profile-summary-edit-project").click();
  await page.getByTestId("project-assignment-project").locator('input[type="text"]').fill("Billing Gateway");
  await page.getByRole("option", { name: "Billing Gateway", exact: true }).click();
  await page.getByTestId("project-assignment-submit").click();
  await expect(employees).not.toContainText("Alex Morgan");
  await dialog.getByRole("button", { name: "Закрыть", exact: true }).click();
  await expect(dialog).toBeHidden();
  await expect(page).toHaveURL(/\/admin\/projects\/301$/);
  await search.fill("");
  await employees.getByRole("row").filter({ hasText: "Riley Brooks" }).click();
  await expect(dialog).toContainText("Riley Brooks");
  await expect(dialog).not.toContainText("Alex Morgan");
});
