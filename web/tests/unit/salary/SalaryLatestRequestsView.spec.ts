import { describe, expect, it, vi } from "vitest";
import { mount } from "@vue/test-utils";
import { defineComponent, h, nextTick, type PropType } from "vue";
import SalaryLatestRequestsView from "@/views/salary/SalaryLatestRequestsView.vue";

vi.mock("vue-i18n", () => ({
  useI18n: () => ({
    t: (key: string) => key,
  }),
}));

vi.mock("@/lib/permissions", () => ({
  usePermissions: () => ({
    canAdminSalaryRequests: () => true,
  }),
}));

vi.mock("@/services/salary.service", async () => {
  const actual = await vi.importActual<typeof import("@/services/salary.service")>(
    "@/services/salary.service",
  );
  return {
    ...actual,
    fetchEmployeesWithLatestSalaryRequest: vi.fn().mockResolvedValue([]),
  };
});

vi.mock("@/services/dict.service", () => ({
  fetchBusinessAccounts: vi.fn().mockResolvedValue([]),
}));

const simpleStub = (name: string) =>
  defineComponent({
    name: `${name}Stub`,
    setup(_, { attrs, slots }) {
      return () => h("div", { ...attrs, class: [name, attrs.class] }, slots.default?.());
    },
  });

const TablePageCardStub = defineComponent({
  name: "TablePageCardStub",
  setup(_, { attrs, slots }) {
    return () => h("div", attrs, [slots.default?.(), slots.table?.()]);
  },
});

const AdaptiveFilterBarStub = defineComponent({
  name: "AdaptiveFilterBarStub",
  setup(_, { slots }) {
    return () =>
      h("div", { "data-test": "adaptive-filter-bar" }, [
        slots["left-actions"]?.(),
        slots["filter-search"]?.(),
        slots["filter-ba"]?.(),
        slots["filter-current-project"]?.(),
        slots["filter-only-with-requests"]?.(),
      ]);
  },
});

const TextFieldStub = defineComponent({
  name: "VTextFieldStub",
  props: {
    modelValue: {
      type: String as PropType<string | null>,
      default: "",
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit, attrs }) {
    return () =>
      h("input", {
        ...attrs,
        value: props.modelValue ?? "",
        onInput: (event: Event) => {
          emit("update:modelValue", (event.target as HTMLInputElement).value);
        },
      });
  },
});

const AutocompleteStub = defineComponent({
  name: "VAutocompleteStub",
  props: {
    modelValue: {
      type: Array,
      default: () => [],
    },
  },
  emits: ["update:modelValue"],
  setup() {
    return () => h("div");
  },
});

const SelectStub = defineComponent({
  name: "VSelectStub",
  props: {
    modelValue: {
      type: Boolean,
      default: true,
    },
  },
  emits: ["update:modelValue"],
  setup() {
    return () => h("div");
  },
});

const HREasyTableBaseStub = defineComponent({
  name: "HREasyTableBaseStub",
  props: {
    items: {
      type: Array,
      default: () => [],
    },
  },
  setup(props) {
    return () => h("div", { "data-test": "salary-latest-table" }, String(props.items.length));
  },
});

function mountView(errors: unknown[]) {
  return mount(SalaryLatestRequestsView, {
    global: {
      config: {
        errorHandler(error) {
          errors.push(error);
        },
      },
      stubs: {
        RouterLink: simpleStub("router-link"),
        TableFirstPageLayout: simpleStub("table-first-page-layout"),
        TableFirstPageState: simpleStub("table-first-page-state"),
        TablePageCard: TablePageCardStub,
        AdaptiveFilterBar: AdaptiveFilterBarStub,
        TableToolbarActions: simpleStub("table-toolbar-actions"),
        CollapsedSelectionContent: simpleStub("collapsed-selection-content"),
        HREasyTableBase: HREasyTableBaseStub,
        VAlert: simpleStub("v-alert"),
        VTextField: TextFieldStub,
        VAutocomplete: AutocompleteStub,
        VSelect: SelectStub,
      },
    },
  });
}

describe("SalaryLatestRequestsView", () => {
  it("keeps search stable when Vuetify clear emits null", async () => {
    const errors: unknown[] = [];
    const wrapper = mountView(errors);
    const search = wrapper.getComponent(TextFieldStub);

    await search.vm.$emit("update:modelValue", "alex");
    await nextTick();
    expect(errors).toHaveLength(0);

    await search.vm.$emit("update:modelValue", null);
    await nextTick();

    expect(errors).toHaveLength(0);
  });
});
