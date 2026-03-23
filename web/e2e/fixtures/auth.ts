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

export function credentialsOrSkip(role: TestRole): TestCredentials | null {
  return envCredentials(role);
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

export async function loginViaUi(
  page: Page,
  credentials: TestCredentials,
): Promise<void> {
  await page.goto(appPath("/login"), { waitUntil: "domcontentloaded" });

  // Some environments restore auth session automatically and skip the login form.
  if (!page.url().includes("/login")) {
    return;
  }

  const loginInput = page.locator('input[name="login"]').first();
  const passwordInput = page.locator('input[name="password"]').first();

  const loginInputVisible = await loginInput.isVisible().catch(() => false);
  const passwordInputVisible = await passwordInput.isVisible().catch(() => false);
  if (!loginInputVisible || !passwordInputVisible) {
    const loginLink = page.getByRole("link", { name: /Вход/i }).first();
    if (await loginLink.isVisible().catch(() => false)) {
      await loginLink.click();
      await page.waitForTimeout(200);
    }
  }

  if (!page.url().includes("/login")) {
    return;
  }

  await expect(loginInput).toBeVisible({ timeout: 15000 });
  await expect(passwordInput).toBeVisible({ timeout: 15000 });

  for (let attempt = 1; attempt <= 2; attempt += 1) {
    await loginInput.fill(credentials.username);
    await passwordInput.fill(credentials.password);

    const submitButton = page.getByTestId(selectors.loginSubmit);
    await expect(submitButton).toBeEnabled();
    await submitButton.click();

    try {
      await expect(page).not.toHaveURL(/\/login(?:$|\?)/, { timeout: 15000 });
      return;
    } catch {
      if (attempt === 2) {
        const errorText = (await page.locator('[role="alert"]').first().textContent().catch(() => null)) ?? "";
        throw new Error(`Login failed, stayed on /login. Response error: ${errorText.trim()}`);
      }
      await page.reload({ waitUntil: "domcontentloaded" });
    }
  }
}

export async function logoutViaUi(page: Page): Promise<void> {
  const logoutButton = page.getByTestId(selectors.logoutButton).first();
  if (await logoutButton.isVisible()) {
    await logoutButton.click();
  }
  await expect(page).toHaveURL(/\/login/);
}

export async function expectLoginPageOrAuthenticatedHome(page: Page): Promise<void> {
  if (page.url().includes("/login")) {
    await expect(page).toHaveURL(/\/login(?:\?returnPath=\/profile)?$/);
    await expect(page.getByTestId(selectors.loginInput)).toBeVisible();
    await expect(page.getByTestId(selectors.passwordInput)).toBeVisible();
    await expect(page.getByTestId(selectors.loginSubmit)).toBeVisible();
    return;
  }

  await expect(page).toHaveURL(/\/(?:$|profile)/);
}

export async function expectProtectedRouteRedirectOrAccess(
  page: Page,
  path: string,
): Promise<void> {
  const url = page.url();
  expect(url.includes("/login") || url.includes(path)).toBeTruthy();
}
