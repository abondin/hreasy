import { defineComponent } from "vue";
import { flushPromises, shallowMount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import OfficeMapsView from "@/views/office-map/OfficeMapsView.vue";
import OfficeMap from "@/components/office-map/OfficeMap.vue";
import OfficeMapSidebar from "@/components/office-map/OfficeMapSidebar.vue";
import { fetchOffices, fetchOfficeLocations } from "@/services/dict.service";
import { listEmployees } from "@/services/employee.service";

vi.mock("vue-i18n", () => ({ useI18n: () => ({ t: (key: string) => key }) }));
vi.mock("@/services/dict.service", () => ({ fetchOffices: vi.fn(), fetchOfficeLocations: vi.fn() }));
vi.mock("@/services/employee.service", () => ({ listEmployees: vi.fn() }));
const SelectStub = defineComponent({ props: ["items", "modelValue", "disabled"], emits: ["update:modelValue"], template: "<div />" });
const location = { id: 11, name: "Room A", officeId: 1, mapName: "north.svg" };
const employees = [
  { id: 1, displayName: "Alex Morgan", officeLocation: location, officeWorkplace: "A1" },
  { id: 2, displayName: "Jordan Lee", officeLocation: location, officeWorkplace: " a1 " },
  { id: 3, displayName: "Casey Taylor", officeLocation: { id: 12, name: "Room B", officeId: 1 }, officeWorkplace: null },
  { id: 4, displayName: "Robin Lane" },
];
function mountView() {
  return shallowMount(OfficeMapsView, { global: { renderStubDefaultSlot: true, stubs: {
    VAutocomplete: SelectStub, VAlert: { template: "<div><slot/><slot name='append'/></div>" },
  } } });
}
beforeEach(() => {
  vi.mocked(fetchOffices).mockResolvedValue([{ id: 1, name: "North", primaryOffice: false }]);
  vi.mocked(fetchOfficeLocations).mockResolvedValue([location, { id: 12, name: "Room B", officeId: 1, mapName: null }]);
  vi.mocked(listEmployees).mockResolvedValue(employees);
});
describe("OfficeMapsView", () => {
  it("initially selects the first primary office and lets the user clear it", async () => {
    vi.mocked(fetchOffices).mockResolvedValue([
      { id: 2, name: "Other", primaryOffice: false },
      { id: 1, name: "North", primaryOffice: true },
      { id: 3, name: "South", primaryOffice: true },
    ]);
    const wrapper = mountView();
    await flushPromises();
    const [office, room] = wrapper.findAllComponents(SelectStub);
    const sidebar = wrapper.getComponent(OfficeMapSidebar);
    expect(office!.props("modelValue")).toBe(1);
    expect(room!.props("modelValue")).toBeNull();
    expect(sidebar.props("employees")).toHaveLength(3);
    expect(sidebar.props("scope")).toBe("North");
    office!.vm.$emit("update:modelValue", null);
    await flushPromises();
    expect(office!.props("modelValue")).toBeNull();
    expect(sidebar.props("employees")).toHaveLength(4);
    wrapper.unmount();
  });
  it("scopes all employees by office and room, including rooms without maps", async () => {
    const wrapper = mountView();
    await flushPromises();
    const sidebar = wrapper.getComponent(OfficeMapSidebar);
    const [office, room] = wrapper.findAllComponents(SelectStub);
    expect(sidebar.props("employees")).toHaveLength(4);
    expect(sidebar.props("scope")).toBe("Все сотрудники");
    expect(office!.props("modelValue")).toBeNull();
    office!.vm.$emit("update:modelValue", 1);
    await flushPromises();
    expect(sidebar.props("employees")).toHaveLength(3);
    expect(sidebar.props("scope")).toBe("North");
    room!.vm.$emit("update:modelValue", 12);
    await flushPromises();
    expect(sidebar.props("employees")).toEqual([employees[2]]);
    expect(sidebar.props("scope")).toBe("Room B");
    expect(wrapper.text()).toContain("Для этого кабинета карта не загружена");
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    wrapper.unmount();
  });
  it("clears search on map click, preserves duplicate assignments, and resets place on room change", async () => {
    const wrapper = mountView();
    await flushPromises();
    const sidebar = wrapper.getComponent(OfficeMapSidebar);
    const [office, room] = wrapper.findAllComponents(SelectStub);
    office!.vm.$emit("update:modelValue", 1);
    room!.vm.$emit("update:modelValue", 11);
    await flushPromises();
    sidebar.vm.$emit("update:search", "unrelated");
    await flushPromises();
    wrapper.getComponent(OfficeMap).vm.$emit("workplace-select", "a1");
    await flushPromises();
    expect(sidebar.props("search")).toBe("");
    expect(sidebar.props("workplace")).toBe("a1");
    expect(sidebar.props("employees")).toEqual(employees.slice(0, 2));
    sidebar.vm.$emit("update:search", "Casey");
    room!.vm.$emit("update:modelValue", 12);
    await flushPromises();
    expect(sidebar.props("workplace")).toBeNull();
    expect(sidebar.props("modelValue")).toBeNull();
    expect(sidebar.props("search")).toBe("Casey");
    wrapper.unmount();
  });
  it("highlights list selection without changing scope or hiding other employees", async () => {
    const wrapper = mountView();
    await flushPromises();
    const sidebar = wrapper.getComponent(OfficeMapSidebar);
    const [office, room] = wrapper.findAllComponents(SelectStub);
    office!.vm.$emit("update:modelValue", 1);
    room!.vm.$emit("update:modelValue", 11);
    await flushPromises();
    sidebar.vm.$emit("update:modelValue", employees[0]);
    await flushPromises();
    expect(office!.props("modelValue")).toBe(1);
    expect(room!.props("modelValue")).toBe(11);
    expect(wrapper.getComponent(OfficeMap).props("workplace")).toBe("A1");
    expect(wrapper.getComponent(OfficeMap).props("employees")).toEqual(employees);
    expect(sidebar.props("employees")).toHaveLength(2);
    wrapper.unmount();
  });
  it("retries initial loading without presenting a failed load as an empty directory", async () => {
    vi.mocked(fetchOffices).mockRejectedValueOnce(new Error("Office loading failed"));
    const wrapper = mountView();
    await flushPromises();
    expect(wrapper.text()).toContain("Office loading failed");
    expect(wrapper.getComponent(OfficeMapSidebar).props("error")).toBe("Office loading failed");
    await wrapper.get("v-btn-stub").trigger("click");
    await flushPromises();
    expect(wrapper.getComponent(OfficeMapSidebar).props("error")).toBe("");
    expect(wrapper.getComponent(OfficeMapSidebar).props("employees")).toHaveLength(4);
    wrapper.unmount();
  });
  it("opens an employee map from the full list without narrowing scope or clearing search", async () => {
    const wrapper = mountView();
    await flushPromises();
    const sidebar = wrapper.getComponent(OfficeMapSidebar);
    const [office, room] = wrapper.findAllComponents(SelectStub);
    sidebar.vm.$emit("update:search", "Morgan");
    sidebar.vm.$emit("update:modelValue", employees[0]);
    await flushPromises();
    expect(office!.props("modelValue")).toBeNull();
    expect(room!.props("modelValue")).toBeNull();
    expect(sidebar.props("employees")).toHaveLength(4);
    expect(sidebar.props("search")).toBe("Morgan");
    expect(wrapper.getComponent(OfficeMap).props()).toMatchObject({ mapName: "north.svg", workplace: "A1" });
    expect(wrapper.get('[data-testid="office-maps-map-title"]').text()).toContain("North · Room A");
    wrapper.getComponent(OfficeMap).vm.$emit("workplace-select", "a1");
    await flushPromises();
    expect(wrapper.getComponent(OfficeMap).props("mapName")).toBe("north.svg");
    expect(sidebar.props("employees")).toHaveLength(2);
    expect(sidebar.props("search")).toBe("");
    wrapper.unmount();
  });
  it("clears room and office filters independently and resets an employee map", async () => {
    const wrapper = mountView();
    await flushPromises();
    const sidebar = wrapper.getComponent(OfficeMapSidebar);
    const [office, room] = wrapper.findAllComponents(SelectStub);
    office!.vm.$emit("update:modelValue", 1);
    room!.vm.$emit("update:modelValue", 11);
    await flushPromises();
    room!.vm.$emit("update:modelValue", null);
    await flushPromises();
    expect(office!.props("modelValue")).toBe(1);
    expect(sidebar.props("employees")).toHaveLength(3);
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    office!.vm.$emit("update:modelValue", null);
    await flushPromises();
    expect(sidebar.props("employees")).toHaveLength(4);
    office!.vm.$emit("update:modelValue", 1);
    room!.vm.$emit("update:modelValue", 11);
    sidebar.vm.$emit("update:search", "Morgan");
    sidebar.vm.$emit("update:modelValue", employees[0]);
    await flushPromises();
    office!.vm.$emit("update:modelValue", null);
    await flushPromises();
    expect(room!.props("modelValue")).toBeNull();
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    expect(sidebar.props("modelValue")).toBeNull();
    expect(sidebar.props("workplace")).toBeNull();
    expect(sidebar.props("search")).toBe("Morgan");
    expect(sidebar.props("employees")).toHaveLength(4);
    wrapper.unmount();
  });
  it("removes the previous map when the next employee has no map or room", async () => {
    const wrapper = mountView();
    await flushPromises();
    const sidebar = wrapper.getComponent(OfficeMapSidebar);
    sidebar.vm.$emit("update:modelValue", employees[0]);
    await flushPromises();
    sidebar.vm.$emit("update:modelValue", employees[2]);
    await flushPromises();
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    expect(wrapper.text()).toContain("Для этого кабинета карта не загружена");
    sidebar.vm.$emit("update:modelValue", employees[3]);
    await flushPromises();
    expect(wrapper.findComponent(OfficeMap).exists()).toBe(false);
    expect(wrapper.text()).toContain("Кабинет не указан");
    expect(sidebar.props("employees")).toHaveLength(4);
    wrapper.unmount();
  });
  it("clears both person and seat selection when the seat chip is closed", async () => {
    const wrapper = mountView();
    await flushPromises();
    const sidebar = wrapper.getComponent(OfficeMapSidebar);
    sidebar.vm.$emit("update:modelValue", employees[0]);
    await flushPromises();
    wrapper.getComponent(OfficeMap).vm.$emit("workplace-select", "a1");
    sidebar.vm.$emit("update:modelValue", employees[0]);
    await flushPromises();
    sidebar.vm.$emit("update:workplace", null);
    await flushPromises();
    expect(sidebar.props("modelValue")).toBeNull();
    expect(sidebar.props("workplace")).toBeNull();
    expect(sidebar.props("employees")).toHaveLength(4);
    expect(wrapper.getComponent(OfficeMap).props("workplace")).toBeNull();
    expect(wrapper.getComponent(OfficeMap).props("mapName")).toBe("north.svg");
    wrapper.unmount();
  });
});
