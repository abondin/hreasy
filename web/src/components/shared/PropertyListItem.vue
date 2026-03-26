<!--
  Shared property row with line clamping and overflow tooltips for both label
  and value cells.
-->
<template>
  <div :class="['property-list-item', `property-list-item--${variant}`]">
    <div
      ref="labelRef"
      :class="[
        'property-list-item__label',
        { 'property-list-item__label--truncate': props.truncateLabel && !hasCustomLabelSlot },
      ]"
      :style="props.truncateLabel && !hasCustomLabelSlot ? { '-webkit-line-clamp': String(resolvedLabelLines) } : undefined"
    >
      <slot name="label">
        {{ label }}:
      </slot>
      <v-tooltip
        v-if="showLabelTooltip"
        activator="parent"
        location="top"
        max-width="420"
      >
        {{ label }}
      </v-tooltip>
    </div>
    <div
      ref="valueRef"
      :class="[
        'property-list-item__value',
        { 'property-list-item__value--truncate': props.truncateValue },
      ]"
      :style="props.truncateValue ? { '-webkit-line-clamp': String(resolvedValueLines) } : undefined"
    >
      <slot />
      <v-tooltip
        v-if="showValueTooltip && valueTooltipText"
        activator="parent"
        location="top"
        max-width="420"
      >
        {{ valueTooltipText }}
      </v-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, inject, nextTick, onBeforeUnmount, onMounted, onUpdated, ref, useSlots, type ComputedRef } from "vue";

const props = withDefaults(defineProps<{
  label: string;
  truncateLabel?: boolean;
  truncateValue?: boolean;
}>(), {
  truncateLabel: true,
  truncateValue: true,
});

const variant = inject<"inline" | "aligned">("property-list-variant", "inline");
const labelLines = inject<ComputedRef<number>>("property-list-label-lines", computed(() => 2));
const valueLines = inject<ComputedRef<number>>("property-list-value-lines", computed(() => 2));

const labelRef = ref<HTMLElement | null>(null);
const valueRef = ref<HTMLElement | null>(null);
const showLabelTooltip = ref(false);
const showValueTooltip = ref(false);
const valueTooltipText = ref("");

const resolvedLabelLines = computed(() => labelLines.value);
const resolvedValueLines = computed(() => valueLines.value);
const hasCustomLabelSlot = computed(() => Boolean(useSlots().label));

function hasOverflow(element: HTMLElement | null): boolean {
  if (!element) {
    return false;
  }

  return element.scrollHeight > element.clientHeight + 1 || element.scrollWidth > element.clientWidth + 1;
}

function updateOverflowState(): void {
  showLabelTooltip.value = props.truncateLabel && !hasCustomLabelSlot.value && hasOverflow(labelRef.value);
  valueTooltipText.value = valueRef.value?.textContent?.trim() ?? "";
  showValueTooltip.value = props.truncateValue && Boolean(valueTooltipText.value) && hasOverflow(valueRef.value);
}

function scheduleOverflowStateUpdate(): void {
  void nextTick(updateOverflowState);
}

onMounted(() => {
  scheduleOverflowStateUpdate();
  window.addEventListener("resize", scheduleOverflowStateUpdate);
});

onUpdated(() => {
  scheduleOverflowStateUpdate();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", scheduleOverflowStateUpdate);
});
</script>

<style scoped>
.property-list-item {
  font-size: 0.92rem;
  color: rgba(0, 0, 0, 0.72);
  margin-bottom: 4px;
}

.property-list-item--inline {
  display: grid;
  grid-template-columns: minmax(0, var(--property-list-label-width, 220px)) minmax(0, 1fr);
  column-gap: 6px;
  align-items: start;
}

.property-list-item--aligned {
  display: grid;
  grid-template-columns: minmax(0, var(--property-list-label-width, 220px)) minmax(0, 1fr);
  column-gap: 6px;
  align-items: start;
}

.property-list-item__label {
  font-weight: 600;
  line-height: 1.35;
  min-width: 0;
}

.property-list-item__label--truncate {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.property-list-item__value {
  color: rgba(0, 0, 0, 0.86);
  line-height: 1.35;
  min-width: 0;
}

.property-list-item__value--truncate {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  overflow-wrap: anywhere;
}

.property-list-item--aligned .property-list-item__label,
.property-list-item--aligned .property-list-item__value {
  min-width: 0;
}

@media (max-width: 599px) {
  .property-list-item--inline {
    grid-template-columns: 1fr;
    row-gap: 2px;
  }

  .property-list-item--aligned {
    grid-template-columns: 1fr;
    row-gap: 2px;
  }
}
</style>
