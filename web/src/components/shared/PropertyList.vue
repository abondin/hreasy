<!--
  Shared wrapper for property rows with consistent label area sizing across a
  section. Supports density presets and mobile stacking.
-->
<template>
  <div
    :class="['property-list', `property-list--${variant}`]"
    :style="{ '--property-list-label-width': resolvedLabelWidth }"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed, provide } from "vue";

const props = withDefaults(defineProps<{
  variant?: "inline" | "aligned";
  density?: "compact" | "default" | "wide";
  labelWidth?: string;
  labelLines?: number;
  valueLines?: number;
}>(), {
  variant: "inline",
  density: "default",
  labelWidth: undefined,
  labelLines: 2,
  valueLines: 2,
});

const labelWidthByDensity: Record<NonNullable<typeof props.density>, string> = {
  compact: "180px",
  default: "220px",
  wide: "260px",
};

const resolvedLabelWidth = computed(() => props.labelWidth ?? labelWidthByDensity[props.density]);

provide("property-list-variant", props.variant);
provide("property-list-label-width", resolvedLabelWidth);
provide("property-list-label-lines", computed(() => props.labelLines));
provide("property-list-value-lines", computed(() => props.valueLines));
</script>

<style scoped>
.property-list {
  display: flex;
  flex-direction: column;
}
</style>
