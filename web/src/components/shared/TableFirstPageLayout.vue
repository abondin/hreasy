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
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";

const props = withDefaults(defineProps<{
  testId?: string;
  minContentHeight?: number;
  footerSelector?: string;
}>(), {
  testId: undefined,
  minContentHeight: 320,
  footerSelector: ".v-footer",
});

const contentRef = ref<HTMLElement | null>(null);
const contentHeight = ref<number | null>(null);

let footerResizeObserver: ResizeObserver | null = null;

const contentStyle = computed(() => {
  if (contentHeight.value == null) {
    return undefined;
  }
  return { height: `${contentHeight.value}px` };
});

onMounted(async () => {
  await nextTick();
  recalculateContentHeight();
  window.addEventListener("resize", recalculateContentHeight);

  const footer = getFooterElement();
  if (typeof ResizeObserver !== "undefined" && footer) {
    footerResizeObserver = new ResizeObserver(() => {
      recalculateContentHeight();
    });
    footerResizeObserver.observe(footer);
  }
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", recalculateContentHeight);
  footerResizeObserver?.disconnect();
});

function getFooterElement(): HTMLElement | null {
  const footer = document.querySelector(props.footerSelector);
  return footer instanceof HTMLElement ? footer : null;
}

function recalculateContentHeight(): void {
  const contentElement = contentRef.value;
  if (!contentElement) {
    return;
  }

  const containerElement = contentElement.parentElement;
  const containerStyles = containerElement ? window.getComputedStyle(containerElement) : null;
  const containerPaddingBottom = containerStyles ? Number.parseFloat(containerStyles.paddingBottom) || 0 : 0;
  const footerHeight = getFooterElement()?.getBoundingClientRect().height ?? 0;
  const contentTop = contentElement.getBoundingClientRect().top;
  const nextHeight = Math.max(
    props.minContentHeight,
    Math.floor(window.innerHeight - contentTop - footerHeight - containerPaddingBottom),
  );

  contentHeight.value = nextHeight;
}
</script>
