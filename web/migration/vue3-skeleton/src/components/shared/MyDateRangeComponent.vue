<!--
  Compact date range selector matching legacy API: v-model is [start, end].
-->
<template>
  <v-row dense>
    <v-col cols="12" sm="6">
      <v-text-field
        v-model="startDate"
        type="date"
        :label="label"
        density="compact"
      />
    </v-col>
    <v-col cols="12" sm="6">
      <v-text-field
        v-model="endDate"
        type="date"
        :label="t('Окончание')"
        density="compact"
      />
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    modelValue: string[];
    label?: string;
  }>(),
  {
    modelValue: () => [],
    label: "",
  },
);

const emit = defineEmits<{ (event: "update:modelValue", value: string[]): void }>();
const { t } = useI18n();

const startDate = computed({
  get: () => props.modelValue[0] ?? "",
  set: (value: string) => {
    const next = [...props.modelValue];
    next[0] = value;
    emit("update:modelValue", next);
  },
});

const endDate = computed({
  get: () => props.modelValue[1] ?? "",
  set: (value: string) => {
    const next = [...props.modelValue];
    next[1] = value;
    emit("update:modelValue", next);
  },
});
</script>
