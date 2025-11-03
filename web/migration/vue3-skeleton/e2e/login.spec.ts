import { test, expect } from "@playwright/test";

test("redirects unauthenticated user to login form", async ({ page }) => {
  await page.goto("/");

  await expect(page).toHaveURL(/\/login(?:\?returnPath=\/profile)?$/);
  await expect(page.getByTestId("login-input")).toBeVisible();
  await expect(page.getByTestId("password-input")).toBeVisible();
  await expect(page.getByTestId("login-submit")).toBeVisible();
});
