import { expect, test } from "@playwright/test";
import { loginViaUi, requireCredentials } from "../fixtures/auth";
import { appPath } from "../support/navigation";
import { selectors } from "../support/selectors";
import { routes } from "../support/test-data";

function isEmployeesListResponse(url: string): boolean {
  return /\/api\/v1\/employee(?:\?.*)?$/.test(url);
}

test.describe("Employees List Load Diagnostic", () => {
  test("captures state around the exact employees list response", async ({ page }) => {
    const credentials = requireCredentials("employee");
    const listResponses: Array<{ url: string; status: number }> = [];

    page.on("response", (response) => {
      const url = response.url();
      if (!isEmployeesListResponse(url) || response.request().method() !== "GET") {
        return;
      }

      listResponses.push({
        url,
        status: response.status(),
      });
    });

    await loginViaUi(page, credentials);
    await page.setViewportSize({ width: 1685, height: 1099 });
    await page.goto(appPath(routes.employees), { waitUntil: "domcontentloaded" });

    await expect(page.getByTestId(selectors.employeesView)).toBeVisible();

    await expect
      .poll(() => listResponses.some((response) => response.status === 200), {
        message: "Expected successful employees list response",
        timeout: 30000,
      })
      .toBeTruthy();

    const table = page.getByTestId(selectors.employeesTable);
    const stateAfterListResponse = await table.evaluate((element) => {
      const wrapper = element.querySelector(".v-table__wrapper");
      const rows = Array.from(element.querySelectorAll("tbody tr"));
      return {
        rowCount: rows.length,
        textRows: rows
          .map((row) => row.textContent?.trim() ?? "")
          .filter(Boolean)
          .slice(0, 5),
        clientHeight: wrapper instanceof HTMLElement ? wrapper.clientHeight : null,
        scrollHeight: wrapper instanceof HTMLElement ? wrapper.scrollHeight : null,
      };
    });

     
    console.log("LIST_RESPONSE_STATE", JSON.stringify(stateAfterListResponse));

    await expect
      .poll(async () => {
        const rows = await table.locator("tbody tr").count();
        return rows;
      }, {
        message: "Expected employees table to settle after the list response",
        timeout: 10000,
      })
      .toBeGreaterThan(0);

    const stateAfterNetworkIdle = await table.evaluate((element) => {
      const wrapper = element.querySelector(".v-table__wrapper");
      const rows = Array.from(element.querySelectorAll("tbody tr"));
      return {
        rowCount: rows.length,
        textRows: rows
          .map((row) => row.textContent?.trim() ?? "")
          .filter(Boolean)
          .slice(0, 5),
        clientHeight: wrapper instanceof HTMLElement ? wrapper.clientHeight : null,
        scrollHeight: wrapper instanceof HTMLElement ? wrapper.scrollHeight : null,
      };
    });

     
    console.log("NETWORK_IDLE_STATE", JSON.stringify(stateAfterNetworkIdle));

    await page.setViewportSize({ width: 1684, height: 1099 });

    const stateAfterResize = await table.evaluate((element) => {
      const wrapper = element.querySelector(".v-table__wrapper");
      const rows = Array.from(element.querySelectorAll("tbody tr"));
      return {
        rowCount: rows.length,
        textRows: rows
          .map((row) => row.textContent?.trim() ?? "")
          .filter(Boolean)
          .slice(0, 5),
        clientHeight: wrapper instanceof HTMLElement ? wrapper.clientHeight : null,
        scrollHeight: wrapper instanceof HTMLElement ? wrapper.scrollHeight : null,
      };
    });

     
    console.log("AFTER_RESIZE_STATE", JSON.stringify(stateAfterResize));

    expect(listResponses.some((response) => response.status === 200)).toBeTruthy();
  });
});
