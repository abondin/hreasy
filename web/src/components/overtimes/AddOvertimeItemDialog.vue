<template>
  <v-dialog v-model="dialog" max-width="760">
    <template #activator="{ props }">
      <v-btn v-bind="props" color="primary" :disabled="periodClosed">
        {{ t("Добавить") }}
      </v-btn>
    </template>

    <v-card>
      <v-card-title>{{ t("Учёт овертаймов за день") }}</v-card-title>
      <v-card-text>
        <v-autocomplete
          v-model="item.projectId"
          :items="activeProjects"
          item-title="name"
          item-value="id"
          :label="t('Проект')"
          required
        />

        <v-text-field
          v-model="item.date"
          type="date"
          :label="t('Дата')"
        >
          <template #prepend>
            <v-btn size="small" icon="mdi-chevron-left" @click="setPrevDay" />
            <v-btn size="small" icon="mdi-chevron-right" @click="setNextDay" />
          </template>
        </v-text-field>

        <v-slider
          v-model="item.hours"
          :label="t('Часы')"
          :min="0.5"
          :max="24"
          :step="0.5"
          thumb-label="always"
          class="px-3"
        />

        <v-textarea
          v-model="item.notes"
          :label="t('Комментарий')"
          rows="3"
        />

        <v-alert v-if="errorMessage" type="error" variant="tonal" class="mt-2">
          {{ errorMessage }}
        </v-alert>
      </v-card-text>

      <v-card-actions>
        <v-checkbox v-model="addMore" :label="t('Добавить ещё')" hide-details />
        <v-spacer />
        <v-btn @click="closeDialog">{{ t("Закрыть") }}</v-btn>
        <v-btn color="primary" :loading="submitting" @click="submit">
          {{ t("Добавить") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { withArchivedOptionById, withCurrentOptionById } from "@/lib/dict-options";
import { errorUtils } from "@/lib/errors";
import { formatIsoDate } from "@/lib/datetime";
import {
  addOvertimeItem,
  type OvertimeItem,
  type OvertimeReport,
} from "@/services/overtime.service";
import type { SimpleDict } from "@/services/projects.service";

const props = defineProps<{
  employeeId: number;
  periodId: number;
  periodClosed: boolean;
  allProjects: SimpleDict[];
  defaultProject?: number | null;
}>();

const emit = defineEmits<{
  submitted: [report: OvertimeReport];
  close: [];
}>();

const { t } = useI18n();
const dialog = ref(false);
const submitting = ref(false);
const addMore = ref(true);
const errorMessage = ref<string | null>(null);

const item = reactive<OvertimeItem>(createDefaultItem());

const activeProjects = computed(() => {
  const selectedProjectId = item.projectId ?? props.defaultProject ?? null;
  const activeOnly = props.allProjects.filter((project) => project.active !== false);
  const selectedProject = selectedProjectId == null
    ? null
    : props.allProjects.find((project) => project.id === selectedProjectId) ?? null;

  const withCurrent = withCurrentOptionById(activeOnly, selectedProject);
  return withArchivedOptionById(withCurrent, selectedProjectId, (id) => ({
    id,
    name: `${t("Архив")} #${id}`,
    active: false,
  }));
});

watch(dialog, (value) => {
  if (value) {
    resetItem();
    errorMessage.value = null;
  } else {
    emit("close");
  }
});

function createDefaultItem(
  projectId?: number | null,
  date?: string,
  hours = 4,
): OvertimeItem {
  return {
    projectId: projectId ?? props.defaultProject ?? undefined,
    date: date ?? formatIsoDate(new Date()),
    hours,
    notes: undefined,
  };
}

function resetItem(projectId?: number | null, date?: string, hours = 4): void {
  Object.assign(item, createDefaultItem(projectId, date, hours));
}

function shiftDate(days: number): void {
  const date = new Date(item.date);
  if (Number.isNaN(date.getTime())) {
    item.date = formatIsoDate(new Date());
    return;
  }
  date.setDate(date.getDate() + days);
  item.date = formatIsoDate(date);
}

function setPrevDay(): void {
  shiftDate(-1);
}

function setNextDay(): void {
  shiftDate(1);
}

function validate(): string | null {
  if (!item.projectId) {
    return t("Проект обязателен");
  }
  if (!item.date) {
    return t("Дата обязательна");
  }
  if (Number.isNaN(new Date(item.date).getTime())) {
    return t("Дата в формате ГГГГ.ММ.ДД");
  }
  if (!item.hours || item.hours <= 0) {
    return t("Часы обязательны");
  }
  return null;
}

async function submit(): Promise<void> {
  const validationError = validate();
  errorMessage.value = validationError;
  if (validationError) {
    return;
  }

  submitting.value = true;
  try {
    const report = await addOvertimeItem(props.employeeId, props.periodId, {
      ...item,
      createdAt: undefined,
    });
    emit("submitted", report);
    if (addMore.value) {
      const currentProject = item.projectId;
      const nextDate = new Date(item.date);
      nextDate.setDate(nextDate.getDate() + 1);
      resetItem(currentProject ?? null, formatIsoDate(nextDate), item.hours);
    } else {
      closeDialog();
    }
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
  } finally {
    submitting.value = false;
  }
}

function closeDialog(): void {
  dialog.value = false;
}
</script>
