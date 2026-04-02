<!--
  Dialog form for creating or editing vacation entries.
-->
<template>
  <v-form ref="formRef">
    <v-card>
      <v-card-item>
        <template #title>
          <span v-if="form.isNew">{{ t("Добавление отпуска") }}</span>
          <span v-else>{{ t("Изменение отпуска") }}</span>
        </template>
        <template #append>
          <v-btn icon="mdi-close" variant="text" @click="closeDialog" />
        </template>
      </v-card-item>
      <v-card-text>
        <v-autocomplete
          v-model="form.employeeId"
          :items="employeeOptions"
          item-title="name"
          item-value="id"
          :disabled="!form.isNew"
          :label="`${t('Сотрудник')}*`"
          :rules="[requiredRule]"
        />

        <v-select
          v-model="form.year"
          :items="allYears"
          :label="t('Год')"
        />

        <my-date-form-component
          v-model="form.startDate"
          :label="`${t('Начало')}*`"
          :rules="[requiredRule]"
        />

        <my-date-form-component
          v-model="form.endDate"
          :label="`${t('Окончание')}*`"
          :rules="[requiredRule]"
        />

        <v-select
          v-model="form.status"
          :items="allStatuses"
          item-title="title"
          item-value="value"
          :label="t('Статус')"
        />

        <v-slider
          v-model="form.daysNumber"
          min="0"
          max="31"
          step="1"
          thumb-label="always"
          :label="t('Количество дней')"
        />

        <v-text-field
          v-model="form.documents"
          :label="t('Документ')"
          :counter="255"
          :rules="[maxLengthRule]"
        />

        <v-textarea
          v-model="form.notes"
          rows="3"
          :label="t('Примечание')"
          :counter="255"
          :rules="[maxLengthRule]"
        />

        <v-alert v-if="error" type="error" variant="tonal" border="start">
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" @click="closeDialog">
          {{ t("Закрыть") }}
        </v-btn>
        <v-btn color="primary" @click="submit">
          {{ form.isNew ? t("Создать") : t("Изменить") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { errorUtils } from "@/lib/errors";
import { withArchivedOptionById, withCurrentOptionById } from "@/lib/dict-options";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import {
  createVacation,
  updateVacation,
  type CreateOrUpdateVacation,
  type Vacation,
  type VacationStatus,
} from "@/services/vacation.service";
import {
  addDays,
  calculateVacationDays,
  formatDateOnly,
  parseDateOnly,
} from "@/lib/vacation-dates";
import type { Dict } from "@/services/employee.service";

const props = defineProps<{
  input?: Vacation | null;
  allStatuses: Array<{ value: VacationStatus; title: string }>;
  allYears: number[];
  allEmployees: Dict[];
  daysNotIncludedInVacations: string[];
  defaultYear?: number;
}>();

const emit = defineEmits<{ (event: "close"): void }>();

const { t } = useI18n();
const formRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(
  null,
);
const error = ref<string | null>(null);

const defaultNumberOrDays = 14;

const form = reactive({
  isNew: true,
  id: undefined as number | undefined,
  year: new Date().getFullYear(),
  employeeId: undefined as number | undefined,
  startDate: "",
  endDate: "",
  plannedStartDate: "",
  plannedEndDate: "",
  status: "PLANNED" as VacationStatus,
  notes: "",
  documents: "",
  daysNumber: 0,
});

const employeeOptions = computed(() => {
  const currentEmployee = props.input
    ? {
      id: props.input.employee,
      name: props.input.employeeDisplayName,
    }
    : null;
  const withCurrent = withCurrentOptionById(props.allEmployees, currentEmployee);
  return withArchivedOptionById(withCurrent, form.employeeId ?? null, (id) => ({
    id,
    name: `${t("Архив")} #${id}`,
  }));
});

function resetForm() {
  const baseYear = props.defaultYear ?? new Date().getFullYear();
  const defaultStart =
    baseYear !== new Date().getFullYear() ? new Date(baseYear, 0, 1) : null;

  form.isNew = true;
  form.id = undefined;
  form.year = baseYear;
  form.employeeId = undefined;
  form.startDate = defaultStart ? formatDateOnly(defaultStart) : "";
  form.endDate = "";
  form.plannedStartDate = "";
  form.plannedEndDate = "";
  form.status = "PLANNED";
  form.notes = "";
  form.documents = "";
  form.daysNumber = 0;

  if (props.input) {
    form.isNew = false;
    form.id = props.input.id;
    form.year = props.input.year;
    form.employeeId = props.input.employee;
    form.startDate = props.input.startDate;
    form.endDate = props.input.endDate;
    form.plannedStartDate = props.input.plannedStartDate ?? "";
    form.plannedEndDate = props.input.plannedEndDate ?? "";
    form.status = props.input.status;
    form.notes = props.input.notes;
    form.documents = props.input.documents;
    form.daysNumber = props.input.daysNumber;
  }
  error.value = null;
}

watch(
  () => props.input,
  () => {
    resetForm();
  },
  { immediate: true },
);

watch(
  () => form.startDate,
  (value) => {
    if (!value) {
      return;
    }
    if (!form.endDate) {
      const start = parseDateOnly(value);
      if (!start) {
        return;
      }
      const end = addDays(start, defaultNumberOrDays - 1);
      form.endDate = formatDateOnly(end);
    }
    updateDaysNumber();
  },
);

watch(
  () => form.endDate,
  () => {
    updateDaysNumber();
  },
);

function updateDaysNumber() {
  if (!form.startDate || !form.endDate) {
    return;
  }
  form.daysNumber = calculateVacationDays(
    form.startDate,
    form.endDate,
    props.daysNotIncludedInVacations,
  );
}

const requiredRule = (value: unknown) =>
  Boolean(value) || t("Обязательное поле");

const maxLengthRule = (value: string | null) => {
  if (!value || value.length <= 255) {
    return true;
  }
  return t("Не более N символов", { n: 255 });
};

async function submit() {
  if (!formRef.value) {
    return;
  }
  const { valid } = await formRef.value.validate();
  if (!valid) {
    return;
  }

  const payload: CreateOrUpdateVacation = {
    year: form.year,
    startDate: form.startDate,
    endDate: form.endDate,
    plannedStartDate: form.plannedStartDate,
    plannedEndDate: form.plannedEndDate,
    status: form.status,
    notes: form.notes,
    documents: form.documents,
    daysNumber: form.daysNumber,
  };

  try {
    if (form.isNew) {
      await createVacation(form.employeeId as number, payload);
    } else {
      await updateVacation(form.employeeId as number, form.id as number, payload);
    }
    emit("close");
  } catch (err) {
    error.value = errorUtils.shortMessage(err);
  }
}

function closeDialog() {
  resetForm();
  emit("close");
}

defineExpose({ resetForm });
</script>
