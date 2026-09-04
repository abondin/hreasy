import { defineComponent, h, ref, type PropType } from "vue";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { BusinessError } from "@/lib/errors";
import ResourceAllocationAnalyticsView from "@/views/allocations/ResourceAllocationAnalyticsView.vue";
import ResourceAllocationInputView from "@/views/allocations/ResourceAllocationInputView.vue";
import {
  fetchResourceAllocationAnalytics,
  fetchResourceAllocationProjectInput,
  saveResourceAllocations,
} from "@/services/resource-allocation.service";

const routerMocks = vi.hoisted(() => ({
  query: {} as Record<string, string>,
  replace: vi.fn().mockResolvedValue(undefined),
}));

vi.mock("vue-router", async () => ({
  ...await vi.importActual<typeof import("vue-router")>("vue-router"),
  onBeforeRouteLeave: vi.fn(),
  useRoute: () => ({ query: routerMocks.query }),
  useRouter: () => ({ replace: routerMocks.replace }),
}));

vi.mock("vue-i18n", () => ({
  useI18n: () => ({ t: (key: string) => key }),
}));

vi.mock("@/services/resource-allocation.service", () => ({
  fetchResourceAllocationAnalytics: vi.fn(),
  fetchResourceAllocationProjectInput: vi.fn(),
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
  setup(_, { attrs }) {
    return () => h("div", attrs);
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

interface GridColumnStub {
  prop: string;
  name?: string;
  children?: GridColumnStub[];
  readonly?: boolean | ((props: { model: Record<string, unknown> }) => boolean);
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
          h("div", { role: "columnheader" }, column.name),
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
  VCheckbox: PassThroughStub,
  VSheet: PassThroughStub,
  VSpacer: PassThroughStub,
  VAlert: PassThroughStub,
  VChip: PassThroughStub,
  VDivider: PassThroughStub,
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

beforeEach(() => {
  routerMocks.query = {};
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
    canManagePeriods: false,
  });
});

describe("ResourceAllocationsView", () => {
  it("opens the first managed project and supports adding any employee", async () => {
    routerMocks.query = { year: "2026", projectId: "20" };
    vi.mocked(fetchResourceAllocationProjectInput).mockResolvedValue({
      year: 2026,
      selectedProjectId: 20,
      months: Array.from({ length: 12 }, (_, month) => ({
        period: 202600 + month,
        closed: false,
      })),
      employees: [
        {
          id: 1,
          displayName: "Alex Morgan",
          currentProjectId: 20,
          currentProjectName: "Alpha",
          dateOfEmployment: "2020-01-01",
          dateOfDismissal: null,
          dismissed: false,
        },
        {
          id: 2,
          displayName: "Jordan Lee",
          currentProjectId: 30,
          currentProjectName: "Other",
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
      ],
      projects: [
        {
          id: 10,
          name: "Zulu",
          departmentId: null,
          departmentName: null,
          baId: null,
          baName: null,
          active: true,
          editable: true,
          managed: true,
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
          managed: true,
        },
      ],
      allocations: [
        { period: 202607, employeeId: 4, percent: 25, revisionId: 2 },
        { period: 202606, employeeId: 2, percent: 30, revisionId: 1 },
      ],
      otherAllocations: [
        { period: 202607, employeeId: 1, percent: 50 },
      ],
      canManagePeriods: false,
    });
    vi.mocked(saveResourceAllocations).mockResolvedValue();

    const wrapper = mount(ResourceAllocationInputView, {
      global: { stubs: globalStubs },
    });
    await flushPromises();

    expect(fetchResourceAllocationProjectInput).toHaveBeenCalledWith(2026, 20);
    expect(routerMocks.replace).toHaveBeenCalledWith({
      query: { year: "2026", projectId: "20" },
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
        .find('[data-testid="resource-allocation-input-3-202607"]')
        .exists(),
    ).toBe(false);

    const addEmployeeFromGrid = wrapper
      .getComponent(GridStub)
      .props("additionalData")!.addEmployee as (employeeId: number) => void;
    addEmployeeFromGrid(3);
    await flushPromises();
    await wrapper
      .get('[data-testid="resource-allocation-input-3-202607"]')
      .trigger("click");
    await wrapper
      .get('[data-testid="resource-allocation-input-3-202607"]')
      .setValue("40");
    await wrapper
      .get('[data-testid="resource-allocation-input-3-202607"]')
      .trigger("keydown", { key: "Enter" });
    await wrapper
      .get('[data-testid="resource-allocations-save"]')
      .trigger("click");
    await flushPromises();

    expect(saveResourceAllocations).toHaveBeenCalledWith(2026, 20, [
      { period: 202607, employeeId: 3, percent: 40, expectedRevisionId: null },
    ]);
  });

  it("allows the dismissal month and blocks later months", async () => {
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
          managed: true,
        },
      ],
      allocations: [],
      otherAllocations: [],
      canManagePeriods: false,
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
          managed: true,
        },
        {
          id: 20,
          name: "No data",
          departmentId: null,
          departmentName: null,
          baId: null,
          baName: null,
          active: true,
          editable: false,
          managed: false,
        },
      ],
      allocations: [
        { period: 202600, employeeId: 1, projectId: 10, percent: 60 },
        { period: 202601, employeeId: 1, projectId: 10, percent: 40 },
      ],
    });

    const wrapper = mount(ResourceAllocationAnalyticsView, {
      global: { stubs: globalStubs },
    });
    await flushPromises();

    const grid = wrapper.getComponent(GridStub);
    expect(grid.props("columns")).toHaveLength(13);
    expect(grid.props("grouping")).toEqual(
      expect.objectContaining({
        props: ["businessAccountGroup", "projectGroup"],
      }),
    );
    expect(grid.props("source")).toEqual([
      expect.objectContaining({
        businessAccountGroup: "ba:none",
        projectGroup: "project:10",
        entity: "Alex Morgan",
        month_202600: 60,
        month_202601: 40,
      }),
    ]);
    const summaries = grid.props("additionalData") as unknown as Map<
      string,
      { label: string; months: Map<number, number>; projects: Set<string> }
    >;
    expect(summaries.get("ba:none")?.months.get(202600)).toBe(60);
    expect(summaries.get("ba:none,project:10")?.months.get(202601)).toBe(40);
    expect(wrapper.findAll("input")).toHaveLength(0);
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
          managed: true,
        },
      ],
      allocations: [
        { period: 202600, employeeId: 1, percent: 60, revisionId: 1 },
        { period: 202601, employeeId: 1, percent: 20, revisionId: 1 },
      ],
      otherAllocations: [
        { period: 202600, employeeId: 1, percent: 40 },
      ],
      canManagePeriods: false,
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
              percent: 0,
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
          managed: true,
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
          managed: true,
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
          managed: false,
        },
      ],
      allocations: [
        { period: 202600, employeeId: 1, projectId: 10, percent: 40 },
        { period: 202601, employeeId: 2, projectId: 30, percent: 50 },
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

    wrapper.getComponent(ToggleStub).vm.$emit("update:modelValue", "employees");
    await flushPromises();
    expect(wrapper.getComponent(GridStub).props("grouping")).toEqual(
      expect.objectContaining({ props: ["employeeGroup"], expandedAll: false }),
    );
    expect(wrapper.getComponent(GridStub).props("source")).toEqual([
      expect.objectContaining({
        employeeGroup: "employee:2",
        entity: "Manual access",
      }),
    ]);
  });
});
