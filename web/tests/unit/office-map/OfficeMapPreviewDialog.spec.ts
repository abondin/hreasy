import { ref } from "vue";
import { shallowMount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import OfficeMapPreviewDialog from "@/components/office-map/OfficeMapPreviewDialog.vue";
import OfficeMap from "@/components/office-map/OfficeMap.vue";
import OfficeMapEmployeeCard from "@/components/office-map/OfficeMapEmployeeCard.vue";

vi.mock("vue-i18n", () => ({ useI18n: () => ({ t: (key: string) => key }) }));
vi.mock("vuetify", () => ({ useDisplay: () => ({ smAndDown: ref(false) }) }));

describe("OfficeMapPreviewDialog", () => {
  it("mounts the map only while open and clears employee details on close", async () => {
    const wrapper = shallowMount(OfficeMapPreviewDialog, {
      props: { mapName: "north.svg", workplace: "desk-1" },
      global: { renderStubDefaultSlot: true },
    });
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    await wrapper.setProps({ modelValue: true });
    expect(wrapper.getComponent(OfficeMap).props()).toMatchObject({
      mapName: "north.svg", workplace: "desk-1",
    });
    wrapper.getComponent(OfficeMap).vm.$emit("employee-select", { id: 1, displayName: "Alex Morgan" });
    await wrapper.vm.$nextTick();
    expect(wrapper.getComponent(OfficeMapEmployeeCard).props("employeeId")).toBe(1);
    await wrapper.setProps({ modelValue: false });
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    expect(wrapper.findComponent(OfficeMapEmployeeCard).exists()).toBe(false);
    wrapper.unmount();
  });
});
