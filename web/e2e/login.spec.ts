import { test } from "@playwright/test";
import { expectLoginPageOrAuthenticatedHome } from "./fixtures/auth";
import { appPath } from "./support/navigation";

test("redirects unauthenticated user to login form", async ({ page }) => {
  await page.goto(appPath("/"), { waitUntil: "domcontentloaded" });
  await expectLoginPageOrAuthenticatedHome(page);
});
