<!--
  Planned vacations for the current employee.
-->
<template>
  <v-card>
    <v-card-title class="d-flex align-center">
      <span>{{ t("Планируемые отпуска") }}</span>
      <v-spacer />

      <v-menu v-if="openedPeriods.length" location="bottom">
        <template #activator="{ props: menuProps }">
          <v-tooltip location="top">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                color="primary"
                v-bind="{ ...menuProps, ...tooltipProps }"
              >
                {{ t("Запланировать") }}
              </v-btn>
            </template>
            <span>{{ t("Запланировать отпуск на будущий год") }}</span>
          </v-tooltip>
        </template>
        <v-list density="compact">
          <v-list-item
            v-for="period in openedPeriods"
            :key="period.year"
            @click="requestAction.openRequestVacationDialog(period.year)"
          >
            <v-list-item-title>{{ period.year }}</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </v-card-title>

    <v-card-text>
      <v-alert
        v-if="loadError"
        type="error"
        variant="tonal"
        border="start"
        class="mb-4"
      >
        {{ loadError }}
      </v-alert>
      <v-data-table
        class="vacations-table"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :headers="headers"
        :items="filteredVacations"
        :sort-by="[
          { key: 'startDate', order: 'asc' },
          { key: 'endDate', order: 'asc' },
        ]"
        density="compact"
        hide-default-footer
        :items-per-page="-1"
      >
        <template v-slot:[`item.startDate`]="{ item }">
          {{ formatDate(item.startDate) }}
        </template>
        <template v-slot:[`item.endDate`]="{ item }">
          {{ formatDate(item.endDate) }}
        </template>
        <template v-slot:[`item.status`]="{ item }">
          {{ t(`VACATION_STATUS_ENUM.${item.status}`) }}
        </template>
        <template v-slot:[`item.notes`]="{ item }">
          <div class="d-flex justify-space-between align-center">
            <span>{{ item.notes || "" }}</span>
            <v-tooltip location="bottom">
              <template #activator="{ props }">
                <v-btn
                  v-if="vacationCanBeRejected(item)"
                  v-bind="props"
                  icon="mdi-delete"
                  variant="text"
                  class="vacations-table__delete"
                  @click="openRejectDialog(item.id)"
                />
              </template>
              <span>{{ t("Отозвать") }}</span>
            </v-tooltip>
          </div>
        </template>
      </v-data-table>
    </v-card-text>

    <v-dialog v-model="requestDialogOpen" max-width="640">
      <v-card>
        <v-card-title>
          {{ t("Запланировать отпуск на X год", { year: requestAction.formData.year }) }}
        </v-card-title>
        <v-card-text>
          <request-vacations-form-fields :data="requestAction" />
          <v-alert
            v-if="requestError"
            type="error"
            variant="tonal"
            border="start"
            class="mt-4"
          >
            {{ requestError }}
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="requestAction.closeDialog">
            {{ t("Закрыть") }}
          </v-btn>
          <v-btn
            color="primary"
            :loading="requestLoading"
            :disabled="requestLoading"
            @click="submitRequest"
          >
            {{ t("Создать") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <confirm-delete-dialog
      :open="rejectDialogOpen"
      :title="t('Отозвать отпуск')"
      :message="t('Вы уверены, что хотите отозвать отпуск?')"
      :confirm-label="t('Да')"
      :cancel-label="t('Нет')"
      :loading="rejectLoading"
      :error-message="rejectError"
      @close="closeRejectDialog"
      @confirm="confirmReject"
    />
  </v-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import RequestVacationsFormFields from "@/components/vacations/RequestVacationsFormFields.vue";
import ConfirmDeleteDialog from "@/components/shared/ConfirmDeleteDialog.vue";
import {
  fetchMyVacations,
  openPlanningPeriods,
  rejectVacationRequest,
  type MyVacation,
  type VacPlanningPeriod,
} from "@/services/vacation.service";
import { formatDate } from "@/lib/datetime";
import { getDefaultYears } from "@/lib/vacation-dates";
import { useRequestVacationAction } from "@/components/vacations/request-vacation.data.container";
import { errorUtils } from "@/lib/errors";
import { useVacationsDictionaries } from "@/components/vacations/useVacationsDictionaries";

const { t } = useI18n();

const headers = computed(() => [
  { title: t("Год"), key: "year" },
  { title: t("Начало"), key: "startDate" },
  { title: t("Окончание"), key: "endDate" },
  { title: t("Статус"), key: "status" },
  { title: t("Примечание"), key: "notes" },
]);

const loading = ref(false);
const vacations = ref<MyVacation[]>([]);
const openedPeriods = ref<VacPlanningPeriod[]>([]);
const requestAction = useRequestVacationAction();
const { daysNotIncludedInVacations, loadDaysNotIncluded } = useVacationsDictionaries();
const loadError = ref("");
const requestDialogOpen = computed({
  get: () => requestAction.open.value,
  set: (value) => {
    requestAction.open.value = value;
  },
});
const requestLoading = computed(() => requestAction.loading.value);
const requestError = computed(() => requestAction.error.value);

const rejectDialogOpen = ref(false);
const rejectLoading = ref(false);
const rejectError = ref("");
const rejectId = ref<number | null>(null);

const filteredVacations = computed(() =>
  vacations.value.filter((vacation) =>
    ["PLANNED", "REQUESTED"].includes(vacation.status),
  ),
);

onMounted(() => {
  fetchData().catch(() => undefined);
});

async function fetchData() {
  loading.value = true;
  loadError.value = "";
  try {
    const periods = await openPlanningPeriods();
    openedPeriods.value = periods;
    await loadDaysNotIncluded(getDefaultYears());
    requestAction.setDaysNotIncluded(daysNotIncludedInVacations.value);
    const data = await fetchMyVacations();
    vacations.value = data.filter((item) => item.startDate && item.endDate);
  } catch (error) {
    loadError.value = errorUtils.shortMessage(error);
  } finally {
    loading.value = false;
  }
}

function vacationCanBeRejected(vacation: MyVacation): boolean {
  if (vacation.status !== "REQUESTED") {
    return false;
  }
  return openedPeriods.value.some((period) => period.year === vacation.year);
}

function openRejectDialog(id: number) {
  rejectId.value = id;
  rejectDialogOpen.value = true;
}

function closeRejectDialog() {
  rejectDialogOpen.value = false;
  rejectLoading.value = false;
  rejectError.value = "";
  rejectId.value = null;
}

async function confirmReject() {
  if (!rejectId.value || rejectLoading.value) {
    return;
  }
  rejectLoading.value = true;
  rejectError.value = "";
  try {
    await rejectVacationRequest(rejectId.value);
    closeRejectDialog();
    await fetchData();
  } catch (error) {
    rejectError.value = errorUtils.shortMessage(error);
  } finally {
    rejectLoading.value = false;
  }
}

async function submitRequest() {
  await requestAction.submit();
  if (!requestDialogOpen.value) {
    await fetchData();
  }
}
</script>

<style scoped>
.vacations-table .vacations-table__delete {
  visibility: hidden;
}

.vacations-table :deep(tbody tr:hover) .vacations-table__delete {
  visibility: visible;
}
</style>
