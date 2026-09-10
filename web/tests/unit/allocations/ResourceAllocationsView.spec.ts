import { defineComponent, h, reactive, ref, type PropType } from "vue";
import { flushPromises, mount, type VueWrapper } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { PSEUDO_GROUP_ITEM_VALUE } from "@revolist/revogrid";
import { BusinessError } from "@/lib/errors";
import ResourceAllocationAnalyticsView from "@/views/allocations/ResourceAllocationAnalyticsView.vue";
import ResourceAllocationInputView from "@/views/allocations/ResourceAllocationInputView.vue";
import {
  exportResourceAllocationAnalytics,
  fetchClosedResourceAllocationPeriods,
  fetchResourceAllocationAnalytics,
  fetchResourceAllocationProjectInput,
  saveClosedResourceAllocationPeriods,
  saveResourceAllocations,
} from "@/services/resource-allocation.service";

const routerMocks = vi.hoisted(() => ({
  query: {} as Record<string, string>,
  replace: vi.fn().mockResolvedValue(undefined),
}));
const permissionMocks = vi.hoisted(() => ({ canAdmin: false }));

vi.mock("vue-router", async () => ({
  ...await vi.importActual<typeof import("vue-router")>("vue-router"),
  onBeforeRouteLeave: vi.fn(),
  useRoute: () => ({ name: "resource-allocations-analytics", query: routerMocks.query }),
  useRouter: () => ({ replace: routerMocks.replace }),
}));

vi.mock("vue-i18n", () => ({
  useI18n: () => ({
    t: (key: string, params: Record<string, unknown> = {}) => Object.entries(params)
      .reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), key),
  }),
}));

vi.mock("@/lib/permissions", () => ({
  usePermissions: () => ({
    canAdminResourceAllocations: () => permissionMocks.canAdmin,
  }),
}));

vi.mock("@/services/resource-allocation.service", () => ({
  exportResourceAllocationAnalytics: vi.fn(),
  fetchClosedResourceAllocationPeriods: vi.fn(),
  fetchResourceAllocationAnalytics: vi.fn(),
  fetchResourceAllocationProjectInput: vi.fn(),
  saveClosedResourceAllocationPeriods: vi.fn(),
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
    return () => (props.modelValue ? h("div", attrs, slots.default?.()) : null);
  },
});

const CheckboxStub = defineComponent({
  inheritAttrs: false,
  props: {
    modelValue: { type: Array as PropType<number[]>, default: () => [] },
    value: { type: Number, required: true },
  },
  emits: ["update:modelValue"],
  setup(props, { attrs, emit }) {
    return () => h("input", {
      ...attrs,
      type: "checkbox",
      checked: props.modelValue.includes(props.value),
      onChange: (event: Event) => emit(
        "update:modelValue",
        (event.target as HTMLInputElement).checked
          ? [...props.modelValue, props.value]
          : props.modelValue.filter((period) => period !== props.value),
      ),
    });
  },
});

const SelectStub = defineComponent({
  name: "VSelectStub",
  inheritAttrs: false,
  props: {
    modelValue: {
      type: [Array, Number, Object] as PropType<
        number[] | number | object | null
      >,
      default: null,
    },
    items: { type: Array, default: () => [] },
  },
  emits: ["update:modelValue"],
  setup(props, { attrs, slots }) {
    return () => h("div", attrs, props.items.map((item, index) =>
      slots.item?.({ props: {}, item, index }),
    ));
  },
});

const ListItemStub = defineComponent({
  props: { title: String, subtitle: String },
  setup(props) {
    return () => h("div", [props.title, props.subtitle]);
  },
});

