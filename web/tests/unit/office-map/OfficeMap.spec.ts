import { flushPromises, shallowMount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import OfficeMap from "@/components/office-map/OfficeMap.vue";
import { loadOfficeMapData, type OfficeMapData } from "@/services/office-map.service";
import { initializeWorkplaces } from "@/components/office-map/workplaceOnMap";

const panZoom = vi.hoisted(() => ({ resize: vi.fn(), fit: vi.fn(), center: vi.fn(), destroy: vi.fn() }));
vi.mock("vue-i18n", () => ({ useI18n: () => ({ t: (key: string) => key }) }));
vi.mock("@/services/office-map.service", () => ({ loadOfficeMapData: vi.fn() }));
vi.mock("@/components/office-map/workplaceOnMap", () => ({ initializeWorkplaces: vi.fn() }));
vi.mock("svg-pan-zoom", () => ({ default: () => panZoom }));

const data = (id: string): OfficeMapData => ({
  svg: `<rect id="${id}" width="100" height="100" />`, employees: [],
});

describe("OfficeMap", () => {
  beforeEach(() => vi.clearAllMocks());

  it("highlights the requested workplace, emits employee selection, and releases pan/zoom", async () => {
    vi.mocked(loadOfficeMapData).mockResolvedValue(data("north"));
    const wrapper = shallowMount(OfficeMap, { props: { mapName: "north.svg", workplace: "desk-1" } });
    await flushPromises();
    expect(wrapper.find("svg #north").exists()).toBe(true);
    expect(initializeWorkplaces).toHaveBeenCalledWith(expect.anything(), expect.any(Function), [], ["desk-1"]);
    const employee = { id: 1, displayName: "Alex Morgan" };
    const click = vi.mocked(initializeWorkplaces).mock.calls[0]![1]!;
    click("desk-1", employee);
    click("desk-2");
    expect(wrapper.emitted("employee-select")).toEqual([[employee]]);
    await wrapper.setProps({ workplace: "desk-2" });
    expect(initializeWorkplaces).toHaveBeenLastCalledWith(expect.anything(), expect.any(Function), [], ["desk-2"]);
    wrapper.unmount();
    expect(panZoom.destroy).toHaveBeenCalled();
  });

  it("ignores a previous map response after selection changes", async () => {
    let resolvePrevious!: (value: OfficeMapData) => void;
    vi.mocked(loadOfficeMapData)
      .mockReturnValueOnce(new Promise((resolve) => { resolvePrevious = resolve; }))
      .mockResolvedValueOnce(data("south"));
    const wrapper = shallowMount(OfficeMap, { props: { mapName: "north.svg" } });
    await wrapper.setProps({ mapName: "south.svg" });
    await flushPromises();
    resolvePrevious(data("north"));
    await flushPromises();
    expect(wrapper.find("#south").exists()).toBe(true);
    expect(wrapper.find("#north").exists()).toBe(false);
    wrapper.unmount();
  });

  it("shows loading errors", async () => {
    vi.mocked(loadOfficeMapData).mockRejectedValue(new Error("Map unavailable"));
    const wrapper = shallowMount(OfficeMap, {
      props: { mapName: "north.svg" },
      global: { renderStubDefaultSlot: true },
    });
    await flushPromises();
    expect(wrapper.text()).toContain("Map unavailable");
    wrapper.unmount();
  });
});
