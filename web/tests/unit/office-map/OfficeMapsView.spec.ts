import { defineComponent } from "vue";
import { flushPromises, shallowMount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import OfficeMapsView from "@/views/office-map/OfficeMapsView.vue";
import OfficeMap from "@/components/office-map/OfficeMap.vue";
import { fetchOffices, fetchOfficeLocations } from "@/services/dict.service";

vi.mock("vue-i18n", () => ({ useI18n: () => ({ t: (key: string) => key }) }));
vi.mock("@/services/dict.service", () => ({ fetchOffices: vi.fn(), fetchOfficeLocations: vi.fn() }));

const SelectStub = defineComponent({
  props: ["items", "modelValue", "disabled"],
  emits: ["update:modelValue"],
  template: "<div />",
});

function mountView() {
  return shallowMount(OfficeMapsView, {
    global: {
      renderStubDefaultSlot: true,
      stubs: {
        VContainer: { template: "<div><slot /></div>" },
        VCard: { template: "<div><slot /></div>" },
        VCardText: { template: "<div><slot /></div>" },
        VRow: { template: "<div><slot /></div>" },
        VCol: { template: "<div><slot /></div>" },
        VAlert: { template: "<div><slot /></div>" },
        VAutocomplete: SelectStub,
        VBtn: { props: ["disabled"], template: '<button :disabled="disabled"><slot /></button>' },
      },
    },
  });
}

beforeEach(() => {
  vi.mocked(fetchOffices).mockResolvedValue([{ id: 1, name: "North" }, { id: 2, name: "South" }]);
  vi.mocked(fetchOfficeLocations).mockResolvedValue([
    { id: 11, name: "Room A", officeId: 1, mapName: "north.svg" },
    { id: 21, name: "Room B", officeId: 2, mapName: null },
  ]);
});

describe("OfficeMapsView", () => {
  it("filters rooms, opens the selected map, and resets the room when the office changes", async () => {
    const wrapper = mountView();
    await flushPromises();
    const [office, room] = wrapper.findAllComponents(SelectStub);
    expect(room!.props("disabled")).toBe(true);
    office!.vm.$emit("update:modelValue", 1);
    await flushPromises();
    expect(room!.props("items")).toEqual([expect.objectContaining({ id: 11 })]);
    room!.vm.$emit("update:modelValue", 11);
    await flushPromises();
    expect(wrapper.getComponent(OfficeMap).props()).toMatchObject({
      mapName: "north.svg",
    });
    office!.vm.$emit("update:modelValue", 2);
    await flushPromises();
    expect(room!.props("modelValue")).toBeNull();
    expect(room!.props("items")).toEqual([expect.objectContaining({ id: 21 })]);
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    room!.vm.$emit("update:modelValue", 21);
    await flushPromises();
    expect(wrapper.text()).toContain("Для этого кабинета карта не загружена");
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    wrapper.unmount();
  });

  it("shows a loading failure without enabling map viewing", async () => {
    vi.mocked(fetchOffices).mockRejectedValue(new Error("Office loading failed"));
    const wrapper = mountView();
    await flushPromises();
    expect(wrapper.text()).toContain("Office loading failed");
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    wrapper.unmount();
  });
});
