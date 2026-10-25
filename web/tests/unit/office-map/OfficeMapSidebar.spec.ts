import { ref } from "vue";
import { flushPromises, shallowMount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import OfficeMapSidebar from "@/components/office-map/OfficeMapSidebar.vue";
import OfficeMapEmployeeCard from "@/components/office-map/OfficeMapEmployeeCard.vue";
import SearchTextField from "@/components/shared/SearchTextField.vue";
vi.mock("vue-i18n", () => ({ useI18n: () => ({ t: (key: string) => key }) }));
vi.mock("vuetify", () => ({ useDisplay: () => ({ smAndDown: ref(false), lgAndUp: ref(true) }) }));
const employees = [
  { id: 1, displayName: "Alex Morgan", officeWorkplace: "A1" },
  { id: 2, displayName: "Jordan Lee" },
];
describe("OfficeMapSidebar", () => {
  it("uses shared search settings and separates row selection from opening employee details", async () => {
    const wrapper = shallowMount(OfficeMapSidebar, {
      props: { employees, modelValue: null, loading: false, scope: "All employees", error: "" },
      global: {
        renderStubDefaultSlot: true,
        stubs: {
          VHover: { template: "<slot :is-hovering='false' :props='{}' />" },
          VNavigationDrawer: { template: "<div><slot name='prepend'/><slot/></div>" },
          VListItem: { template: "<div><slot/><slot name='append'/></div>" },
        },
      },
    });
    expect(wrapper.text()).toContain("Рабочее место не указано");
    expect(wrapper.text()).toContain("All employees");
    const row = wrapper.get('[data-testid="office-maps-employee-1"]');
    const detailsButton = row.get("v-btn-stub");
    expect(detailsButton.classes()).toContain("opacity-0");
    await detailsButton.trigger("focus");
    expect(detailsButton.classes()).not.toContain("opacity-0");
    await detailsButton.trigger("blur");
    await row.trigger("click");
    expect(wrapper.emitted("update:modelValue")).toEqual([[employees[0]]]);
    expect(wrapper.findComponent(OfficeMapEmployeeCard).exists()).toBe(false);
    await row.get("v-btn-stub").trigger("click");
    expect(wrapper.getComponent(OfficeMapEmployeeCard).props("employeeId")).toBe(1);
    expect(wrapper.emitted("update:modelValue")).toHaveLength(1);
    await wrapper.setProps({ search: "Morgna" });
    expect(wrapper.find('[data-testid="office-maps-employee-1"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="office-maps-employee-2"]').exists()).toBe(false);
    wrapper.getComponent(SearchTextField).vm.$emit("update:settings", { fuzzy: false, keyboardLayout: false, unorderedTerms: false });
    await flushPromises();
    expect(wrapper.find('[data-testid="office-maps-employee-1"]').exists()).toBe(false);
    wrapper.unmount();
  });
});
