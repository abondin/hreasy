<template>
  <div class="junior-report-rating-field">
    <div class="junior-report-rating-field__label">
      <div class="d-flex align-center ga-1 min-width-0">
        <span class="text-body-1">{{ t(`JUNIOR_REPORT_RATING.${field}.title`) }}</span>
        <v-tooltip location="bottom" max-width="500">
          <template #activator="{ props }">
            <v-icon v-bind="props" size="default" icon="mdi-help-circle-outline" />
          </template>
          <div>
            <p>{{ t(`JUNIOR_REPORT_RATING.${field}.description`) }}</p>
            <p><strong>min</strong>: {{ t(`JUNIOR_REPORT_RATING.${field}.min`) }}</p>
            <p><strong>max</strong>: {{ t(`JUNIOR_REPORT_RATING.${field}.max`) }}</p>
          </div>
        </v-tooltip>
        <v-tooltip v-if="showDiff" location="bottom">
          <template #activator="{ props }">
            <v-chip
              v-bind="props"
              :color="diffColor"
              size="small"
              density="compact"
            >
              {{ diffLabel }}
            </v-chip>
          </template>
          <span>{{ t("По сравнению с последним результатом") }}</span>
        </v-tooltip>
      </div>
    </div>

    <div class="junior-report-rating-field__value">
      <v-rating
        :model-value="modelValue"
        :readonly="readonly"
        :length="5"
        :density="compact ? 'compact' : 'comfortable'"
        :size="compact ? 'default' : 'large'"
        color="warning"
        active-color="warning"
        @update:model-value="onUpdate"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { AddOrUpdateJuniorReportBody } from "@/services/junior-registry.service";

const props = withDefaults(
  defineProps<{
    field: keyof AddOrUpdateJuniorReportBody["ratings"];
    modelValue: number;
    prev?: number | null;
    readonly?: boolean;
    compact?: boolean;
  }>(),
  {
    prev: null,
    readonly: false,
    compact: false,
  },
);

const emit = defineEmits<{
  "update:modelValue": [value: number];
}>();

const { t } = useI18n();

const diff = computed(() => {
  if (props.prev == null || props.prev === props.modelValue) {
    return null;
  }
  return props.modelValue - props.prev;
});

const showDiff = computed(() => diff.value !== null);
const diffColor = computed(() => (diff.value ?? 0) < 0 ? "error" : "success");
const diffLabel = computed(() => {
  if (diff.value == null) {
    return "";
  }
  return diff.value > 0 ? `+${diff.value}` : `${diff.value}`;
});

function onUpdate(value: string | number) {
  emit("update:modelValue", Number(value));
}
</script>

<style scoped>
.junior-report-rating-field {
  display: grid;
  grid-template-columns: 380px auto;
  column-gap: 12px;
  align-items: center;
}

.junior-report-rating-field__label {
  min-width: 0;
}

.junior-report-rating-field__value {
  justify-self: start;
}

@media (max-width: 599px) {
  .junior-report-rating-field {
    grid-template-columns: 1fr;
    row-gap: 4px;
  }
}
</style>
