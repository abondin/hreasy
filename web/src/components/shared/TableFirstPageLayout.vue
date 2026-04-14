<template>
  <v-container fluid class="py-6" :data-testid="props.testId">
    <div
      ref="contentRef"
      class="d-flex flex-column h-100 min-h-0"
      :style="contentStyle"
    >
      <slot :content-height="contentHeight" />
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { computed, nextTick, onActivated, onBeforeUnmount, onMounted, ref } from "vue";

const props = withDefaults(defineProps<{
  testId?: string;
  minContentHeight?: number;
}>(), {
  testId: undefined,
  minContentHeight: 320,
});

const contentRef = ref<HTMLElement | null>(null);
const contentHeight = ref<number | null>(null);
let pendingRafId: number | null = null;
let hasPendingRecalculation = false;

const contentStyle = computed(() => {
  if (contentHeight.value == null) {
    return undefined;
  }
  return { height: `${contentHeight.value}px` };
});

onMounted(async () => {
  await nextTick();
  recalculateContentHeight();
  window.addEventListener("resize", handleWindowResize);
});

onActivated(async () => {
  await nextTick();
  recalculateContentHeight();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleWindowResize);
  if (pendingRafId != null) {
    window.cancelAnimationFrame(pendingRafId);
    pendingRafId = null;
  }
  hasPendingRecalculation = false;
});

function handleWindowResize(): void {
  scheduleRecalculateContentHeight();
}

function scheduleRecalculateContentHeight(): void {
  hasPendingRecalculation = true;

  if (pendingRafId !== null) {
    return;
  }

  pendingRafId = window.requestAnimationFrame(() => {
    pendingRafId = null;
    if (!hasPendingRecalculation) {
      return;
    }
    hasPendingRecalculation = false;
    recalculateContentHeight();
  });
}

function recalculateContentHeight(): void {
  const contentElement = contentRef.value;
  if (!contentElement) {
    return;
  }

  const containerElement = contentElement.parentElement;
  const containerStyles = containerElement ? window.getComputedStyle(containerElement) : null;
  const containerPaddingBottom = containerStyles ? Number.parseFloat(containerStyles.paddingBottom) || 0 : 0;
  const contentTop = contentElement.getBoundingClientRect().top;
  const nextHeight = Math.max(
    props.minContentHeight,
    Math.floor(window.innerHeight - contentTop - containerPaddingBottom),
  );

  if (contentHeight.value === nextHeight) {
    return;
  }

  contentHeight.value = nextHeight;
}
</script>
