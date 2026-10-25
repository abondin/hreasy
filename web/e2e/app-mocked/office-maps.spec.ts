import { expect, test, type Page } from "@playwright/test";
import { installUnhandledApiGuard, mockAppRouteAuth } from "../support/app-mocked-api";
import { appPath } from "../support/navigation";

test.use({ headless: true, viewport: { width: 1600, height: 1000 } });

const rooms = [
  { id: 11, officeId: 1, name: "Room A", mapName: "wide.svg" },
  { id: 12, officeId: 1, name: "Room B", mapName: "wide.svg" },
  { id: 13, officeId: 1, name: "Room C", mapName: null },
  { id: 14, officeId: 1, name: "Room D", mapName: "tall.svg" },
];
const employees = [
  { id: 1, displayName: "Alex Morgan", officeLocation: rooms[0], officeWorkplace: "A1" },
  { id: 2, displayName: "Jordan Lee", officeLocation: rooms[1], officeWorkplace: "A2" },
  { id: 3, displayName: "Casey Taylor", officeLocation: rooms[0], officeWorkplace: "A3" },
  { id: 4, displayName: "Robin Lane", officeLocation: rooms[2], officeWorkplace: " " },
  { id: 5, displayName: "Taylor Reed", officeLocation: rooms[3], officeWorkplace: "A1" },
];
function plan(width: number, height: number) {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">
    <rect id="plan-border" x="1" y="1" width="${width - 2}" height="${height - 2}" fill="white" stroke="gray"/>
    ${["A1", "A2", "A3", "A4"].map((name, index) => `<g>
      <g id="seat-${name}" data-workplaceName="${name}" data-workplaceType="1">
        <rect x="20" y="${20 + index * 100}" width="240" height="80" rx="8" fill="#dbeafe" stroke="#93b4dc"/>
        <g data-empl=""><text x="30" y="${45 + index * 100}" font-size="14">\${empl}</text></g>
        <text x="30" y="${75 + index * 100}" font-size="18">${name}</text>
      </g></g>`).join("")}
    </svg>`;
}
async function setup(page: Page) {
  await installUnhandledApiGuard(page);
  await mockAppRouteAuth(page, []);
  const requests = { employees: 0 };
  await page.route("**/api/v1/employee", (route) => {
    requests.employees++;
    return route.fulfill({ json: employees });
  });
  await page.route("**/api/v1/employee/*", (route) =>
    route.fulfill({ json: employees.find((employee) => String(employee.id) === route.request().url().split("/").pop()) }));
  await page.route("**/api/v1/dict/offices", (route) => route.fulfill({ json: [{ id: 1, name: "North Office", primaryOffice: false }] }));
  await page.route("**/api/v1/dict/office_locations", (route) => route.fulfill({ json: rooms }));
  await page.route("**/api/v1/dict/office_maps/*", (route) =>
    route.fulfill({ contentType: "image/svg+xml", body: route.request().url().endsWith("tall.svg") ? plan(400, 1200) : plan(1200, 600) }));
  await page.goto(appPath("/office-maps"));
  await expect(page.getByTestId("office-maps-employee-1")).toBeVisible();
  return requests;
}
async function choose(page: Page, field: string, name: string) {
  await page.getByTestId(field).locator("input").fill(name);
  await page.getByRole("option", { name, exact: true }).click();
}

test("shared map, search, clears, details, viewport and one employee snapshot", async ({ page }) => {
  const requests = await setup(page);
  const search = page.getByTestId("office-maps-search").locator("input");
  await page.getByTestId("office-maps-search").screenshot({ path: "test-results/search-settings.png" });
  await page.getByRole("button", { name: "Настройки поиска" }).click();
  await expect(page.getByText("Неточный поиск", { exact: true })).toBeVisible();
  await page.keyboard.press("Escape");
  await page.getByTestId("office-maps-employee-1").focus();
  await page.getByTestId("office-maps-employee-1").press("Enter");
  await expect(page.locator("#seat-A1 rect")).toHaveCSS("filter", /drop-shadow/);
  await expect.poll(async () => {
    const bounds = await page.locator("#plan-border").boundingBox();
    const viewport = await page.locator(".office-map-svg").boundingBox();
    return Math.abs(bounds!.y - viewport!.y);
  }).toBeLessThan(8);
  await page.screenshot({ path: "test-results/office-maps-wide.png" });

  const initialTransform = await page.locator(".svg-pan-zoom_viewport").getAttribute("transform");
  await page.locator("#svg-pan-zoom-zoom-in").click();
  await expect(page.locator(".svg-pan-zoom_viewport")).not.toHaveAttribute("transform", initialTransform!);
  const transform = await page.locator(".svg-pan-zoom_viewport").getAttribute("transform");
  await page.getByTestId("office-maps-employee-3").click();
  await expect(page.locator(".svg-pan-zoom_viewport")).toHaveAttribute("transform", transform!);
  await expect(page.locator("#seat-A3 rect")).toHaveCSS("filter", /drop-shadow/);
  await expect(page.locator("#seat-A1 rect")).toHaveCSS("filter", "none");

  await choose(page, "office-maps-office", "North Office");
  await choose(page, "office-maps-room", "Room A");
  await expect(page.getByTestId("office-maps-scope")).toHaveText("Room A");
  await expect(page.locator("#seat-A2")).toHaveAttribute("aria-disabled", "true");
  await search.fill("Alex");
  await page.locator("#seat-A2 text").last().click();
  await expect(search).toHaveValue("Alex");
  await page.locator("#seat-A3 rect").click();
  await expect(search).toHaveValue("");
  await expect(page.getByTestId("office-maps-employee-3")).toBeVisible();
  await expect(page.getByTestId("office-maps-employee-1")).toHaveCount(0);
  await page.getByTestId("office-maps-employee-3").click();
  await page.getByTestId("office-maps-sidebar").locator(".v-chip__close").click();
  await expect(page.getByTestId("office-maps-employee-1")).toBeVisible();
  await expect(page.locator("#seat-A3 rect")).toHaveCSS("filter", "none");

  const details = page.getByTestId("office-maps-employee-1").getByRole("button", { name: "Карточка сотрудника" });
  await details.focus();
  await expect(details).toHaveCSS("opacity", "1");
  await details.click();
  await expect(page.getByRole("dialog")).toContainText("Alex Morgan");
  await page.getByRole("dialog").getByRole("button", { name: "Закрыть" }).click();

  await page.locator("#seat-A4 rect").click();
  await expect(page.getByTestId("office-maps-sidebar")).toContainText("За этим местом сотрудники не закреплены");
  await page.getByTestId("office-maps-room").locator(".v-field__clearable").click();
  await expect(page.locator(".office-map-svg")).toHaveCount(0);
  await expect(page.getByTestId("office-maps-scope")).toHaveText("North Office");
  await choose(page, "office-maps-room", "Room C");
  await expect(page.getByTestId("office-maps-view")).toContainText("Для этого кабинета карта не загружена");
  await expect(page.getByTestId("office-maps-employee-4")).toContainText("Рабочее место не указано");
  await page.getByTestId("office-maps-office").locator(".v-field__clearable").click();
  await expect(page.getByTestId("office-maps-scope")).toHaveText("Все сотрудники");
  expect(requests.employees).toBe(1);
});

test("map retry, tall plan and narrow-screen details", async ({ page }) => {
  await setup(page);
  let attempts = 0;
  await page.route("**/api/v1/dict/office_maps/tall.svg", (route) => {
    attempts++;
    return attempts === 1
      ? route.fulfill({ status: 500, json: { message: "Map temporarily unavailable" } })
      : route.fulfill({ contentType: "image/svg+xml", body: plan(400, 1200) });
  });
  await page.getByTestId("office-maps-employee-5").click();
  await page.getByRole("button", { name: "Повторить", exact: true }).click();
  await expect(page.locator("#plan-border")).toBeVisible();
  const bounds = await page.locator("#plan-border").boundingBox();
  const viewport = await page.locator(".office-map-svg").boundingBox();
  expect(Math.abs(bounds!.y - viewport!.y)).toBeLessThan(8);
  await page.screenshot({ path: "test-results/office-maps-tall.png" });
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.getByTestId("office-maps-sidebar")).toBeVisible();
  await page.screenshot({ path: "test-results/office-maps-mobile.png" });
  const details = page.getByTestId("office-maps-employee-5").getByRole("button", { name: "Карточка сотрудника" });
  await details.scrollIntoViewIfNeeded();
  await expect(details).toHaveCSS("opacity", "1");
  await details.click();
  await expect(page.getByRole("dialog")).toContainText("Taylor Reed");
});

test("admin primary-office checkbox controls the initial maps filter", async ({ page }) => {
  await setup(page);
  await mockAppRouteAuth(page, ["admin_office"]);
  let office = { id: 1, name: "North Office", archived: false, primaryOffice: false, address: "", description: "" };
  await page.route("**/api/v1/admin/dict/offices", (route) => route.fulfill({ json: [office] }));
  await page.route("**/api/v1/admin/dict/offices/1", (route) => {
    office = { ...office, ...route.request().postDataJSON() };
    return route.fulfill({ json: 1 });
  });
  await page.route("**/api/v1/dict/offices", (route) => route.fulfill({ json: [office] }));
  for (const primary of [true, false]) {
    await page.goto(appPath("/admin/dicts/offices"));
    await page.getByRole("row").filter({ hasText: "North Office" }).click();
    const dialog = page.getByRole("dialog");
    await dialog.getByRole("checkbox", { name: "Основной офис", exact: true }).setChecked(primary);
    await dialog.getByRole("button", { name: "Сохранить", exact: true }).click();
    await expect(dialog).toBeHidden();
    expect(office.primaryOffice).toBe(primary);
    await page.goto(appPath("/office-maps"));
    await expect(page.getByTestId("office-maps-scope")).toHaveText(primary ? "North Office" : "Все сотрудники");
    await expect(page.getByTestId("office-maps-office").locator('input[type="text"]')).toHaveValue(primary ? "North Office" : "");
    await expect(page.getByTestId("office-maps-room").locator('input[type="text"]')).toHaveValue("");
  }
});
