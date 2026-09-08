<template>
  <v-text-field
    :model-value="modelValue"
    clearable
    hide-details
    density="compact"
    variant="outlined"
    prepend-inner-icon="mdi-magnify"
    :label="label"
    :data-testid="testId"
    @update:model-value="modelValue = normalizeSearchInput($event)"
  />
</template>

<script setup lang="ts">
import { normalizeSearchInput } from "@/lib/search";

defineProps<{
  label: string;
  testId?: string;
}>();
const modelValue = defineModel<string>({ required: true });
</script>
