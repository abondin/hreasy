import { defineComponent, h, ref, type PropType } from "vue";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vitest";
import ConfirmDeleteDialog from "@/components/shared/ConfirmDeleteDialog.vue";
import { BusinessError } from "@/lib/errors";
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

const DialogStub = defineComponent({
  props: { modelValue: Boolean },
  setup(props, { attrs, slots }) {
    return () => props.modelValue ? h("div", attrs, slots.default?.()) : null;
  },
});

const SelectStub = defineComponent({
  name: "VSelectStub",
  inheritAttrs: false,
  props: {
    modelValue: { type: Array, default: () => [] },
    items: { type: Array, default: () => [] },
  },
  emits: ["update:modelValue"],
  setup(_, { attrs }) {
    return () => h("div", attrs);
  },
});

const ToggleStub = defineComponent({
  name: "VBtnToggleStub",
  inheritAttrs: false,
  props: { modelValue: String },
  emits: ["update:modelValue"],
  setup(_, { attrs, slots }) {
    return () => h("div", attrs, slots.default?.());
  },
});

interface GridColumnStub {
  prop: string;
  name?: string;
  children?: GridColumnStub[];
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
    return () => {
      const leafColumns = props.columns.flatMap(column => column.children ?? [column]);
      const headers = props.columns.flatMap(column => column.children ? [column, ...column.children] : [column]);
      return h("div", [
      ...headers.map(column => h("div", { role: "columnheader" }, column.name)),
      ...props.source.flatMap((model, rowIndex) => leafColumns.map((column) => {
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
    };
  },
});

const globalStubs = {
  TableFirstPageLayout: PassThroughStub,
  TablePageCard: PassThroughStub,
  Grid: GridStub,
  RevoGrid: GridStub,
  PeriodSwitcherControl: PassThroughStub,
  VBtn: PassThroughStub,
  VBtnGroup: PassThroughStub,
  VBtnToggle: ToggleStub,
  VTextField: PassThroughStub,
  VSelect: SelectStub,
  VAutocomplete: SelectStub,
  VCheckbox: PassThroughStub,
  VSheet: PassThroughStub,
  VSpacer: PassThroughStub,
  VAlert: PassThroughStub,
  VChip: PassThroughStub,
  VEmptyState: PassThroughStub,
  VDialog: DialogStub,
  VCard: PassThroughStub,
  VCardTitle: PassThroughStub,
  VCardText: PassThroughStub,
  VCardActions: PassThroughStub,
  VIcon: PassThroughStub,
};

afterEach(() => {
  vi.clearAllMocks();
  vi.unstubAllGlobals();
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
        currentProjectId: 10,
        currentProjectName: "Managed",
      }],
      projects: [
        { id: 10, name: "Managed", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: true, managed: true },
        { id: 20, name: "Read only", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: false, managed: false },
      ],
      allocations: [
        { employeeId: 1, projectId: 10, percent: 60, revisionId: 1 },
        { employeeId: 1, projectId: 20, percent: 40, revisionId: 1 },
      ],
      previousAllocations: [],
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
        { id: 1, displayName: "Alex Morgan", departmentId: null, departmentName: null, currentProjectId: 10, currentProjectName: "Managed A" },
        { id: 2, displayName: "Jordan Lee", departmentId: null, departmentName: null, currentProjectId: 10, currentProjectName: "Managed A" },
      ],
      projects: [
        { id: 10, name: "Managed A", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: true, managed: true },
        { id: 20, name: "Managed B", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: true, managed: true },
        { id: 30, name: "Read only", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: false, managed: false },
      ],
      allocations: [
        { employeeId: 1, projectId: 10, percent: 60, revisionId: 1 },
        { employeeId: 1, projectId: 20, percent: 20, revisionId: 1 },
        { employeeId: 1, projectId: 30, percent: 20, revisionId: 1 },
        { employeeId: 2, projectId: 10, percent: 30, revisionId: 1 },
        { employeeId: 2, projectId: 20, percent: 70, revisionId: 1 },
      ],
      previousAllocations: [],
    });
    vi.mocked(saveResourceAllocations).mockResolvedValue();
    const wrapper = mount(ResourceAllocationsView, { global: { stubs: globalStubs } });
    await flushPromises();

    await wrapper.get('[data-testid="resource-allocation-cell-1-10"]').trigger("keydown", { key: "Delete" });
    await wrapper.get('[data-testid="resource-allocation-clear-row-2"]').trigger("click");
    wrapper.findAllComponents(ConfirmDeleteDialog)
      .find(dialog => dialog.attributes("data-testid") === "resource-allocations-clear-employee-dialog")!
      .vm.$emit("confirm");
    await flushPromises();
    await wrapper.get('[data-testid="resource-allocations-save"]').trigger("click");
    await flushPromises();

    expect(saveResourceAllocations).toHaveBeenCalledWith(expect.any(Number), [
      { employeeId: 1, projectId: 10, percent: 0, expectedRevisionId: 1 },
      { employeeId: 2, projectId: 10, percent: 0, expectedRevisionId: 1 },
      { employeeId: 2, projectId: 20, percent: 0, expectedRevisionId: 1 },
    ]);

    await wrapper.get('[data-testid="resource-allocation-cell-1-10"]').trigger("keydown", { key: "Delete" });
    await wrapper.get('[data-testid="resource-allocations-cancel"]').trigger("click");
    wrapper.findAllComponents(ConfirmDeleteDialog)
      .find(dialog => dialog.attributes("data-testid") === "resource-allocations-discard-dialog")!
      .vm.$emit("confirm");
    await flushPromises();
    expect(wrapper.get('[data-testid="resource-allocation-cell-1-10"]').text()).toBe("60");

    await wrapper.get('[data-testid="resource-allocation-cell-1-10"]').trigger("keydown", { key: "Delete" });
    const fetchCallsBeforeDiscard = vi.mocked(fetchResourceAllocations).mock.calls.length;
    await wrapper.get('[aria-label="Обновить данные"]').trigger("click");
    expect(wrapper.find('[data-testid="resource-allocations-discard-dialog"]').exists()).toBe(true);
    expect(fetchResourceAllocations).toHaveBeenCalledTimes(fetchCallsBeforeDiscard);

    wrapper.getComponent(ConfirmDeleteDialog).vm.$emit("close");
    await flushPromises();
    expect(wrapper.find('[data-testid="resource-allocations-discard-dialog"]').exists()).toBe(false);
    await wrapper.get('[aria-label="Обновить данные"]').trigger("click");
    wrapper.getComponent(ConfirmDeleteDialog).vm.$emit("confirm");
    await flushPromises();
    expect(fetchResourceAllocations).toHaveBeenCalledTimes(fetchCallsBeforeDiscard + 1);
  });

  it("applies server conflicts and keeps unrelated local edits without another load", async () => {
    vi.mocked(fetchResourceAllocations).mockResolvedValue({
      period: 202607,
      employees: [{
        id: 1,
        displayName: "Alex Morgan",
        departmentId: null,
        departmentName: null,
        currentProjectId: 10,
        currentProjectName: "Managed A",
      }],
      projects: [
        { id: 10, name: "Managed A", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: true, managed: true },
        { id: 20, name: "Managed B", departmentId: null, departmentName: null, baId: null, baName: null, active: true, editable: true, managed: true },
      ],
      allocations: [
        { employeeId: 1, projectId: 10, percent: 60, revisionId: 1 },
        { employeeId: 1, projectId: 20, percent: 20, revisionId: 1 },
      ],
      previousAllocations: [],
    });
    vi.mocked(saveResourceAllocations).mockRejectedValue(new BusinessError(
      "Аллокации были изменены другим пользователем",
      "errors.resource_allocation.conflict",
      {
        allocations: [
          { employeeId: 1, projectId: 10, percent: 80, revisionId: 2 },
          { employeeId: 1, projectId: 20, percent: 20, revisionId: 1 },
        ],
        changes: [{ employeeId: 1, projectId: 20, percent: 0, expectedRevisionId: 1 }],
        conflicts: [{ employeeId: 1, projectId: 10 }],
      },
    ));
    const wrapper = mount(ResourceAllocationsView, { global: { stubs: globalStubs } });
    await flushPromises();

    await wrapper.get('[data-testid="resource-allocation-cell-1-10"]').trigger("keydown", { key: "Delete" });
    await wrapper.get('[data-testid="resource-allocation-cell-1-20"]').trigger("keydown", { key: "Delete" });
    await wrapper.get('[data-testid="resource-allocations-save"]').trigger("click");
    await flushPromises();

    expect(fetchResourceAllocations).toHaveBeenCalledTimes(1);
    expect(wrapper.get('[data-testid="resource-allocation-cell-1-10"]').text()).toBe("80");
    expect(wrapper.get('[data-testid="resource-allocation-cell-1-20"]').text()).toBe("");
    expect(wrapper.text()).toContain("Конфликтующих ячеек: {count}");
    expect(wrapper.get('[data-testid="resource-allocations-save"]').attributes("disabled")).toBe("false");
  });

  it("defaults to employees from managed projects and limits project filters by BA", async () => {
    vi.mocked(fetchResourceAllocations).mockResolvedValue({
      period: 202607,
      employees: [
        { id: 1, displayName: "Managed Employee", departmentId: null, departmentName: null, currentProjectId: 10, currentProjectName: "Project A" },
        { id: 2, displayName: "Other Employee", departmentId: null, departmentName: null, currentProjectId: 30, currentProjectName: "Read only" },
      ],
      projects: [
        { id: 10, name: "Project A", departmentId: null, departmentName: null, baId: 100, baName: "BA A", active: true, editable: true, managed: true },
        { id: 20, name: "Project B", departmentId: null, departmentName: null, baId: 200, baName: "BA B", active: true, editable: true, managed: true },
        { id: 30, name: "Manual access", departmentId: null, departmentName: null, baId: 200, baName: "BA B", active: true, editable: true, managed: false },
      ],
      allocations: [],
      previousAllocations: [{ employeeId: 1, projectId: 10, percent: 40, revisionId: 1 }],
    });

    const wrapper = mount(ResourceAllocationsView, { global: { stubs: globalStubs } });
    await flushPromises();

    expect(wrapper.text()).toContain("Managed Employee");
    expect(wrapper.text()).not.toContain("Other Employee");
    expect(wrapper.get('[data-testid="resource-allocation-cell-1-10"]').text()).toBe("40");
    expect(wrapper.find('[data-testid="resource-allocations-employee-ba"]').exists()).toBe(false);
    expect(wrapper.find('[data-testid="resource-allocations-project-ba"]').exists()).toBe(false);

    await wrapper.get('[data-testid="resource-allocations-employees-selected"]').trigger("click");
    await flushPromises();
    const [employeeBa, employeeProjects] = wrapper.findAllComponents(SelectStub);
    employeeBa.vm.$emit("update:modelValue", [100]);
    await flushPromises();
    expect(employeeProjects.props("items")).toEqual([expect.objectContaining({ id: 10 })]);

    await wrapper.get('[data-testid="resource-allocations-scope-selected"]').trigger("click");
    await flushPromises();
    const [, , projectBa, projectProjects] = wrapper.findAllComponents(SelectStub);
    projectBa.vm.$emit("update:modelValue", [200]);
    await flushPromises();
    expect(projectProjects.props("items")).toEqual([
      expect.objectContaining({ id: 30 }),
      expect.objectContaining({ id: 20 }),
    ]);
    projectProjects.vm.$emit("update:modelValue", [20]);
    await flushPromises();
    expect(wrapper.text()).toContain("BA B");
    expect(wrapper.text()).toContain("Project B");
    expect(wrapper.text()).not.toContain("Project A");

    await wrapper.get('[data-testid="resource-allocations-preset-company"]').trigger("click");
    expect(wrapper.find('[data-testid="resource-allocations-employee-ba"]').exists()).toBe(false);
    expect(wrapper.find('[data-testid="resource-allocations-project-ba"]').exists()).toBe(false);
    expect(wrapper.text()).toContain("Other Employee");
  });
});
