import { expect, type Page } from "@playwright/test";
import type { TestCredentials, TestRole } from "./users";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";

function envCredentials(role: TestRole): TestCredentials | null {
  const rolePrefix = `E2E_${role.toUpperCase()}`.replaceAll("-", "_");
  const roleUsername = process.env[`${rolePrefix}_USERNAME`];
  const rolePassword = process.env[`${rolePrefix}_PASSWORD`];

  if (roleUsername && rolePassword) {
    return { username: roleUsername, password: rolePassword };
  }

  if (role !== "employee") {
    const fallbackUsername = process.env.E2E_EMPLOYEE_USERNAME;
    const fallbackPassword = process.env.E2E_EMPLOYEE_PASSWORD;
    if (fallbackUsername && fallbackPassword) {
      return { username: fallbackUsername, password: fallbackPassword };
    }
  }

  return null;
}

export function requireCredentials(...roles: TestRole[]): TestCredentials {
  for (const role of roles) {
    const credentials = envCredentials(role);
    if (credentials) {
      return credentials;
    }
  }

  throw new Error(
    `Missing E2E credentials for roles: ${roles.join(", ")}. Set matching E2E_*_USERNAME and E2E_*_PASSWORD variables.`,
  );
}

export async function loginViaApi(
  page: Page,
  credentials: TestCredentials,
): Promise<void> {
  const response = await page.request.post("/api/v1/login", { data: credentials });
  await expect(response).toBeOK();
}

export async function loginViaUi(
  page: Page,
  credentials: TestCredentials,
): Promise<void> {
  await page.goto(appPath("/login"));
  const loginInput = page.getByTestId(selectors.loginInput).locator("input");
  const passwordInput = page.getByTestId(selectors.passwordInput).locator("input");

  await loginInput.fill(credentials.username);
  await passwordInput.fill(credentials.password);

  const submitButton = page.getByTestId(selectors.loginSubmit);
  await expect(submitButton).toBeEnabled();
  const response = page.waitForResponse(response =>
    response.request().method() === "POST" && new URL(response.url()).pathname === "/api/v1/login",
  );
  await submitButton.click();
  expect((await response).status()).toBe(200);
  await expect(page).toHaveURL(/\/profile(?:$|\?)/);
}
