<template>
  <div class="adaptive-filter-bar d-flex flex-nowrap align-center ga-4">
    <div
      v-if="props.hasLeftActions"
      class="adaptive-filter-bar__actions adaptive-filter-bar__actions--left d-flex align-center"
    >
      <slot name="left-actions" />
    </div>

    <v-divider
      v-if="props.hasLeftActions && items.length > 0"
      vertical
      class="adaptive-filter-bar__divider"
    />

    <div ref="visibleFiltersRef" class="adaptive-filter-bar__filters flex-grow-1">
      <div class="adaptive-filter-bar__filters-grid d-flex flex-nowrap ga-4">
        <div
          v-for="item in visibleItems"
          :key="item.id"
          class="adaptive-filter-bar__field"
          :style="getFieldStyle(item)"
        >
          <slot :name="`filter-${item.id}`" :item="item" :hidden="false" />
        </div>
      </div>
    </div>

    <div
      v-if="showRightCluster"
      class="adaptive-filter-bar__right d-flex align-center ga-4 ms-auto"
    >
      <v-divider
        vertical
        class="adaptive-filter-bar__divider"
      />

      <div class="adaptive-filter-bar__actions adaptive-filter-bar__actions--right d-flex align-center justify-end ga-2">
        <v-menu
          v-if="hiddenItems.length > 0"
          v-model="overflowMenu"
          location="bottom end"
          :close-on-content-click="false"
        >
          <template #activator="{ props: activatorProps }">
            <v-badge
              :model-value="hiddenActiveCount > 0"
              :content="hiddenActiveCount"
              color="info"
              floating
            >
              <v-btn
                v-bind="activatorProps"
                data-testid="adaptive-filter-overflow"
                :color="hiddenActiveCount > 0 ? 'info' : undefined"
                :variant="hiddenActiveCount > 0 ? 'tonal' : 'text'"
                icon="mdi-filter-variant"
                size="small"
                density="comfortable"
              />
            </v-badge>
          </template>

          <v-card :min-width="props.overflowMenuMinWidth" class="pa-4">
            <div class="d-flex flex-column ga-4">
              <div
                v-for="item in hiddenItems"
                :key="item.id"
                class="adaptive-filter-bar__overflow-field"
              >
                <slot :name="`filter-${item.id}`" :item="item" :hidden="true" />
              </div>
            </div>
          </v-card>
        </v-menu>

        <slot name="right-actions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";

export type AdaptiveFilterBarItem = {
  id: string;
  minWidth: number;
  active?: boolean;
  grow?: boolean;
};

const props = withDefaults(defineProps<{
  items: AdaptiveFilterBarItem[];
  overflowMenuMinWidth?: number | string;
  hasLeftActions?: boolean;
  hasRightActions?: boolean;
}>(), {
  overflowMenuMinWidth: 360,
  hasLeftActions: true,
  hasRightActions: false,
});

const overflowMenu = ref(false);
const visibleFiltersRef = ref<HTMLElement | null>(null);
const visibleItemIds = ref<string[]>(props.items.map((item) => item.id));
const hiddenItemIds = ref<string[]>([]);

const FILTER_GAP_PX = 16;

let visibleFiltersResizeObserver: ResizeObserver | null = null;

const visibleItems = computed(() => props.items.filter((item) => visibleItemIds.value.includes(item.id)));
const hiddenItems = computed(() => props.items.filter((item) => hiddenItemIds.value.includes(item.id)));
const hiddenActiveCount = computed(() => hiddenItems.value.reduce((count, item) => count + (item.active ? 1 : 0), 0));
const showRightCluster = computed(() => props.hasRightActions || hiddenItems.value.length > 0);

onMounted(() => {
  nextTick(() => {
    recalculateVisibleFilters();

    if (typeof ResizeObserver !== "undefined" && visibleFiltersRef.value) {
      visibleFiltersResizeObserver = new ResizeObserver(() => {
        recalculateVisibleFilters();
      });
      visibleFiltersResizeObserver.observe(visibleFiltersRef.value);
    }
  });
});

watch(
  () => props.items.map((item) => `${item.id}:${item.minWidth}:${item.active ? 1 : 0}`).join("|"),
  async () => {
    await nextTick();
    recalculateVisibleFilters();
  },
);

onBeforeUnmount(() => {
  visibleFiltersResizeObserver?.disconnect();
});

function recalculateVisibleFilters(): void {
  const availableWidth = visibleFiltersRef.value?.clientWidth ?? 0;
  if (availableWidth <= 0) {
    return;
  }

  const nextVisible: string[] = [];
  const nextHidden: string[] = [];
  let usedWidth = 0;

  props.items.forEach((item) => {
    const nextWidth = item.minWidth + (nextVisible.length > 0 ? FILTER_GAP_PX : 0);
    if (usedWidth + nextWidth <= availableWidth) {
      nextVisible.push(item.id);
      usedWidth += nextWidth;
      return;
    }
    nextHidden.push(item.id);
  });

  visibleItemIds.value = nextVisible;
  hiddenItemIds.value = nextHidden;

  if (nextHidden.length === 0) {
    overflowMenu.value = false;
  }
}

function getFieldStyle(item: AdaptiveFilterBarItem): { flex: string; minWidth: string } {
  const width = `${item.minWidth}px`;
  return {
    flex: item.grow ? `1 1 ${width}` : `0 0 ${width}`,
    minWidth: width,
  };
}
</script>

<style scoped>
.adaptive-filter-bar {
  align-items: center;
}

.adaptive-filter-bar__actions,
.adaptive-filter-bar__right {
  flex: 0 0 auto;
}

.adaptive-filter-bar__actions--left,
.adaptive-filter-bar__actions--right,
.adaptive-filter-bar__right {
  align-self: center;
}

.adaptive-filter-bar__divider {
  align-self: center;
  flex: 0 0 auto;
  height: 32px;
}

.adaptive-filter-bar__filters {
  min-width: 0;
}

.adaptive-filter-bar__filters-grid {
  width: 100%;
}

.adaptive-filter-bar__field {
  flex: 0 0 auto;
}

@media (max-width: 959px) {
  .adaptive-filter-bar__divider {
    display: none;
  }
}
</style>
