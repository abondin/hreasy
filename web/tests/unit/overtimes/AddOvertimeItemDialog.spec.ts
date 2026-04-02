import { beforeEach, describe, expect, it, vi } from "vitest";
import { mount } from "@vue/test-utils";
import { defineComponent, h, nextTick } from "vue";
import AddOvertimeItemDialog from "@/components/overtimes/AddOvertimeItemDialog.vue";
import type { OvertimeReport } from "@/services/overtime.service";

const mockAddOvertimeItem = vi.hoisted(() => vi.fn());

vi.mock("vue-i18n", () => ({
  useI18n: () => ({
    t: (key: string) => key,
  }),
}));

vi.mock("@/services/overtime.service", async () => {
  const actual = await vi.importActual<typeof import("@/services/overtime.service")>(
    "@/services/overtime.service",
  );
  return {
    ...actual,
    addOvertimeItem: mockAddOvertimeItem,
  };
});

const DialogStub = defineComponent({
  name: "VDialogStub",
  props: {
    modelValue: {
      type: Boolean,
      default: false,
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit, slots }) {
    return () =>
      h("div", { class: "v-dialog-stub" }, [
        slots.activator?.({
          props: {
            onClick: () => emit("update:modelValue", true),
          },
        }),
        props.modelValue ? h("div", { "data-test": "dialog-content" }, slots.default?.()) : null,
      ]);
  },
});

const ButtonStub = defineComponent({
  name: "VBtnStub",
  props: {
    disabled: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  emits: ["click"],
  setup(props, { attrs, emit, slots }) {
    return () =>
      h(
        "button",
        {
          ...attrs,
          type: "button",
          disabled: props.disabled || props.loading,
          onClick: (event: Event) => {
            if (!props.disabled && !props.loading) {
              emit("click", event);
            }
          },
        },
        slots.default?.(),
      );
  },
});

const SliderStub = defineComponent({
  name: "VSliderStub",
  props: {
    modelValue: {
      type: Number,
      default: 0,
    },
    min: {
      type: Number,
      default: 0,
    },
    max: {
      type: Number,
      default: 100,
    },
    step: {
      type: Number,
      default: 1,
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit, attrs }) {
    return () =>
      h("input", {
        ...attrs,
        "data-test": "hours-slider",
        type: "number",
        min: props.min,
        max: props.max,
        step: props.step,
        value: props.modelValue,
        onInput: (event: Event) => {
          const target = event.target as HTMLInputElement;
          emit("update:modelValue", Number(target.value));
        },
      });
  },
});

const AutocompleteStub = defineComponent({
  name: "VAutocompleteStub",
  props: {
    modelValue: {
      type: [Number, String, null],
      default: null,
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit, attrs }) {
    return () =>
      h("input", {
        ...attrs,
        "data-test": "project-input",
        value: props.modelValue ?? "",
        onInput: (event: Event) => {
          const target = event.target as HTMLInputElement;
          emit("update:modelValue", Number(target.value));
        },
      });
  },
});

const TextFieldStub = defineComponent({
  name: "VTextFieldStub",
  props: {
    modelValue: {
      type: String,
      default: "",
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit, attrs, slots }) {
    return () =>
      h("div", { class: "v-text-field-stub" }, [
        slots.prepend?.(),
        h("input", {
          ...attrs,
          "data-test": "date-input",
          value: props.modelValue,
          onInput: (event: Event) => {
            const target = event.target as HTMLInputElement;
            emit("update:modelValue", target.value);
          },
        }),
      ]);
  },
});

const TextareaStub = defineComponent({
  name: "VTextareaStub",
  props: {
    modelValue: {
      type: String,
      default: "",
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit, attrs }) {
    return () =>
      h("textarea", {
        ...attrs,
        value: props.modelValue,
        onInput: (event: Event) => {
          const target = event.target as HTMLTextAreaElement;
          emit("update:modelValue", target.value);
        },
      });
  },
});

const CheckboxStub = defineComponent({
  name: "VCheckboxStub",
  props: {
    modelValue: {
      type: Boolean,
      default: false,
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit, attrs }) {
    return () =>
      h("input", {
        ...attrs,
        type: "checkbox",
        checked: props.modelValue,
        onChange: (event: Event) => {
          const target = event.target as HTMLInputElement;
          emit("update:modelValue", target.checked);
        },
      });
  },
});

const simpleStub = (name: string) =>
  defineComponent({
    name: `${name}Stub`,
    setup(_, { attrs, slots }) {
      return () =>
        h(
          "div",
          {
            ...attrs,
            class: [`${name}-stub`, attrs.class],
          },
          slots.default?.(),
        );
    },
  });

function createReport(): OvertimeReport {
  return {
    employeeId: 42,
    period: 202602,
    items: [],
    approvals: [],
    lastUpdate: null,
  };
}

function mountDialog() {
  return mount(AddOvertimeItemDialog, {
    props: {
      employeeId: 42,
      periodId: 202602,
      periodClosed: false,
      defaultProject: 7,
      allProjects: [{ id: 7, name: "Project 7", active: true }],
    },
    global: {
      stubs: {
        VDialog: DialogStub,
        VBtn: ButtonStub,
        VSlider: SliderStub,
        VAutocomplete: AutocompleteStub,
        VTextField: TextFieldStub,
        VTextarea: TextareaStub,
        VCheckbox: CheckboxStub,
        VCard: simpleStub("v-card"),
        VCardTitle: simpleStub("v-card-title"),
        VCardText: simpleStub("v-card-text"),
        VCardActions: simpleStub("v-card-actions"),
        VAlert: simpleStub("v-alert"),
        VSpacer: simpleStub("v-spacer"),
      },
    },
  });
}

async function openDialog(wrapper: ReturnType<typeof mountDialog>): Promise<void> {
  await wrapper.find("button").trigger("click");
  await nextTick();
}

function getSubmitButton(wrapper: ReturnType<typeof mountDialog>) {
  const buttons = wrapper.findAll("button");
  const submitButton = buttons[buttons.length - 1];
  if (!submitButton) {
    throw new Error("Submit button not found");
  }
  return submitButton;
}

describe("AddOvertimeItemDialog", () => {
  beforeEach(() => {
    mockAddOvertimeItem.mockReset();
    mockAddOvertimeItem.mockResolvedValue(createReport());
  });

  it("configures hours slider to integer values only", async () => {
    const wrapper = mountDialog();

    await openDialog(wrapper);

    const slider = wrapper.get('[data-test="hours-slider"]');
    expect(slider.attributes("min")).toBe("1");
    expect(slider.attributes("max")).toBe("24");
    expect(slider.attributes("step")).toBe("1");
  });

  it("rejects fractional hours", async () => {
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await wrapper.get('[data-test="hours-slider"]').setValue("1.5");
    await getSubmitButton(wrapper).trigger("click");
    await nextTick();

    expect(mockAddOvertimeItem).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain("Часы обязательны");
  });

  it("submits integer hours within range", async () => {
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await wrapper.get('[data-test="hours-slider"]').setValue("2");
    await getSubmitButton(wrapper).trigger("click");

    expect(mockAddOvertimeItem).toHaveBeenCalledTimes(1);
    expect(mockAddOvertimeItem).toHaveBeenCalledWith(
      42,
      202602,
      expect.objectContaining({
        projectId: 7,
        hours: 2,
      }),
    );
  });
});
