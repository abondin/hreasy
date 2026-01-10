<!--
  Form fields for requesting or updating planned vacations.
-->
<template>
  <div class="vacation-request-fields">
    <v-date-picker
      v-model="pickerRange"
      multiple="range"
      :first-day-of-week="1"
      hide-title
      hide-header
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

    <v-slider
      v-if="manualDays"
      v-model="data.formData.daysNumber"
      min="0"
      max="31"
      step="1"
      thumb-label="always"
      :label="t('Количество дней')"
    />

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
import {computed} from "vue";
import {useI18n} from "vue-i18n";
import type {RequestVacationAction} from "@/components/vacations/request-vacation.data.container";
import {formatDateOnly, parseDateOnly} from "@/lib/vacation-dates";

const props = defineProps<{
  data: RequestVacationAction;
}>();

const {t} = useI18n();

const formattedDates = computed(() => props.data.formattedDates());
const manualDays = computed({
  get: () => props.data.daysNumberSetManually.value,
  set: (value: boolean) => {
    props.data.daysNumberSetManually.value = value;
  },
});

const pickerRange = computed({
  get: () =>
    props.data.formData.dates
      .map((value) => (value ? parseDateOnly(value) : null))
      .filter((value): value is Date => Boolean(value)),
  set: (value: Date[] | null) => {
    const dates = (value ?? []).filter(Boolean);
    if (!dates.length) {
      props.data.formData.dates = [];
      props.data.datesUpdated();
      return;
    }
    const sorted = dates
      .map((item) => new Date(item))
      .sort((a, b) => a.getTime() - b.getTime());
    const start = sorted[0];
    const end = sorted[sorted.length - 1];
    props.data.formData.dates = [formatDateOnly(start), formatDateOnly(end)];
    props.data.datesUpdated();
  },
});


function handleManualToggle(value: boolean) {
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
