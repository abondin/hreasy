import { test, expect } from "@playwright/test";
import { appPath } from "./support/navigation";

test("redirects unauthenticated user to login form", async ({ page }) => {
  await page.goto(appPath("/"));

  await expect(page).toHaveURL(/\/login(?:\?returnPath=\/profile)?$/);
  await expect(page.getByTestId("login-input")).toBeVisible();
  await expect(page.getByTestId("password-input")).toBeVisible();
  await expect(page.getByTestId("login-submit")).toBeVisible();
});
