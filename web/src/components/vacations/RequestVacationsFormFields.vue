<!-- eslint-disable vue/no-mutating-props -->
<!--
  Form fields for requesting or updating planned vacations.
-->
<template>
  <div class="vacation-request-fields">
    <v-date-picker
      v-model="pickerRange"
      multiple="range"
      :first-day-of-week="1"
      show-adjacent-months
      width="100%"
      max-width="100%"
    />
    <div class="text-body-2">
      <span v-if="formattedDates" class="text-medium-emphasis">
        {{ formattedDates }}
      </span>
      <span v-else class="text-error">
        {{ t("Выберите даты") }}
      </span>
    </div>

    <div
        class="text-body-2 mt-2"
        :class="{ 'text-warning': data.formData.daysNumber > 28 }"
    >
      {{ t("Количество дней отпуска") }}: {{ data.formData.daysNumber }}
    </div>

    <v-checkbox
      v-model="manualDays"
      density="compact"
      class="mt-2"
      :label="t('Скорректировать количество дней вручную')"
      @update:model-value="handleManualToggle"
    />

    <!-- eslint-disable-next-line vue/no-mutating-props -->
    <v-slider
      v-if="manualDays"
      v-model="data.formData.daysNumber"
      min="0"
      max="31"
      step="1"
      thumb-label="always"
      :label="t('Количество дней')"
    />

    <!-- eslint-disable-next-line vue/no-mutating-props -->
    <v-textarea
        v-model="data.formData.notes"
        rows="3"
        :label="t('Примечание')"
        :counter="255"
        :rules="[validateLength]"
    />
  </div>
</template>

<script setup lang="ts">
/* eslint-disable vue/no-mutating-props */
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type {RequestVacationAction} from "@/components/vacations/request-vacation.data.container";
import { addDays, formatDateOnly, parseDateOnly } from "@/lib/vacation-dates";

const props = defineProps<{
  data: RequestVacationAction;
}>();

const {t} = useI18n();
const draftDates = ref<string[]>([]);

watch(
  () => props.data.formData.dates,
  (value) => {
    draftDates.value = expandBoundaryRange(value);
  },
  { immediate: true },
);

const formattedDates = computed(() => props.data.formattedDates());
const manualDays = computed({
  get: () => props.data.daysNumberSetManually.value,
  set: (value: boolean | null) => {
    props.data.daysNumberSetManually.value = Boolean(value);
  },
});

const pickerRange = computed({
  get: () =>
    draftDates.value
      .map((value) => (value ? parseDateOnly(value) : null))
      .filter((value): value is Date => Boolean(value)),
  set: (value: Date[] | null) => {
    draftDates.value = normalizeSelection(value);
    props.data.formData.dates = normalizeBoundaryRange(draftDates.value);
    props.data.datesUpdated();
  },
});

function normalizeSelection(value: unknown): string[] {
  const asArray = Array.isArray(value) ? value : [];
  return asArray
    .map((item) => normalizeToIsoDate(item))
    .filter((item): item is string => Boolean(item))
    .sort((left, right) => left.localeCompare(right))
    .filter((item, index, arr) => index === 0 || item !== arr[index - 1]);
}

function normalizeBoundaryRange(value: unknown): string[] {
  const normalized = normalizeSelection(value);
  if (normalized.length === 0) {
    return [];
  }
  if (normalized.length === 1) {
    const single = normalized[0];
    return single ? [single] : [];
  }
  const start = normalized[0];
  const end = normalized[normalized.length - 1];
  if (!start || !end) {
    return [];
  }
  return [start, end];
}

function normalizeToIsoDate(value: unknown): string | null {
  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return formatDateOnly(value);
  }
  if (typeof value === "string") {
    const parsed = parseDateOnly(value);
    return parsed ? formatDateOnly(parsed) : null;
  }
  return null;
}

function buildRangeSelection(start: Date, end: Date): string[] {
  const from = start <= end ? start : end;
  const to = start <= end ? end : start;
  const dates: string[] = [];
  for (let cursor = new Date(from); cursor <= to; cursor = addDays(cursor, 1)) {
    dates.push(formatDateOnly(cursor));
  }
  return dates;
}

function expandBoundaryRange(value: unknown): string[] {
  const boundary = normalizeBoundaryRange(value);
  if (boundary.length !== 2) {
    return boundary;
  }
  const start = parseDateOnly(boundary[0]);
  const end = parseDateOnly(boundary[1]);
  if (!start || !end) {
    return boundary;
  }
  return buildRangeSelection(start, end);
}


function handleManualToggle(value: boolean | null) {
  if (!value) {
    props.data.updateDaysNumber();
  }
}

function validateLength(value: string | null): true | string {
  if (!value || value.length <= 255) {
    return true;
  }
  return t("Не более N символов", {n: 255});
}
</script>

<style scoped>
.vacation-request-fields {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
