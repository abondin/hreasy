import { defineComponent, h, ref, type PropType } from "vue";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vitest";
import ResourceAllocationsView from "@/views/allocations/ResourceAllocationsView.vue";
import {
  fetchResourceAllocations,
  saveResourceAllocations,
} from "@/services/resource-allocation.service";

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

interface GridColumnStub {
  prop: string;
  name?: string;
  readonly?: boolean | ((props: { model: Record<string, unknown> }) => boolean);
  cellProperties?: (props: { model: Record<string, unknown> }) => Record<string, unknown>;
  cellTemplate?: (
    createElement: typeof h,
    props: { model: Record<string, unknown> },
  ) => ReturnType<typeof h> | string | number | null | undefined;
}

const GridStub = defineComponent({
  name: "RevoGridStub",
  props: {
    source: { type: Array as PropType<Array<Record<string, unknown>>>, default: () => [] },
    columns: { type: Array as PropType<GridColumnStub[]>, default: () => [] },
  },
  emits: ["beforeeditstart", "afteredit"],
  setup(props, { emit }) {
    const activeCell = ref("");
    return () => h("div", [
      ...props.columns.map(column => h("div", { role: "columnheader" }, column.name)),
      ...props.source.flatMap((model, rowIndex) => props.columns.map((column) => {
        const cellProps = column.cellProperties?.({ model }) ?? {};
        const key = `${rowIndex}:${column.prop}`;
        const readonly = typeof column.readonly === "function"
          ? column.readonly({ model })
          : column.readonly;
        if (activeCell.value === key) {
          return h("input", {
            ...cellProps,
            value: model[column.prop],
          });
        }
        return h("div", {
          ...cellProps,
          tabindex: 0,
          onClick: () => {
            if (!readonly) {
              activeCell.value = key;
              emit("beforeeditstart");
            }
          },
          onKeydown: (event: KeyboardEvent) => {
            if (!readonly && (event.key === "Delete" || event.key === "Backspace")) {
              model[column.prop] = "";
              emit("afteredit", { detail: { prop: column.prop, model, val: "" } });
            }
          },
        }, column.cellTemplate?.(h, { model }) ?? String(model[column.prop] ?? ""));
      })),
    ]);
  },
});

const globalStubs = {
  TableFirstPageLayout: PassThroughStub,
  TablePageCard: PassThroughStub,
  Grid: GridStub,
  RevoGrid: GridStub,
  PeriodSwitcherControl: PassThroughStub,
  VBtn: PassThroughStub,
  VBtnToggle: PassThroughStub,
  VTextField: PassThroughStub,
  VSelect: PassThroughStub,
  VCheckbox: PassThroughStub,
  VSheet: PassThroughStub,
  VSpacer: PassThroughStub,
  VAlert: PassThroughStub,
  VChip: PassThroughStub,
  VEmptyState: PassThroughStub,
};

afterEach(() => vi.unstubAllGlobals());

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
        stubs: globalStubs,
      },
    });
    await flushPromises();

    expect(wrapper.text()).toContain("100%");
    expect(wrapper.findAll("input")).toHaveLength(0);
    await wrapper.get('[data-testid="resource-allocation-cell-1-10"]').trigger("click");
    await flushPromises();
    expect(wrapper.findAll("input")).toHaveLength(1);
    expect(wrapper.get("input").attributes("aria-label")).toContain("Managed");
  });

  it("clears a cell and editable row as changed-only zero values", async () => {
    vi.mocked(fetchResourceAllocations).mockResolvedValue({
      period: 202607,
      employees: [
        { id: 1, displayName: "Alex Morgan", departmentId: null, departmentName: null, currentProjectId: null, currentProjectName: null },
        { id: 2, displayName: "Jordan Lee", departmentId: null, departmentName: null, currentProjectId: null, currentProjectName: null },
      ],
      projects: [
        { id: 10, name: "Managed A", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: true },
        { id: 20, name: "Managed B", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: true },
        { id: 30, name: "Read only", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: false },
      ],
      allocations: [
        { employeeId: 1, projectId: 10, percent: 60, revisionId: 1 },
        { employeeId: 1, projectId: 20, percent: 20, revisionId: 1 },
        { employeeId: 1, projectId: 30, percent: 20, revisionId: 1 },
        { employeeId: 2, projectId: 10, percent: 30, revisionId: 1 },
        { employeeId: 2, projectId: 20, percent: 70, revisionId: 1 },
      ],
    });
    vi.mocked(saveResourceAllocations).mockResolvedValue();
    vi.stubGlobal("confirm", vi.fn(() => true));
    const wrapper = mount(ResourceAllocationsView, { global: { stubs: globalStubs } });
    await flushPromises();

    await wrapper.get('[data-testid="resource-allocation-cell-1-10"]').trigger("keydown", { key: "Delete" });
    await wrapper.get('[data-testid="resource-allocation-clear-row-2"]').trigger("click");
    await wrapper.get('[data-testid="resource-allocations-save"]').trigger("click");
    await flushPromises();

    expect(saveResourceAllocations).toHaveBeenCalledWith(expect.any(Number), [
      { employeeId: 1, projectId: 10, percent: 0 },
      { employeeId: 2, projectId: 10, percent: 0 },
      { employeeId: 2, projectId: 20, percent: 0 },
    ]);
  });
});
