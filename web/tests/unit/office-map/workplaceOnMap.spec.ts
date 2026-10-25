import { describe, expect, it, vi } from "vitest";
import { highlightWorkplaces, initializeWorkplaces } from "@/components/office-map/workplaceOnMap";

describe("workplace room scope", () => {
  it.each([null, 11, 12])("only enables employees in room %s, keeping empty places selectable", (locationId) => {
    const container = document.createElement("div");
    container.innerHTML = `<svg>${["a1", "a2", "a3"].map((name) => `
      <g><g data-workplaceName="${name}" data-workplaceType="1"><rect /></g>
      <g data-empl><text>\${empl}</text></g></g>`).join("")}</svg>`;
    const svg = container.querySelector("svg")!;
    // Happy DOM preserves SVG attribute casing instead of normalizing the dataset key.
    svg.querySelectorAll<SVGElement>("[data-workplaceName]").forEach((place) => {
      place.dataset.workplacename = place.getAttribute("data-workplaceName")!;
    });
    svg.querySelectorAll("*").forEach((element) => {
      Object.defineProperty(element, "getBBox", { value: () => ({ width: 100 }) });
    });
    const employees = [
      { id: 1, displayName: "Alex Morgan", officeWorkplace: "A1", officeLocation: { id: 11, name: "Room A" } },
      { id: 2, displayName: "Jordan Lee", officeWorkplace: "A2", officeLocation: { id: 12, name: "Room B" } },
    ];
    const click = vi.fn();
    initializeWorkplaces(svg, click, employees, ["a1", "a2"], locationId);
    const places = svg.querySelectorAll<SVGElement>("[data-workplaceName]");
    places.forEach((place, index) => {
      const disabled = locationId != null && index < 2 && employees[index]!.officeLocation.id !== locationId;
      expect(place.getAttribute("aria-disabled")).toBe(disabled ? "true" : null);
      place.dispatchEvent(new MouseEvent("mouseenter"));
      expect(place.style.strokeWidth).toBe(disabled ? "" : "3px");
      place.dispatchEvent(new MouseEvent("click"));
      expect(click).toHaveBeenCalledTimes(disabled ? 0 : 1);
      expect(place.style.opacity).toBe(disabled ? "0.35" : "");
      expect(Boolean(place.querySelector("rect")!.style.filter)).toBe(!disabled && index < 2);
      click.mockClear();
    });
    highlightWorkplaces(svg, []);
    expect([...svg.querySelectorAll("rect")].every((rect) => rect.style.filter === "")).toBe(true);
    highlightWorkplaces(svg, [" A1 "]);
    expect(Boolean(places[0]!.querySelector("rect")!.style.filter)).toBe(locationId !== 12);
    expect(places[1]!.querySelector("rect")!.style.filter).toBe("");
  });
});
