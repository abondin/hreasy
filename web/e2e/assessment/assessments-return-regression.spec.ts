import { expect, test, type Page, type Route } from "@playwright/test";
import { mockAuthenticatedUser } from "../support/auth-mocks";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

type Dict = {
  id: number;
  name: string;
};

function dict(id: number, name: string): Dict {
  return { id, name };
}

function jsonResponse(route: Route, body: unknown, status = 200): Promise<void> {
  return route.fulfill({
    status,
    contentType: "application/json",
    body: JSON.stringify(body),
  });
}

async function mockAssessmentsWorkflow(page: Page): Promise<void> {
  let completedAt: string | null = null;
  let completedBy: Dict | null = null;

  const employee = {
    id: 101,
    displayName: "Alex Morgan",
    email: "alex.morgan@example.test",
    currentProject: { id: 301, name: "Retail Terminal Platform", role: "Developer" },
    ba: { id: 401, name: "Northwind Delivery" },
    department: { id: 501, name: "Software Development" },
    position: { id: 701, name: "Engineer" },
  };

  function summary() {
    return [{
      employeeId: employee.id,
      displayName: employee.displayName,
      lastAssessmentId: 9001,
      lastAssessmentDate: "2026-04-20",
      lastAssessmentCompletedDate: completedAt,
      employeeDateOfEmployment: "2024-01-15",
      latestActivity: completedAt,
      currentProject: employee.currentProject,
      ba: employee.ba,
      daysWithoutAssessment: completedAt ? 0 : 21,
    }];
  }

  function assessmentDetails() {
    return {
      id: 9001,
      plannedDate: "2026-04-20",
      createdAt: "2026-04-01T09:00:00Z",
      employee: dict(employee.id, employee.displayName),
      createdBy: dict(1, "Assessment Manager"),
      completedAt,
      completedBy,
      canceledAt: null,
      canceledBy: null,
      forms: [],
      attachmentsFilenames: [],
      attachmentsAccessToken: "token",
    };
  }

  await page.route("**/api/v1/business_account", async (route) => {
    await jsonResponse(route, [{ ...employee.ba, active: true }]);
  });

  await page.route("**/api/v1/dict/projects", async (route) => {
    await jsonResponse(route, [{ id: employee.currentProject.id, name: employee.currentProject.name, baId: 401, active: true }]);
  });

  await page.route("**/api/v1/employee", async (route) => {
    await jsonResponse(route, [employee]);
  });

  await page.route("**/api/v1/employee/*", async (route) => {
    await jsonResponse(route, employee);
  });

  await page.route("**/api/v1/assessment", async (route) => {
    await jsonResponse(route, summary());
  });

  await page.route(/\/api\/v1\/assessment\/\d+$/, async (route) => {
    await jsonResponse(route, [assessmentDetails()]);
  });

  await page.route(/\/api\/v1\/assessment\/\d+\/\d+$/, async (route) => {
    await jsonResponse(route, assessmentDetails());
  });

  await page.route(/\/api\/v1\/assessment\/\d+\/\d+\/complete$/, async (route) => {
    completedAt = "2026-04-30T12:00:00Z";
    completedBy = dict(2, "Assessment Lead");
    await jsonResponse(route, {});
  });
}

function assessmentsRows(page: Page) {
  return page.getByTestId(selectors.assessmentsTable).locator("tbody tr");
}

async function summaryRowText(page: Page, employeeName: string): Promise<string> {
  return (await assessmentsRows(page).filter({ hasText: employeeName }).first().textContent())?.trim() ?? "";
}

test.describe("Assessments return from details", () => {
  test("refreshes summary after completing an assessment in details", async ({ page }) => {
    await mockAuthenticatedUser(page, {
      authorities: ["create_assessment"],
    });
    await mockAssessmentsWorkflow(page);

    await page.goto(appPath(routes.assessments), { waitUntil: "domcontentloaded" });
    await expect(page.getByTestId(selectors.assessmentsView)).toBeVisible();
    await expect(assessmentsRows(page).filter({ hasText: "Alex Morgan" })).toBeVisible();
    await expect.poll(() => summaryRowText(page, "Alex Morgan")).not.toContain("30.04.2026");

    await assessmentsRows(page).filter({ hasText: "Alex Morgan" }).locator("td").first().click();
    await expect(page.getByTestId(selectors.employeeAssessmentsView)).toBeVisible();
    await page
      .getByTestId(selectors.employeeAssessmentsView)
      .locator("tbody a[href='/assessments/101/9001']")
      .click();
    await expect(page.getByTestId("assessment-details-view")).toBeVisible();

    await page.getByTestId("assessment-details-complete-open").click();
    await page.getByTestId("assessment-details-complete-submit").click();
    await expect(page.getByTestId("assessment-details-complete-open")).toBeDisabled();

    await page.goBack();
    await expect(page.getByTestId(selectors.employeeAssessmentsView)).toBeVisible();
    await page.goBack();
    await expect(page.getByTestId(selectors.assessmentsView)).toBeVisible();
    await expect.poll(() => summaryRowText(page, "Alex Morgan")).toContain("30.04.2026");
  });
});
