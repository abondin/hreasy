import type { Page, Route } from "@playwright/test";

interface MockAuthenticatedUserOptions {
  authorities?: string[];
  employeeId?: number;
  username?: string;
  accessibleBas?: number[];
}

function jsonResponse(route: Route, status: number, body: unknown): Promise<void> {
  return route.fulfill({
    status,
    contentType: "application/json",
    body: JSON.stringify(body),
  });
}

export async function mockAuthenticatedUser(
  page: Page,
  options: MockAuthenticatedUserOptions = {},
): Promise<void> {
  const user = {
    username: options.username ?? "e2e.user",
    email: "e2e.user@example.test",
    authorities: options.authorities ?? [],
    employee: {
      employeeId: options.employeeId ?? 101,
      accessibleBas: options.accessibleBas ?? [200, 201, 202, 203],
    },
  };

  await page.route("**/api/v1/current-user", async (route) => {
    await jsonResponse(route, 200, user);
  });

  await page.route("**/api/v1/login", async (route) => {
    await jsonResponse(route, 200, { currentUser: user });
  });

  await page.route("**/api/v1/logout", async (route) => {
    await jsonResponse(route, 200, {});
  });
}
