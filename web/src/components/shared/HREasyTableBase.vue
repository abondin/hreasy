<template>
  <div class="h-reasy-table-base d-flex flex-column h-100 min-h-0">
    <div v-if="hasFiltersSlot">
      <slot name="filters" />
    </div>

    <div v-if="hasBeforeTableSlot">
      <slot name="before-table" />
    </div>

    <div ref="tableAreaRef" class="h-reasy-table-base__table-area flex-grow-1 min-h-0 overflow-hidden">
      <v-data-table-virtual
        :class="['h-reasy-table-base__table', tableClass]"
        :headers="headers"
        :items="items"
        :item-key="itemKey"
        :height="resolvedHeight"
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
  </div>
</template>

<script setup lang="ts" generic="TItem = unknown">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, useSlots, watch } from "vue";
import type { VDataTable } from "vuetify/components";

type DataTableHeader = VDataTable["$props"]["headers"];
type DataTableSortBy = VDataTable["$props"]["sortBy"];
type DataTableDensity = "default" | "comfortable" | "compact";
type DataTableRowProps = VDataTable["$props"]["rowProps"];

const props = withDefaults(
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
const tableAreaRef = ref<HTMLElement | null>(null);
const fillHeight = ref<number | undefined>(undefined);

let tableAreaResizeObserver: ResizeObserver | null = null;

const hasFiltersSlot = computed(() => Boolean(slots.filters));
const hasBeforeTableSlot = computed(() => Boolean(slots["before-table"]));
const forwardedSlots = computed(() =>
  Object.keys(slots).filter(
    (name) => name.startsWith("item.") || name.startsWith("header."),
  ),
);
const resolvedHeight = computed(() => {
  if (props.height !== "fill") {
    return props.height;
  }
  return fillHeight.value != null ? `${fillHeight.value}px` : undefined;
});

onMounted(async () => {
  await nextTick();
  recalculateFillHeight();
  window.addEventListener("resize", recalculateFillHeight);

  if (typeof ResizeObserver !== "undefined" && tableAreaRef.value) {
    tableAreaResizeObserver = new ResizeObserver(() => {
      recalculateFillHeight();
    });
    tableAreaResizeObserver.observe(tableAreaRef.value);
  }
});

watch(
  () => [hasFiltersSlot.value, hasBeforeTableSlot.value, props.height],
  async () => {
    await nextTick();
    recalculateFillHeight();
  },
);

onBeforeUnmount(() => {
  window.removeEventListener("resize", recalculateFillHeight);
  tableAreaResizeObserver?.disconnect();
});

function recalculateFillHeight(): void {
  if (props.height !== "fill") {
    fillHeight.value = undefined;
    return;
  }

  const tableAreaHeight = tableAreaRef.value?.clientHeight ?? 0;
  fillHeight.value = tableAreaHeight > 0 ? tableAreaHeight : undefined;
}

function onClickRow(eventPayload: Event, rowPayload: unknown) {
  emit("click:row", eventPayload, rowPayload);
}
</script>

<style scoped>
.h-reasy-table-base__table {
  min-height: 0;
}

.h-reasy-table-base__table-area :deep(.v-table__wrapper) {
  min-height: 0;
  overflow: auto;
}
</style>
