import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import SearchTextField from "@/components/shared/SearchTextField.vue";
import { createSearchSettings } from "@/lib/search";

vi.mock("vue-i18n", () => ({
  useI18n: () => ({ t: (value: string) => value }),
}));

describe("SearchTextField", () => {
  it("updates search settings from the magnifier menu", async () => {
    const wrapper = mount(SearchTextField, {
      props: {
        modelValue: "",
        settings: createSearchSettings(),
        label: "Search",
      },
      global: {
        stubs: {
          VTextField: { template: '<div><slot name="prepend-inner" /></div>' },
          VMenu: { template: '<div><slot name="activator" :props="{}" /><slot /></div>' },
          VBtn: { template: "<button><slot /></button>" },
          VIcon: true,
          VList: { template: "<div><slot /></div>" },
          VListItem: { template: '<button class="v-list-item" @click="$emit(\'click\')"><slot /></button>' },
          VCheckboxBtn: true,
          VListItemTitle: { template: "<span><slot /></span>" },
        },
      },
    });

    await wrapper.get(".v-list-item").trigger("click");

    expect(wrapper.emitted("update:settings")?.[0]).toEqual([{
      fuzzy: false,
      keyboardLayout: true,
      unorderedTerms: true,
    }]);
  });
});
