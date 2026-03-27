<template>
  <v-chip v-if="index < visibleCount" size="small" label>
    {{ label }}
  </v-chip>
  <span v-else-if="index === visibleCount" class="text-caption text-medium-emphasis ml-2">
    (+{{ hiddenCount }})
  </span>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(defineProps<{
  index: number;
  total: number;
  label: string;
  visibleCount?: number;
}>(), {
  visibleCount: 1,
});

const hiddenCount = computed(() => Math.max(props.total - props.visibleCount, 0));
</script>
