<template>
  <span v-if="value">
    <span v-if="isOk"><slot>{{ value.value }}</slot></span>
    <v-chip
      v-else-if="isWarning"
      :size="dense ? 'small' : 'default'"
      :density="dense ? 'compact' : 'default'"
      color="warning"
      variant="tonal"
    >
      <slot>{{ value.value }}</slot>
    </v-chip>
    <v-chip
      v-else-if="isError"
      :size="dense ? 'small' : 'default'"
      :density="dense ? 'compact' : 'default'"
      color="error"
      variant="tonal"
    >
      <slot>{{ value.value }}</slot>
    </v-chip>
    <span v-else><slot>{{ value.value }}</slot></span>
  </span>
  <span v-else>-</span>
</template>

<script setup lang="ts" generic="T">
import { computed } from "vue";
import type { ValueWithStatus } from "@/services/junior-registry.service";

const props = withDefaults(
  defineProps<{
    value: ValueWithStatus<T> | null | undefined;
    dense?: boolean;
  }>(),
  {
    dense: false,
  },
);

const normalizedStatus = computed(() => Number(props.value?.status ?? 0));
const isOk = computed(() => normalizedStatus.value === 1);
const isWarning = computed(() => normalizedStatus.value === 2);
const isError = computed(() => normalizedStatus.value === 3);
</script>
