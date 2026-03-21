<template>
  <div class="h-reasy-table-base">
    <div v-if="hasFiltersSlot">
      <slot name="filters" />
    </div>

    <slot name="before-table" />

    <v-data-table-virtual
      :class="tableClass"
      :headers="headers"
      :items="items"
      :item-key="itemKey"
      :height="height"
      :fixed-header="fixedHeader"
      :density="density"
      :loading="loading"
      :loading-text="loadingText"
      :no-data-text="noDataText"
      :hover="hover"
      :sort-by="sortBy"
      :multi-sort="multiSort"
      :row-props="rowProps"
      @click:row="onClickRow"
    >
      <template
        v-for="slotName in forwardedSlots"
        :key="slotName"
        #[slotName]="slotProps"
      >
        <slot :name="slotName" v-bind="slotProps" />
      </template>
    </v-data-table-virtual>
  </div>
</template>

<script setup lang="ts" generic="TItem = unknown">
import { computed, useSlots } from "vue";
import type { VDataTable } from "vuetify/components";

type DataTableHeader = VDataTable["$props"]["headers"];
type DataTableSortBy = VDataTable["$props"]["sortBy"];
type DataTableDensity = "default" | "comfortable" | "compact";
type DataTableRowProps = VDataTable["$props"]["rowProps"];

withDefaults(
  defineProps<{
    headers: DataTableHeader;
    items: TItem[];
    loading?: boolean;
    loadingText?: string;
    noDataText?: string;
    height?: number | string;
    fixedHeader?: boolean;
    density?: DataTableDensity;
    hover?: boolean;
    multiSort?: boolean;
    sortBy?: DataTableSortBy;
    rowProps?: DataTableRowProps;
    itemKey?: string;
    tableClass?: string;
  }>(),
  {
    loading: false,
    loadingText: "",
    noDataText: "",
    height: undefined,
    fixedHeader: false,
    density: "default",
    hover: false,
    multiSort: false,
    sortBy: undefined,
    rowProps: undefined,
    itemKey: "id",
    tableClass: "",
  },
);

const emit = defineEmits<{
  (event: "click:row", eventPayload: Event, rowPayload: unknown): void;
}>();

const slots = useSlots();

const hasFiltersSlot = computed(() => Boolean(slots.filters));
const forwardedSlots = computed(() =>
  Object.keys(slots).filter(
    (name) => name.startsWith("item.") || name.startsWith("header."),
  ),
);

function onClickRow(eventPayload: Event, rowPayload: unknown) {
  emit("click:row", eventPayload, rowPayload);
}
</script>
