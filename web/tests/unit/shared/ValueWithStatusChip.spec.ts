import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import { defineComponent, h } from "vue";
import ValueWithStatusChip from "@/components/shared/ValueWithStatusChip.vue";

const VChipStub = defineComponent({
  name: "VChipStub",
  setup(_, { slots, attrs }) {
    return () => h("div", { class: ["v-chip-stub", attrs.color] }, slots.default?.());
  },
});

function mountChip(status: string | null, value = 5) {
  return mount(ValueWithStatusChip<number>, {
    props: {
      value: {
        value,
        status,
      },
    },
    global: {
      stubs: {
        VChip: VChipStub,
      },
    },
  });
}

describe("ValueWithStatusChip", () => {
  it("renders plain value for ok status", () => {
    const wrapper = mountChip("1", 3);
    expect(wrapper.text()).toContain("3");
    expect(wrapper.find(".v-chip-stub").exists()).toBe(false);
  });

  it("renders warning chip for warning status", () => {
    const wrapper = mountChip("2", 4);
    expect(wrapper.find(".v-chip-stub.warning").exists()).toBe(true);
    expect(wrapper.text()).toContain("4");
  });

  it("renders error chip for error status", () => {
    const wrapper = mountChip("3", 6);
    expect(wrapper.find(".v-chip-stub.error").exists()).toBe(true);
    expect(wrapper.text()).toContain("6");
  });
});