const TableStub = defineComponent({
  setup(_, { attrs, slots }) {
    return () => h("table", attrs, slots.default?.());
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

const TooltipStub = defineComponent({
  setup(_, { slots }) {
    return () => h("div", slots.activator?.({ props: {} }));
  },
});

interface GridColumnStub {
  prop: string;
  name?: string;
  children?: GridColumnStub[];
  readonly?: boolean | ((props: { model: Record<string, unknown> }) => boolean);
  columnTemplate?: (createElement: typeof h) => ReturnType<typeof h>;
  cellProperties?: (props: {
    model: Record<string, unknown>;
  }) => Record<string, unknown>;
  cellTemplate?: (
    createElement: typeof h,
    props: { model: Record<string, unknown>; prop: string; value: unknown },
    addition?: Record<string, unknown>,
  ) => ReturnType<typeof h> | string | number | null | undefined;
}

const GridStub = defineComponent({
  name: "RevoGridStub",
  props: {
    source: {
      type: Array as PropType<Array<Record<string, unknown>>>,
      default: () => [],
    },
    columns: { type: Array as PropType<GridColumnStub[]>, default: () => [] },
    additionalData: {
      type: Object as PropType<Record<string, unknown>>,
      default: () => ({}),
    },
    readonly: Boolean,
    stretch: Boolean,
    grouping: { type: Object, default: undefined },
  },
  emits: ["beforeeditstart", "afteredit"],
  setup(props, { emit }) {
    const activeCell = ref("");
    return () => {
      const leafColumns = props.columns.flatMap(
        (column) => column.children ?? [column],
      );
      const headers = props.columns.flatMap((column) =>
        column.children ? [column, ...column.children] : [column],
      );
      return h("div", [
        ...headers.map((column) =>
          h("div", { role: "columnheader" }, column.columnTemplate?.(h) ?? column.name),
        ),
        ...props.source.flatMap((model, rowIndex) =>
          leafColumns.map((column) => {
            const cellProps = column.cellProperties?.({ model }) ?? {};
            const key = `${rowIndex}:${column.prop}`;
            const readonly =
              props.readonly ||
              (typeof column.readonly === "function"
                ? column.readonly({ model })
                : column.readonly);
            if (activeCell.value === key) {
              return h("input", {
                ...cellProps,
                value: model[column.prop],
                onInput: (event: Event) => {
                  model[column.prop] = (event.target as HTMLInputElement).value;
                },
                onKeydown: (event: KeyboardEvent) => {
                  if (event.key === "Enter") {
                    activeCell.value = "";
                    emit("afteredit", {
                      detail: {
                        prop: column.prop,
                        model,
                        val: model[column.prop],
                      },
                    });
                  }
                },
              });
            }
            return h(
              "div",
              {
                ...cellProps,
                tabindex: 0,
                onClick: () => {
                  if (!readonly) {
                    activeCell.value = key;
                    emit("beforeeditstart");
                  }
                },
                onKeydown: (event: KeyboardEvent) => {
                  if (
                    !readonly &&
                    (event.key === "Delete" || event.key === "Backspace")
                  ) {
                    model[column.prop] = "";
                    emit("afteredit", {
                      detail: { prop: column.prop, model, val: "" },
                    });
                  }
                },
              },
              column.cellTemplate?.(
                h,
                { model, prop: column.prop, value: model[column.prop] },
                props.additionalData,
              ) ??
                String(model[column.prop] ?? ""),
            );
          }),
        ),
      ]);
    };
  },
});

const globalStubs = {
  TableFirstPageLayout: PassThroughStub,
  TablePageCard: PassThroughStub,
  TableToolbarActions: PassThroughStub,
  Grid: GridStub,
  RevoGrid: GridStub,
  PeriodSwitcherControl: PassThroughStub,
  VBtn: PassThroughStub,
  VBtnGroup: PassThroughStub,
  VBtnToggle: ToggleStub,
  VTabs: PassThroughStub,
  VTab: PassThroughStub,
  VTable: TableStub,
  VTextField: PassThroughStub,
  VSelect: SelectStub,
  VAutocomplete: SelectStub,
  VSheet: PassThroughStub,
  VAlert: PassThroughStub,
  VChip: PassThroughStub,
  VDivider: PassThroughStub,
  VEmptyState: PassThroughStub,
  VDialog: DialogStub,
  VCheckbox: CheckboxStub,
  VCheckboxBtn: defineComponent({
    props: { modelValue: Boolean },
    emits: ["update:modelValue"],
    setup(props, { emit }) {
      return () => h("button", {
        role: "checkbox",
        "aria-checked": String(props.modelValue),
        onClick: () => emit("update:modelValue", !props.modelValue),
      });
    },
  }),
  VCard: PassThroughStub,
  VCardTitle: PassThroughStub,
  VCardText: PassThroughStub,
  VCardActions: PassThroughStub,
  VRow: PassThroughStub,
  VCol: PassThroughStub,
  VSpacer: PassThroughStub,
  VIcon: PassThroughStub,
  VListItem: ListItemStub,
  VTooltip: TooltipStub,
};

function getToggle(wrapper: VueWrapper, testId: string) {
  const toggle = wrapper.findAllComponents(ToggleStub)
    .find(component => component.attributes("data-testid") === testId);
  if (!toggle) throw new Error(`Toggle not found: ${testId}`);
  return toggle;
}

afterEach(() => {
  vi.clearAllMocks();
  vi.unstubAllGlobals();
});

beforeEach(() => {
  routerMocks.query = {};
  permissionMocks.canAdmin = false;
  vi.mocked(fetchClosedResourceAllocationPeriods).mockResolvedValue([]);
  vi.mocked(fetchResourceAllocationProjectInput).mockResolvedValue({
    year: 2026,
    selectedProjectId: null,
    months: Array.from({ length: 12 }, (_, month) => ({
      period: 202600 + month,
      closed: false,
    })),
    employees: [],
    projects: [],
    allocations: [],
    otherAllocations: [],
  });
});

describe("ResourceAllocationsView", () => {
  it("exports only year and units, prevents duplicate downloads and recovers after failure", async () => {
    routerMocks.query = { year: "2028", unit: "percent", search: "ignored", projectId: "401" };
    vi.mocked(fetchResourceAllocationAnalytics).mockResolvedValue({
      year: 2028, employees: [], projects: [], workstreams: [], allocations: [],
    });
    let rejectExport!: (error: Error) => void;
    vi.mocked(exportResourceAllocationAnalytics).mockReturnValueOnce(new Promise<void>((_, reject) => {
      rejectExport = reject;
    }));
    const ToolbarActionsStub = defineComponent({
      props: { disabled: Boolean },
      emits: ["export", "refresh"],
      setup() { return () => h("div"); },
    });
    const wrapper = mount(ResourceAllocationAnalyticsView, {
      global: { stubs: {
        ...globalStubs,
        TableToolbarActions: ToolbarActionsStub,
      } },
    });
    await flushPromises();
    const actions = wrapper.getComponent(ToolbarActionsStub);
    actions.vm.$emit("export");
    actions.vm.$emit("export");
    await flushPromises();
    expect(exportResourceAllocationAnalytics).toHaveBeenCalledExactlyOnceWith(2028, "percent");
    expect(actions.props("disabled")).toBe(true);
    rejectExport(new Error("Export failed"));
    await flushPromises();
    expect(wrapper.text()).toContain("Export failed");
    expect(actions.props("disabled")).toBe(false);
    vi.mocked(exportResourceAllocationAnalytics).mockResolvedValueOnce();
    getToggle(wrapper, "resource-allocations-display-unit")
      .vm.$emit("update:modelValue", "personMonths");
    await flushPromises();
    actions.vm.$emit("export");
    await flushPromises();
    expect(exportResourceAllocationAnalytics).toHaveBeenLastCalledWith(2028, "personMonths");
    wrapper.unmount();
  });

  it("restores analytics year and units from the URL and synchronizes changes", async () => {
    routerMocks.query = reactive({ year: "2028", unit: "percent", other: "keep" });
    vi.mocked(fetchResourceAllocationAnalytics).mockResolvedValue({
      year: 2028, employees: [], projects: [], workstreams: [], allocations: [],
    });
    const wrapper = mount(ResourceAllocationAnalyticsView, { global: { stubs: globalStubs } });
    await flushPromises();
    expect(fetchResourceAllocationAnalytics).toHaveBeenCalledWith(2028);
    const unitControl = getToggle(wrapper, "resource-allocations-display-unit");
    expect(unitControl.props("modelValue")).toBe("percent");
    const loads = vi.mocked(fetchResourceAllocationAnalytics).mock.calls.length;
    unitControl.vm.$emit("update:modelValue", "personMonths");
    await flushPromises();
    expect(routerMocks.replace).toHaveBeenLastCalledWith({
      query: { year: "2028", unit: "personMonths", other: "keep" },
    });
    expect(fetchResourceAllocationAnalytics).toHaveBeenCalledTimes(loads);

    routerMocks.query.year = "2029";
    routerMocks.query.unit = "personMonths";
    await flushPromises();
    expect(fetchResourceAllocationAnalytics).toHaveBeenLastCalledWith(2029);
    expect(fetchClosedResourceAllocationPeriods).toHaveBeenLastCalledWith(2029);
    expect(unitControl.props("modelValue")).toBe("personMonths");
    wrapper.unmount();
  });

  it("opens the first managed project and supports adding any employee", async () => {
    routerMocks.query = { year: "2026", projectId: "20", workstreamId: "11" };
    vi.mocked(fetchResourceAllocationProjectInput).mockResolvedValue({
      year: 2026,
      selectedProjectId: 20,
      selectedWorkstreamId: 11,
      workstreams: [{ id: 11, displayName: "Delivery" }],
      months: Array.from({ length: 12 }, (_, month) => ({
        period: 202600 + month,
        closed: month === 0,
      })),
      employees: [
        {
          id: 1,
          displayName: "Alex Morgan",
          currentProjectId: 20,
          currentProjectName: "Alpha",
          currentProjectRole: "Java Backend Developer",
          dateOfEmployment: "2020-01-01",
          dateOfDismissal: null,
          dismissed: false,
        },
        {
          id: 2,
          displayName: "Jordan Lee",
          currentProjectId: 30,
          currentProjectName: "Other",
          currentProjectRole: "QA Engineer",
          dateOfEmployment: "2020-01-01",
          dateOfDismissal: "2026-08-10",
          dismissed: true,
        },
        {
          id: 3,
          displayName: "Taylor Kim",
          currentProjectId: 30,
          currentProjectName: "Other",
          dateOfEmployment: "2020-01-01",
          dateOfDismissal: null,
          dismissed: false,
        },
        {
          id: 4,
          displayName: "Casey Smith",
          currentProjectId: 30,
          currentProjectName: "Other",
          dateOfEmployment: "2020-01-01",
          dateOfDismissal: null,
          dismissed: false,
        },
        {
          id: 5,
          displayName: "Morgan Reed",
          currentProjectId: 30,
          currentProjectName: "Other",
          dateOfEmployment: "2020-01-01",
          dateOfDismissal: null,
          dismissed: false,
        },
        {
          id: 6,
          displayName: "Riley Chen",
          currentProjectId: 30,
          currentProjectName: "Other",
          currentProjectRole: "QA Engineer",
          dateOfEmployment: "2020-01-01",
          dateOfDismissal: "2025-12-15",
          dismissed: true,
        },
      ],
      projects: [
        {
          id: 10,
          name: "Zulu",
          departmentId: null,
          departmentName: null,
          baId: null,
          baName: null,
          endDate: "2026-06-30",
          active: true,
          editable: true,
        },
        {
          id: 20,
          name: "Alpha",
          departmentId: null,
          departmentName: null,
          baId: null,
          baName: null,
          active: true,
          editable: true,
        },
      ],
      allocations: [
        { period: 202607, employeeId: 4, percent: 25, revisionId: 2 },
        { period: 202606, employeeId: 2, percent: 30, revisionId: 1 },
      ],
      otherAllocations: [
        { period: 202607, employeeId: 1, percent: 50, sameProject: false },
        { period: 202607, employeeId: 5, percent: 20, sameProject: true },
      ],
    });
    vi.mocked(saveResourceAllocations).mockResolvedValue();

    const wrapper = mount(ResourceAllocationInputView, {
      global: { stubs: globalStubs },
    });
    await flushPromises();
    expect(wrapper.text()).toContain("Закрыт: 30.06.2026");
    expect(wrapper.text()).toContain("Текущий проект: Other");
    expect(wrapper.text()).toContain("Роль: QA Engineer");
    expect(wrapper.text()).toContain("Дата увольнения: 15.12.2025");
    expect(wrapper.find(".mdi-lock").exists()).toBe(true);

    expect(fetchResourceAllocationProjectInput).toHaveBeenCalledWith(2026, 20, 11);
    expect(routerMocks.replace).toHaveBeenCalledWith({
      query: { year: "2026", projectId: "20", workstreamId: "11" },
    });

    const inputProject = wrapper
      .findAllComponents(SelectStub)
      .find(
        (component) =>
          component.attributes("data-testid") ===
          "resource-allocations-input-project",
      );
    expect(inputProject?.props("modelValue")).toBe(20);
    expect(
      wrapper
        .find('[data-testid="resource-allocation-input-1-202607"]')
        .text(),
    ).toContain("+ 50%");
    expect(
      wrapper
        .find('[data-testid="resource-allocation-input-2-202606"]')
        .exists(),
    ).toBe(true);
    expect(
      wrapper
        .find('[data-testid="resource-allocation-input-4-202607"]')
        .exists(),
    ).toBe(true);
    expect(
      wrapper
        .find('[data-testid="resource-allocation-input-5-202607"]')
        .exists(),
    ).toBe(true);
    expect(
      wrapper
        .find('[data-testid="resource-allocation-input-3-202607"]')
        .exists(),
    ).toBe(false);

    const addEmployeeFromGrid = wrapper
      .getComponent(GridStub)
      .props("additionalData")!.addEmployee as (employeeId: number) => void;
    addEmployeeFromGrid(3);
    await flushPromises();
    const grid = wrapper.getComponent(GridStub);
    const editableModel = grid.props("source")?.find(model => model.id === 3);
    for (const value of ["invalid", "10.5", "-1", "1001"]) {
      const invalidEdit = new CustomEvent("beforeedit", {
        cancelable: true,
        detail: { model: editableModel, prop: "month_202607", val: value },
      });
      grid.vm.$emit("beforeedit", invalidEdit);
      expect(invalidEdit.defaultPrevented).toBe(true);
    }
    await wrapper
      .get('[data-testid="resource-allocation-input-3-202607"]')
      .trigger("click");
    await wrapper
      .get('[data-testid="resource-allocation-input-3-202607"]')
      .setValue("0");
    await wrapper
      .get('[data-testid="resource-allocation-input-3-202607"]')
      .trigger("keydown", { key: "Enter" });
    const replaceCalls = routerMocks.replace.mock.calls.length;
    await wrapper.get('[data-testid="resource-allocation-open-employee-3"]').trigger("click");
    expect(wrapper.emitted("open-employee")).toEqual([[3]]);
    expect(routerMocks.replace).toHaveBeenCalledTimes(replaceCalls);
    expect(saveResourceAllocations).not.toHaveBeenCalled();
    await wrapper
      .get('[data-testid="resource-allocations-save"]')
      .trigger("click");
    await flushPromises();

    expect(saveResourceAllocations).toHaveBeenCalledWith(2026, 20, 11, [
      { period: 202607, employeeId: 3, percent: 0, expectedRevisionId: null },
    ]);
  });

  it("allows only clearing existing cells after dismissal and protects closed months", async () => {
    vi.mocked(fetchResourceAllocationProjectInput).mockResolvedValue({
      year: 2026,
      selectedProjectId: 10,
      months: Array.from({ length: 12 }, (_, month) => ({
        period: 202600 + month,
        closed: month === 10,
      })),
      employees: [
        {
          id: 1,
          displayName: "Alex Morgan",
          currentProjectId: 10,
          currentProjectName: "Alpha",
          dateOfEmployment: "2020-01-01",
          dateOfDismissal: "2026-08-10",
          dismissed: true,
        },
      ],
      projects: [
        {
          id: 10,
          name: "Alpha",
          departmentId: null,
          departmentName: null,
          baId: null,
          baName: null,
          active: true,
          editable: true,
        },
      ],
      allocations: [
        { period: 202609, employeeId: 1, percent: 0, revisionId: 5 },
        { period: 202610, employeeId: 1, percent: 50, revisionId: 6 },
      ],
      otherAllocations: [],
    });
    const wrapper = mount(ResourceAllocationInputView, {
      global: { stubs: globalStubs },
    });
    await flushPromises();

    const august = wrapper.get(
      '[data-testid="resource-allocation-input-1-202607"]',
    );
    const september = wrapper.get(
      '[data-testid="resource-allocation-input-1-202608"]',
    );
    await august.trigger("click");
    await september.trigger("click");
    expect(wrapper.findAll("input")).toHaveLength(1);
    expect(wrapper.text()).toContain("Уволен");
    const october = wrapper.get('[data-testid="resource-allocation-input-1-202609"]');
    expect(october.text()).toBe("0%");
    const grid = wrapper.getComponent(GridStub);
    const model = grid.props("source")?.[0];
    expect(model).toBeDefined();
    const numericEdit = new CustomEvent("beforeedit", {
      cancelable: true, detail: { model, prop: "month_202609", val: "0" },
    });
    grid.vm.$emit("beforeedit", numericEdit);
    expect(numericEdit.defaultPrevented).toBe(true);
    const rangeEdit = new CustomEvent("beforerangeedit", {
      cancelable: true, detail: { models: { 0: model }, data: { 0: { month_202609: "0" } } },
    });
    grid.vm.$emit("beforerangeedit", rangeEdit);
    expect(rangeEdit.defaultPrevented).toBe(true);
    await october.trigger("keydown", { key: "Delete" });
    await wrapper.get('[data-testid="resource-allocation-input-1-202610"]').trigger("keydown", { key: "Delete" });
    vi.mocked(saveResourceAllocations).mockResolvedValue();
    await wrapper.get('[data-testid="resource-allocations-save"]').trigger("click");
    await flushPromises();
    expect(saveResourceAllocations).toHaveBeenCalledWith(expect.any(Number), 10, null, [
      { period: 202609, employeeId: 1, percent: null, expectedRevisionId: 5 },
    ]);
    wrapper.unmount();
  });

  it("disables the grid throughout refresh", async () => {
    const response = await fetchResourceAllocationProjectInput(2026);
    vi.mocked(fetchResourceAllocationProjectInput).mockResolvedValue({
      ...response, selectedProjectId: 10,
      projects: [{ id: 10, name: "Alpha", departmentId: null, departmentName: null,
        baId: null, baName: null, active: true, editable: true }],
    });
    const wrapper = mount(ResourceAllocationInputView, { global: { stubs: {
      ...globalStubs,
      AdaptiveFilterBar: defineComponent({ setup(_, { slots }) {
        return () => h("div", Object.values(slots).map(slot => slot?.()));
      } }),
      TableToolbarActions: defineComponent({ emits: ["refresh"], setup(_, { emit }) {
        return () => h("button", { "data-testid": "refresh", onClick: () => emit("refresh") });
      } }),
    } } });
    await flushPromises();
    expect(wrapper.getComponent(GridStub).props("readonly")).toBe(false);
    let finishRefresh!: (value: typeof response) => void;
    vi.mocked(fetchResourceAllocationProjectInput).mockImplementationOnce(() =>
      new Promise(resolve => { finishRefresh = resolve; }));
    await wrapper.get('[data-testid="refresh"]').trigger("click");
    expect(wrapper.getComponent(GridStub).props("readonly")).toBe(true);
    finishRefresh(response);
    await flushPromises();
    wrapper.unmount();
  });

  it("builds an annual project hierarchy with group totals", async () => {
    vi.mocked(fetchResourceAllocationAnalytics).mockResolvedValue({
      year: 2026,
      employees: [
        {
          id: 1,
          displayName: "Alex Morgan",
          departmentId: null,
          departmentName: null,
          currentProjectId: 10,
          currentProjectName: "Managed",
          currentProjectRole: "Java Backend Developer",
        },
      ],
      projects: [
        {
          id: 10,
          name: "Managed",
          departmentId: null,
          departmentName: null,
          baId: null,
          baName: null,
          active: true,
          editable: true,
        },
        {
          id: 20,
          name: "Project only",
          departmentId: null,
          departmentName: null,
          baId: null,
          baName: null,
          active: true,
          editable: false,
        },
      ],
      workstreams: [{ id: 11, displayName: "Delivery" }],
      allocations: [
        { period: 202600, employeeId: 1, projectId: 10, workstreamId: 11, percent: 60 },
        { period: 202601, employeeId: 1, projectId: 10, percent: 40 },
        { period: 202602, employeeId: 1, projectId: 20, percent: 0 },
      ],
    });

    const wrapper = mount(ResourceAllocationAnalyticsView, {
      global: { stubs: globalStubs },
    });
    await flushPromises();

    const unitControl = getToggle(wrapper, "resource-allocations-display-unit");
    expect(unitControl.props("modelValue")).toBe("personMonths");
    const personMonthGrid = wrapper.getComponent(GridStub);
    const personMonthSummaries = personMonthGrid.props("additionalData") as unknown as Map<
      string,
      { months: Map<number, number>; yearTotal: number | null }
    >;
    expect(personMonthSummaries.get("ba:none")?.months.get(202600)).toBe(0.6);
    expect(personMonthSummaries.get("ba:none")?.yearTotal).toBe(1);
    expect(personMonthGrid.props("source")).toEqual(expect.arrayContaining([
      expect.objectContaining({ id: "1:10:11", month_202600: 0.6, yearTotal: 0.6 }),
    ]));
    expect(wrapper.get('[data-testid="resource-allocation-analytics-cell-1:10:11-year-total"]').text()).toBe("0,6");
    unitControl.vm.$emit("update:modelValue", "percent");
    await flushPromises();
    const grid = wrapper.getComponent(GridStub);
    expect(grid.props("columns")).toHaveLength(14);
    expect(grid.props("columns")?.[1]?.prop).toBe("yearTotal");
    expect(grid.props("stretch")).toBe(true);
    expect(grid.props("grouping")).toEqual(
      expect.objectContaining({
        props: ["businessAccountGroup", "projectGroup", "workstreamGroup"],
        expandedAll: false,
      }),
    );
    expect(grid.props("source")).toEqual(expect.arrayContaining([
      expect.objectContaining({
        businessAccountGroup: "ba:none",
        projectGroup: "project:10",
        entity: "Alex Morgan · Java Backend Developer",
        month_202600: 60,
        workstreamId: 11,
      }),
      expect.objectContaining({
        projectGroup: "project:10",
        month_202601: 40,
        workstreamId: null,
      }),
      expect.objectContaining({
        projectGroup: "project:20",
        workstreamGroup: "employee:1",
        month_202602: 0,
      }),
    ]));
    expect(wrapper.get('[data-testid="resource-allocation-analytics-row-1:10:11"]').classes())
      .toContain("resource-allocation-terminal-cell");
    expect(wrapper.get('[data-testid="resource-allocation-analytics-row-1:10:11"]').attributes("style"))
      .toContain("padding-left: 48px");
    const summaries = grid.props("additionalData") as unknown as Map<
      string,
      { label: string; months: Map<number, number>; projects: Set<string>; workstreams: Set<string>; dimensions: Set<string>; terminalRowId: string | null; yearTotal: number | null }
    >;
    expect(summaries.get("ba:none")?.months.get(202600)).toBe(60);
    expect(summaries.get("ba:none")?.yearTotal).toBe(100);
    expect(wrapper.get('[data-testid="resource-allocation-analytics-cell-1:10:11-year-total"]').text()).toBe("60%");
    expect(summaries.get("ba:none,project:20")?.months.get(202602)).toBe(0);
    expect(summaries.get("ba:none,project:10")?.months.get(202601)).toBe(40);
    expect(summaries.get("ba:none,project:10")?.workstreams).toEqual(new Set(["Delivery"]));
    expect(summaries.get("ba:none,project:10")?.dimensions).toEqual(new Set(["11", "project"]));
    expect(summaries.get("ba:none,project:20,employee:1")?.terminalRowId).toBe("1:20:project");
    const totalsToggle = wrapper.get('[data-testid="resource-allocations-group-totals"]');
    const originalRows = grid.props("source");
    const originalGrouping = grid.props("grouping");
    const loadsBeforeToggle = vi.mocked(fetchResourceAllocationAnalytics).mock.calls.length;
    await totalsToggle.trigger("click");
    const withoutTotals = grid.props("additionalData") as unknown as typeof summaries;
    expect(withoutTotals.get("ba:none")?.months.size).toBe(0);
    expect(withoutTotals.get("ba:none")?.yearTotal).toBeNull();
    expect(withoutTotals.get("ba:none,project:20,employee:1")?.yearTotal).toBe(0);
    expect(withoutTotals.get("ba:none,project:10")?.months.size).toBe(0);
    expect(withoutTotals.get("ba:none,project:10,workstream:10:11")?.months.size).toBe(0);
    expect(withoutTotals.get("ba:none,project:20,employee:1")?.months.get(202602)).toBe(0);
    expect(grid.props("source")).toBe(originalRows);
    expect(grid.props("grouping")).toBe(originalGrouping);
    expect(fetchResourceAllocationAnalytics).toHaveBeenCalledTimes(loadsBeforeToggle);
    await wrapper.findAll('[data-testid="resource-allocation-open-employee-1"]')[0]!.trigger("click");
    expect(wrapper.emitted("open-employee")).toEqual([[1]]);
    getToggle(wrapper, "resource-allocations-analytics-mode")
      .vm.$emit("update:modelValue", "employees");
    await flushPromises();
    const employeeGrid = wrapper.getComponent(GridStub);
    const employeeSummaries = employeeGrid.props("additionalData") as unknown as Map<
      string,
      { terminalRowId: string | null; months: Map<number, number> }
    >;
    expect(employeeSummaries.get("employee:1")?.months.size).toBe(0);
    expect(employeeSummaries.get("employee:1,project:10")?.months.size).toBe(0);
    expect(employeeSummaries.get("employee:1,project:20")?.months.get(202602)).toBe(0);
    await totalsToggle.trigger("click");
    const restoredTotals = employeeGrid.props("additionalData") as unknown as typeof employeeSummaries;
    expect(restoredTotals.get("employee:1")?.months.get(202600)).toBe(60);
    expect(employeeSummaries.get("employee:1,project:20")?.terminalRowId).toBe("1:20:project");
    expect(wrapper.get('[data-testid="resource-allocation-analytics-row-1:10:11"]').attributes("style"))
      .toContain("padding-left: 32px");
    expect(wrapper.findAll("input")).toHaveLength(0);

    const onExpand = vi.fn();
    const gridClick = vi.fn();
    const renderGroup = employeeGrid.props("grouping")?.groupCellTemplate;
    const group = mount(defineComponent({
      setup: () => () => h("div", { onClick: gridClick }, [renderGroup(h, {
        model: { [PSEUDO_GROUP_ITEM_VALUE]: "employee:1" },
        group: { isLabelColumn: true, depth: 0, expanded: false, onExpand },
      }, employeeSummaries)]),
    }));
    const profileButton = group.get('[data-testid="resource-allocation-open-employee-1"]');
    await profileButton.trigger("pointerdown");
    await profileButton.trigger("keydown", { key: "Enter" });
    await profileButton.trigger("click");
    expect(wrapper.emitted("open-employee")).toEqual([[1], [1]]);
    expect(onExpand).not.toHaveBeenCalled();
    expect(gridClick).not.toHaveBeenCalled();
    await group.get('[data-testid="resource-allocation-group-employee:1-label"]').trigger("click");
    expect(onExpand).toHaveBeenCalledOnce();
    expect(wrapper.emitted("open-employee")).toHaveLength(2);
    group.unmount();
    expect(fetchResourceAllocationAnalytics).toHaveBeenCalledTimes(loadsBeforeToggle);
    expect(wrapper.get('[data-testid="resource-allocation-analytics-cell-1:10:11-year-total"]').text()).toBe("60%");
    wrapper.unmount();
  });

  it("applies server conflicts and keeps unrelated local edits without another load", async () => {
    vi.mocked(fetchResourceAllocationProjectInput).mockResolvedValue({
      year: 2026,
      selectedProjectId: 10,
      months: Array.from({ length: 12 }, (_, month) => ({
        period: 202600 + month,
        closed: false,
      })),
      employees: [
        {
          id: 1,
          displayName: "Alex Morgan",
          currentProjectId: 10,
          currentProjectName: "Managed A",
          dateOfEmployment: "2020-01-01",
          dateOfDismissal: null,
          dismissed: false,
        },
      ],
      projects: [
        {
          id: 10,
          name: "Managed A",
          departmentId: null,
          departmentName: null,
          baId: null,
          baName: null,
          active: true,
          editable: true,
        },
      ],
      allocations: [
        { period: 202600, employeeId: 1, percent: 60, revisionId: 1 },
        { period: 202601, employeeId: 1, percent: 20, revisionId: 1 },
      ],
      otherAllocations: [
        { period: 202600, employeeId: 1, percent: 40, sameProject: false },
      ],
    });
    vi.mocked(saveResourceAllocations).mockRejectedValue(
      new BusinessError(
        "Аллокации были изменены другим пользователем",
        "errors.resource_allocation.conflict",
        {
          allocations: [
            { period: 202600, employeeId: 1, percent: 80, revisionId: 2 },
            { period: 202601, employeeId: 1, percent: 20, revisionId: 1 },
          ],
          changes: [
            {
              period: 202601,
              employeeId: 1,
              percent: null,
              expectedRevisionId: 1,
            },
          ],
          conflicts: [{ period: 202600, employeeId: 1 }],
        },
      ),
    );
    const wrapper = mount(ResourceAllocationInputView, {
      global: { stubs: globalStubs },
    });
    await flushPromises();

    await wrapper
      .get('[data-testid="resource-allocation-input-1-202600"]')
      .trigger("keydown", { key: "Delete" });
    await wrapper
      .get('[data-testid="resource-allocation-input-1-202601"]')
      .trigger("keydown", { key: "Delete" });
    await wrapper
      .get('[data-testid="resource-allocations-save"]')
      .trigger("click");
    await flushPromises();

    expect(fetchResourceAllocationProjectInput).toHaveBeenCalledTimes(1);
    expect(
      wrapper.get('[data-testid="resource-allocation-input-1-202600"]').text(),
    ).toBe("80%+ 40%");
    expect(
      wrapper.get('[data-testid="resource-allocation-input-1-202601"]').text(),
    ).toBe("");
    expect(
      wrapper
        .get('[data-testid="resource-allocations-conflict"]')
        .attributes("title"),
    ).toBe("Не все изменения сохранены");
    expect(wrapper.text()).toContain("уже изменил другой пользователь");
    expect(
      wrapper
        .get('[data-testid="resource-allocation-input-1-202600"]')
        .attributes("data-conflict"),
    ).toBe("true");
    expect(
      wrapper
        .get('[data-testid="resource-allocations-save"]')
        .attributes("disabled"),
    ).toBe("false");
  });

  it("filters whole allocation groups by BA and switches hierarchy direction", async () => {
    vi.mocked(fetchResourceAllocationAnalytics).mockResolvedValue({
      year: 2026,
      employees: [
        {
          id: 1,
          displayName: "Managed Employee",
          departmentId: null,
          departmentName: null,
          currentProjectId: 10,
          currentProjectName: "Project A",
        },
        {
          id: 2,
          displayName: "Other Employee",
          departmentId: null,
          departmentName: null,
          currentProjectId: 30,
          currentProjectName: "Read only",
        },
      ],
      projects: [
        {
          id: 10,
          name: "Project A",
          departmentId: null,
          departmentName: null,
          baId: 100,
          baName: "BA A",
          active: true,
          editable: true,
        },
        {
          id: 20,
          name: "Project B",
          departmentId: null,
          departmentName: null,
          baId: 200,
          baName: "BA B",
          active: true,
          editable: true,
        },
        {
          id: 30,
          name: "Manual access",
          departmentId: null,
          departmentName: null,
          baId: 200,
          baName: "BA B",
          active: true,
          editable: true,
        },
      ],
      workstreams: [{ id: 31, displayName: "Operations" }],
      allocations: [
        { period: 202600, employeeId: 1, projectId: 10, percent: 40 },
        { period: 202601, employeeId: 2, projectId: 30, workstreamId: 31, percent: 50 },
      ],
    });

    const wrapper = mount(ResourceAllocationAnalyticsView, {
      global: { stubs: globalStubs },
    });
    await flushPromises();

    expect(wrapper.getComponent(GridStub).props("source")).toHaveLength(2);
    const businessAccounts = wrapper
      .findAllComponents(SelectStub)
      .find(
        (component) =>
          component.attributes("data-testid") ===
          "resource-allocations-business-accounts",
      );
    const projects = wrapper
      .findAllComponents(SelectStub)
      .find(
        (component) =>
          component.attributes("data-testid") ===
          "resource-allocations-projects",
      );
    businessAccounts?.vm.$emit("update:modelValue", [200]);
    await flushPromises();
    expect(projects?.props("items")).toEqual([
      expect.objectContaining({ id: 30 }),
      expect.objectContaining({ id: 20 }),
    ]);
    expect(wrapper.getComponent(GridStub).props("source")).toEqual([
      expect.objectContaining({
        projectGroup: "project:30",
        entity: "Other Employee",
      }),
    ]);

    getToggle(wrapper, "resource-allocations-analytics-mode")
      .vm.$emit("update:modelValue", "employees");
    await flushPromises();
    expect(wrapper.getComponent(GridStub).props("grouping")).toEqual(
      expect.objectContaining({
        props: ["employeeGroup", "projectGroup"],
        expandedAll: false,
      }),
    );
    expect(wrapper.getComponent(GridStub).props("source")).toEqual([
      expect.objectContaining({
        employeeGroup: "employee:2",
        entity: "Operations",
      }),
    ]);
  });

  it("saves the annual closed-period selection from analytics", async () => {
    permissionMocks.canAdmin = true;
    vi.mocked(fetchClosedResourceAllocationPeriods).mockResolvedValue([202600]);
    vi.mocked(saveClosedResourceAllocationPeriods).mockResolvedValue([202600, 202601]);
    vi.mocked(fetchResourceAllocationAnalytics).mockResolvedValue({
      year: 2026,
      employees: [],
      projects: [],
      workstreams: [],
      allocations: [],
    });

    const wrapper = mount(ResourceAllocationAnalyticsView, {
      global: { stubs: globalStubs },
    });
    await flushPromises();

    await wrapper.get('[data-testid="resource-allocations-period-toggle"]').trigger("click");
    expect((wrapper.get('[data-testid="resource-allocations-period-202600"]').element as HTMLInputElement).checked)
      .toBe(true);
    await wrapper.get('[data-testid="resource-allocations-period-202601"]').setValue(true);
    await wrapper.get('[data-testid="resource-allocations-periods-save"]').trigger("click");
    await flushPromises();

    expect(saveClosedResourceAllocationPeriods).toHaveBeenCalledWith(2026, [202600, 202601]);
  });
});
