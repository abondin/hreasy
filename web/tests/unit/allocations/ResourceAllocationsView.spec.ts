import { defineComponent, h, type PropType } from "vue";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import ResourceAllocationsView from "@/views/allocations/ResourceAllocationsView.vue";
import { fetchResourceAllocations } from "@/services/resource-allocation.service";

vi.mock("vue-i18n", () => ({
  useI18n: () => ({ t: (key: string) => key }),
}));

vi.mock("@/services/resource-allocation.service", () => ({
  fetchResourceAllocations: vi.fn(),
  saveResourceAllocations: vi.fn(),
}));

const PassThroughStub = defineComponent({
  setup(_, { attrs, slots }) {
    return () => h("div", attrs, [slots.default?.(), slots.table?.()]);
  },
});

const TableStub = defineComponent({
  props: {
    items: { type: Array as PropType<Array<{ id: number; total: number }>>, default: () => [] },
  },
  setup(props, { slots }) {
    return () => {
      const item = props.items[0];
      if (!item) {
        return h("div");
      }
      return h("div", [
        slots["item.total"]?.({ item }),
        ...Object.entries(slots)
          .filter(([name]) => name.startsWith("item.project_"))
          .flatMap(([, slot]) => slot?.({ item }) ?? []),
      ]);
    };
  },
});

describe("ResourceAllocationsView", () => {
  it("includes hidden read-only projects in the employee total", async () => {
    vi.mocked(fetchResourceAllocations).mockResolvedValue({
      period: 202607,
      employees: [{
        id: 1,
        displayName: "Alex Morgan",
        departmentId: null,
        departmentName: null,
        currentProjectId: null,
        currentProjectName: null,
      }],
      projects: [
        { id: 10, name: "Managed", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: true },
        { id: 20, name: "Read only", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: false },
      ],
      allocations: [
        { employeeId: 1, projectId: 10, percent: 60, revisionId: 1 },
        { employeeId: 1, projectId: 20, percent: 40, revisionId: 1 },
      ],
    });

    const wrapper = mount(ResourceAllocationsView, {
      global: {
        stubs: {
          TableFirstPageLayout: PassThroughStub,
          TablePageCard: PassThroughStub,
          HREasyTableBase: TableStub,
          PeriodSwitcherControl: PassThroughStub,
          VBtn: PassThroughStub,
          VBtnToggle: PassThroughStub,
          VTextField: PassThroughStub,
          VSpacer: PassThroughStub,
          VAlert: PassThroughStub,
          VChip: PassThroughStub,
        },
      },
    });
    await flushPromises();

    expect(wrapper.text()).toContain("100%");
    expect(wrapper.findAll("input")).toHaveLength(1);
    expect(wrapper.get("input").attributes("aria-label")).toContain("Managed");
  });
});
