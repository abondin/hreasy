import { expect, type Page } from "@playwright/test";
import type { TestCredentials, TestRole } from "./users";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";

function envCredentials(role: TestRole): TestCredentials | null {
  const prefix = `E2E_${role.toUpperCase()}`.replaceAll("-", "_");
  const username = process.env[`${prefix}_USERNAME`];
  const password = process.env[`${prefix}_PASSWORD`];
  if (!username || !password) {
    return null;
  }
  return { username, password };
}

export function credentialsOrSkip(role: TestRole): TestCredentials | null {
  return envCredentials(role);
}

export async function loginViaUi(
  page: Page,
  credentials: TestCredentials,
): Promise<void> {
  await page.goto(appPath("/login"));
  await page.getByTestId(selectors.loginInput).locator("input").fill(credentials.username);
  await page.getByTestId(selectors.passwordInput).locator("input").fill(credentials.password);
  await page.getByTestId(selectors.loginSubmit).click();
  await expect(page).not.toHaveURL(/\/login$/);
}

export async function logoutViaUi(page: Page): Promise<void> {
  const logoutButton = page.getByTestId(selectors.logoutButton).first();
  if (await logoutButton.isVisible()) {
    await logoutButton.click();
  }
  await expect(page).toHaveURL(/\/login/);
}

