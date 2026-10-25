import { flushPromises, shallowMount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import OfficeMap from "@/components/office-map/OfficeMap.vue";
import { fetchOfficeMapSvg } from "@/services/office-map.service";
import { highlightWorkplaces, initializeWorkplaces } from "@/components/office-map/workplaceOnMap";

const panZoom = vi.hoisted(() => ({
  resize: vi.fn(), fit: vi.fn(), center: vi.fn(), destroy: vi.fn(),
  getSizes: () => ({ viewBox: { width: 200, height: 100 } }),
}));
vi.mock("vue-i18n", () => ({ useI18n: () => ({ t: (key: string) => key }) }));
vi.mock("@/services/office-map.service", () => ({ fetchOfficeMapSvg: vi.fn() }));
vi.mock("@/components/office-map/workplaceOnMap", () => ({
  initializeWorkplaces: vi.fn(), highlightWorkplaces: vi.fn(),
}));
vi.mock("svg-pan-zoom", () => ({ default: () => panZoom }));

const data = (id: string) => `<rect id="${id}" width="200" height="100" />`;
const employee = { id: 1, displayName: "Alex Morgan", officeLocation: { id: 11, name: "Room A", mapName: "north.svg" } };
const otherEmployee = { id: 2, displayName: "Jordan Lee", officeLocation: { id: 12, name: "Room B", mapName: "south.svg" } };
function mountMap(workplace?: string) {
  return shallowMount(OfficeMap, {
    props: { mapName: "north.svg", workplace, employees: [employee, otherEmployee] },
    global: { renderStubDefaultSlot: true, stubs: { VAlert: { template: "<div><slot/><slot name='append'/></div>" } } },
  });
}

describe("OfficeMap", () => {
  beforeEach(() => vi.clearAllMocks());

  it("updates highlights without rebuilding the map or reloading employees", async () => {
    vi.mocked(fetchOfficeMapSvg).mockResolvedValue(data("north"));
    const wrapper = mountMap("desk-1");
    await flushPromises();
    const svg = wrapper.get("svg").element;
    expect(initializeWorkplaces).toHaveBeenCalledWith(svg, expect.any(Function), [employee], ["desk-1"], undefined);
    expect(wrapper.getComponent({ name: "VResponsive" }).props("aspectRatio")).toBe(2);
    const click = vi.mocked(initializeWorkplaces).mock.calls[0]![1]!;
    click("desk-1", employee);
    click("desk-2");
    expect(wrapper.emitted("employee-select")).toEqual([[employee]]);
    expect(wrapper.emitted("workplace-select")).toEqual([["desk-1"], ["desk-2"]]);
    await wrapper.setProps({ workplace: "desk-2" });
    expect(wrapper.get("svg").element).toBe(svg);
    expect(highlightWorkplaces).toHaveBeenLastCalledWith(svg, ["desk-2"]);
    await wrapper.setProps({ workplace: null });
    expect(highlightWorkplaces).toHaveBeenLastCalledWith(svg, []);
    expect(initializeWorkplaces).toHaveBeenCalledTimes(1);
    expect(panZoom.destroy).not.toHaveBeenCalled();
    expect(fetchOfficeMapSvg).toHaveBeenCalledTimes(1);
    await wrapper.setProps({ locationId: 11 });
    expect(initializeWorkplaces).toHaveBeenLastCalledWith(expect.anything(), expect.any(Function), [employee], [], 11);
    await wrapper.setProps({ locationId: null });
    expect(initializeWorkplaces).toHaveBeenLastCalledWith(expect.anything(), expect.any(Function), [employee], [], null);
    wrapper.unmount();
    expect(panZoom.destroy).toHaveBeenCalled();
  });

  it("ignores a previous map response after selection changes", async () => {
    let resolvePrevious!: (value: string) => void;
    vi.mocked(fetchOfficeMapSvg)
      .mockReturnValueOnce(new Promise((resolve) => { resolvePrevious = resolve; }))
      .mockResolvedValueOnce(data("south"));
    const wrapper = mountMap();
    await wrapper.setProps({ mapName: "south.svg" });
    await flushPromises();
    resolvePrevious(data("north"));
    await flushPromises();
    expect(wrapper.find("#south").exists()).toBe(true);
    expect(wrapper.find("#north").exists()).toBe(false);
    wrapper.unmount();
  });

  it("retries a failed map load", async () => {
    vi.mocked(fetchOfficeMapSvg)
      .mockRejectedValueOnce(new Error("Map unavailable"))
      .mockResolvedValueOnce(data("north"));
    const wrapper = mountMap();
    await flushPromises();
    expect(wrapper.text()).toContain("Map unavailable");
    await wrapper.get("v-btn-stub").trigger("click");
    await flushPromises();
    expect(wrapper.find("#north").exists()).toBe(true);
    expect(wrapper.text()).not.toContain("Map unavailable");
    wrapper.unmount();
  });
});
