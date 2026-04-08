<template>
  <v-card class="d-flex flex-column h-100" :data-testid="props.testId">
    <slot name="prepend" />

    <v-card-text :class="contentClassComputed">
      <slot />

      <div v-if="$slots.table" class="flex-grow-1 min-h-0 overflow-hidden">
        <slot name="table" />
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(defineProps<{
  testId?: string;
  contentClass?: string;
}>(), {
  contentClass: "pt-4 pb-2 d-flex flex-column flex-grow-1 min-h-0 overflow-hidden",
});

const contentClassComputed = computed(() => props.contentClass);
</script>
