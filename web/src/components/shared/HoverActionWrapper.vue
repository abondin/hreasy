<template>
  <span class="hover-action-wrapper">
    <span class="hover-action-wrapper__content">
      <slot />
    </span>
    <v-tooltip v-if="showAction" :location="tooltipLocation">
      <template #activator="{ props: tooltipProps }">
        <v-btn
          v-bind="tooltipProps"
          :icon="icon"
          :size="size"
          :variant="variant"
          :density="density"
          class="hover-action-wrapper__action"
          @click.stop="emit('action')"
        />
      </template>
      <span>{{ tooltip }}</span>
    </v-tooltip>
  </span>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    tooltip: string;
    showAction?: boolean;
    icon?: string;
    size?: "x-small" | "small" | "default" | "large" | "x-large";
    variant?: "text" | "flat" | "elevated" | "tonal" | "outlined" | "plain";
    density?: "default" | "comfortable" | "compact";
    tooltipLocation?: "top" | "bottom" | "start" | "end";
  }>(),
  {
    showAction: true,
    icon: "mdi-content-copy",
    size: "x-small",
    variant: "text",
    density: "compact",
    tooltipLocation: "bottom",
  },
);

const emit = defineEmits<{
  (event: "action"): void;
}>();
</script>

<style scoped>
.hover-action-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  max-width: 100%;
}

.hover-action-wrapper__content {
  min-width: 0;
  max-width: 100%;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.hover-action-wrapper__action {
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.15s ease;
}

@media (hover: hover) and (pointer: fine) {
  .hover-action-wrapper:hover .hover-action-wrapper__action,
  .hover-action-wrapper:focus-within .hover-action-wrapper__action {
    opacity: 1;
    visibility: visible;
  }
}

/* Show always only when there is no hover-capable pointer at all (touch-only devices). */
@media (any-hover: none) and (any-pointer: coarse) {
  .hover-action-wrapper__action {
    opacity: 1;
    visibility: visible;
  }
}
</style>
