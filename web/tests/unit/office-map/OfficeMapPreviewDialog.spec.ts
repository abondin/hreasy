import { ref } from "vue";
import { flushPromises, shallowMount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import OfficeMapPreviewDialog from "@/components/office-map/OfficeMapPreviewDialog.vue";
import OfficeMap from "@/components/office-map/OfficeMap.vue";
import OfficeMapEmployeeCard from "@/components/office-map/OfficeMapEmployeeCard.vue";
import { listEmployees } from "@/services/employee.service";

vi.mock("vue-i18n", () => ({ useI18n: () => ({ t: (key: string) => key }) }));
vi.mock("vuetify", () => ({ useDisplay: () => ({ smAndDown: ref(false) }) }));
vi.mock("@/services/employee.service", () => ({ listEmployees: vi.fn() }));

describe("OfficeMapPreviewDialog", () => {
  it("mounts the map only while open and clears employee details on close", async () => {
    const employees = [{ id: 1, displayName: "Alex Morgan" }];
    vi.mocked(listEmployees).mockResolvedValue(employees);
    const wrapper = shallowMount(OfficeMapPreviewDialog, {
      props: { mapName: "north.svg", workplace: "desk-1" },
      global: { renderStubDefaultSlot: true },
    });
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    await wrapper.setProps({ modelValue: true });
    await flushPromises();
    expect(wrapper.getComponent(OfficeMap).props()).toMatchObject({
      mapName: "north.svg", workplace: "desk-1", employees,
    });
    wrapper.getComponent(OfficeMap).vm.$emit("employee-select", { id: 1, displayName: "Alex Morgan" });
    await wrapper.vm.$nextTick();
    expect(wrapper.getComponent(OfficeMapEmployeeCard).props("employeeId")).toBe(1);
    await wrapper.setProps({ modelValue: false });
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    expect(wrapper.findComponent(OfficeMapEmployeeCard).exists()).toBe(false);
    wrapper.unmount();
  });
  it("shows employee-loading errors and recovers on retry", async () => {
    vi.mocked(listEmployees)
      .mockRejectedValueOnce(new Error("Employees unavailable"))
      .mockResolvedValueOnce([]);
    const wrapper = shallowMount(OfficeMapPreviewDialog, {
      props: { mapName: "north.svg", modelValue: true },
      global: { renderStubDefaultSlot: true, stubs: { VAlert: { template: "<div><slot/><slot name='append'/></div>" } } },
    });
    await flushPromises();
    expect(wrapper.text()).toContain("Employees unavailable");
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    await wrapper.findAll("v-btn-stub").find((button) => button.text() === "Повторить")!.trigger("click");
    await flushPromises();
    expect(wrapper.getComponent(OfficeMap).props("employees")).toEqual([]);
    wrapper.unmount();
  });
});
