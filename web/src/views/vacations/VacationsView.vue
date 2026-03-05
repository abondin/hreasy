<!--
  Vacations management view with list, summary, and timeline tabs.
-->
<template>
  <v-container class="py-6">
    <v-alert
      v-if="!canViewVacations"
      type="warning"
      variant="tonal"
      border="start"
    >
      {{ t("Не достаточно прав") }}
    </v-alert>

    <v-card v-else>
      <v-tabs v-model="selectedTab">
        <v-tab>{{ t("Все отпуска") }}</v-tab>
        <v-tab>{{ t("Сводная по сотрудникам") }}</v-tab>
        <v-tab>{{ t("График отпусков") }}</v-tab>
      </v-tabs>

      <v-container class="pt-4">
        <v-row align="center">
          <v-col cols="auto" class="pb-0">
            <table-toolbar-actions
              :disabled="loading"
              show-refresh
              :show-add="canEditVacations"
              :show-export="canExportVacations"
              :refresh-label="t('Обновить данные')"
              :add-label="t('Добавить отпуск')"
              :export-label="t('Экспорт в Excel')"
              @refresh="fetchData(false)"
              @add="openVacationDialog(null)"
              @export="exportToExcel"
            />
          </v-col>

          <v-col cols="12" sm="2" class="pb-0">
            <v-select
              v-model="selectedYear"
              density="compact"
              :items="allYears"
              :disabled="loading"
              :label="t('Год')"
            />
          </v-col>

          <v-col cols="12" sm="4" class="pb-0">
            <my-date-range-component
              v-model="filter.selectedDates"
              :disabled="loading"
              :label="t('Дата начала отпуска')"
            />
          </v-col>

          <v-col cols="12" sm="3" class="pb-0">
            <v-select
              v-model="filter.selectedStatuses"
              density="compact"
              clearable
              multiple
              :disabled="loading"
              :items="allStatuses"
              item-title="title"
              item-value="value"
              :label="t('Статус')"
            />
          </v-col>

          <v-responsive width="100%" />

          <v-col cols="12" sm="4" class="pb-0 pt-0">
            <v-text-field
              v-model="filter.search"
              density="compact"
              clearable
              :disabled="loading"
              :label="t('Поиск')"
            />
          </v-col>

          <v-col cols="12" sm="4" class="pb-0 pt-0">
            <v-autocomplete
              v-model="filter.selectedProjects"
              density="compact"
              clearable
              multiple
              :disabled="loading"
              :items="projectOptions"
              item-title="name"
              item-value="id"
              :label="t('Текущий проект')"
            />
          </v-col>

          <v-col cols="12" sm="4" class="pb-0 pt-0">
            <v-autocomplete
              v-model="filter.selectedProjectRoles"
              density="compact"
              clearable
              multiple
              :disabled="loading"
              :items="projectRoles"
              :label="t('Роль на проекте')"
            />
          </v-col>
        </v-row>
      </v-container>

      <v-window v-model="selectedTab">
        <v-window-item>
          <v-data-table
            class="vacations-list-table text-truncate"
            :loading="loading"
            :loading-text="t('Загрузка_данных')"
            :no-data-text="t('Отсутствуют данные')"
            :headers="headers"
            :items="filteredItems"
            :sort-by="[{ key: 'employeeDisplayName', order: 'asc' }]"
            density="compact"
            multi-sort
            :items-per-page="defaultItemsPerPage"
            hover
            @click:row="onVacationRowClick"
          >
            <template v-slot:[`item.employeeDisplayName`]="{ item }">
              <div class="vacation-employee-cell">
                <span class="vacation-employee-cell__copy-slot">
                  <v-tooltip location="bottom">
                    <template #activator="{ props }">
                      <v-btn
                        v-bind="props"
                        icon="mdi-content-copy"
                        size="x-small"
                        variant="text"
                        class="vacation-employee-cell__copy"
                        @click.stop="copyToClipboard(item)"
                      />
                    </template>
                    <span>{{ t("Скопировать в буфер обмена") }}</span>
                  </v-tooltip>
                </span>
                <span>{{ item.employeeDisplayName }}</span>
              </div>
            </template>
            <template v-slot:[`item.startDate`]="{ item }">
              {{ formatDate(item.startDate) }}
            </template>
            <template v-slot:[`item.endDate`]="{ item }">
              {{ formatDate(item.endDate) }}
            </template>
            <template v-slot:[`item.plannedStartDate`]="{ item }">
              {{ formatDate(item.plannedStartDate) }}
            </template>
            <template v-slot:[`item.plannedEndDate`]="{ item }">
              {{ formatDate(item.plannedEndDate) }}
            </template>
            <template v-slot:[`item.status`]="{ item }">
              {{ t(`VACATION_STATUS_ENUM.${item.status}`) }}
            </template>
          </v-data-table>
        </v-window-item>

        <v-window-item>
          <v-data-table
            :loading="loading"
            :loading-text="t('Загрузка_данных')"
            :no-data-text="t('Отсутствуют данные')"
            :headers="summaryHeaders"
            :items="filteredSummaryItems"
            :sort-by="[{ key: 'employeeDisplayName', order: 'asc' }]"
            density="compact"
            multi-sort
            :items-per-page="defaultItemsPerPage"
            class="text-truncate"
            hover
            @click:row="onSummaryRowClick"
          >
            <template v-slot:[`item.employeeDisplayName`]="{ item }">
              <span>{{ item.employeeDisplayName }}</span>
            </template>
            <template v-slot:[`item.upcomingVacation`]="{ item }">
              <span v-if="item.upcomingVacation">
                {{ formatDate(item.upcomingVacation.startDate) }} -
                {{ formatDate(item.upcomingVacation.endDate) }}
                ({{ t(`VACATION_STATUS_ENUM.${item.upcomingVacation.status}`) }})
              </span>
            </template>
          </v-data-table>
        </v-window-item>

        <v-window-item>
          <vacations-timeline
            :vacations="filteredItems"
            :year="selectedYear"
            @year-navigation="timeLineYearChanged"
          />
        </v-window-item>
      </v-window>

      <v-dialog v-model="vacationDialog" max-width="720">
        <vacation-edit-form
          ref="vacationEditForm"
          :all-employees="allEmployees"
          :all-statuses="statusesForForm(selectedVacation)"
          :all-years="allYears"
          :input="selectedVacation"
          :default-year="selectedYear"
          :days-not-included-in-vacations="daysNotIncludedInVacations"
          @close="handleVacationDialogClose"
        />
      </v-dialog>

      <v-snackbar v-model="snackbarNotification" timeout="5000">
        {{ snackbarMessage }}
        <template #actions>
          <v-btn
            icon="mdi-close-circle-outline"
            @click="snackbarNotification = false"
          />
        </template>
      </v-snackbar>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import MyDateRangeComponent from "@/components/shared/MyDateRangeComponent.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import VacationEditForm from "@/components/vacations/VacationEditForm.vue";
import VacationsTimeline from "@/components/vacations/VacationsTimeline.vue";
import {
  employeeVacationSummaryMapper,
  type EmployeeVacationSummary,
} from "@/components/vacations/employeeVacationSummaryService";
import { errorUtils } from "@/lib/errors";
import { formatDate } from "@/lib/datetime";
import {
  formatDateOnly,
  getDefaultYears,
  getEndOfYear,
  getStartOfYear,
  isDateInRange,
} from "@/lib/vacation-dates";
import { usePermissions } from "@/lib/permissions";
import {
  exportVacations,
  fetchVacations,
  type Vacation,
  type VacationStatus,
  vacationStatuses,
} from "@/services/vacation.service";
import { useVacationsDictionaries } from "@/components/vacations/useVacationsDictionaries";

const { t } = useI18n();
const permissions = usePermissions();

const canViewVacations = computed(() => permissions.canViewAllVacations());
const canEditVacations = computed(() => permissions.canEditAllVacations());
const canExportVacations = computed(() => permissions.canExportAllVacations());

const selectedTab = ref(0);
const defaultItemsPerPage = 15;

const headers = computed(() => [
  { title: t("ФИО"), key: "employeeDisplayName" },
  { title: t("Текущий проект"), key: "employeeCurrentProject.name" },
  { title: t("Роль на проекте"), key: "employeeCurrentProject.role" },
  { title: t("Год"), key: "year" },
  { title: t("Начало"), key: "startDate" },
  { title: t("Окончание"), key: "endDate" },
  { title: t("Кол-во дней"), key: "daysNumber" },
  { title: t("Статус"), key: "status" },
  { title: t("Документ"), key: "documents" },
  { title: t("Примечание"), key: "notes" },
]);

const summaryHeaders = computed(() => [
  { title: t("ФИО"), key: "employeeDisplayName" },
  { title: t("Текущий проект"), key: "employeeCurrentProject.name" },
  { title: t("Роль на проекте"), key: "employeeCurrentProject.role" },
  { title: t("Год"), key: "year" },
  { title: t("Количество отпусков"), key: "vacationsNumber" },
  { title: t("Общее количество дней"), key: "vacationsTotalDays" },
  { title: t("Текущий или ближайший отпуск"), key: "upcomingVacation" },
]);

const allStatuses = computed(() =>
  vacationStatuses.map((status) => ({
    value: status,
    title: t(`VACATION_STATUS_ENUM.${status}`),
  })),
);

const allYears = getDefaultYears();
const selectedYear = ref(new Date().getFullYear());

const filter = reactive({
  selectedStatuses: ["PLANNED", "TAKEN", "REQUESTED"] as VacationStatus[],
  search: "",
  selectedProjects: [] as number[],
  selectedProjectRoles: [] as string[],
  selectedDates: [] as string[],
});

const loading = ref(false);
const vacations = ref<Vacation[]>([]);
const {
  projectOptions,
  projectRoles,
  employees: allEmployees,
  daysNotIncludedInVacations,
  loadProjectsAndRoles,
  loadEmployees,
  loadDaysNotIncluded,
} = useVacationsDictionaries();

const vacationDialog = ref(false);
const selectedVacation = ref<Vacation | null>(null);
const vacationEditForm = ref<InstanceType<typeof VacationEditForm> | null>(null);

const snackbarNotification = ref(false);
const snackbarMessage = ref("");

const filteredItems = computed(() => vacations.value.filter(filterItem));
const filteredSummaryItems = computed(() =>
  employeeVacationSummaryMapper.map(vacations.value).filter(filterItem),
);

watch(selectedYear, () => {
  fetchData(true).catch(() => undefined);
});

onMounted(() => {
  if (!canViewVacations.value) {
    return;
  }
  bootstrapData().catch(() => undefined);
});

watch(canViewVacations, (value) => {
  if (!value || vacations.value.length) {
    return;
  }
  bootstrapData().catch(() => undefined);
});

async function bootstrapData() {
  loading.value = true;
  try {
    await Promise.all([
      loadProjectsAndRoles(),
      loadEmployees(),
      loadDaysNotIncluded(allYears),
    ]);
    await fetchData(true);
  } catch (error) {
    snackbarMessage.value = errorUtils.shortMessage(error);
    snackbarNotification.value = true;
  } finally {
    loading.value = false;
  }
}

async function fetchData(resetFilter: boolean) {
  loading.value = true;
  try {
    const data = await fetchVacations([selectedYear.value]);
    vacations.value = data.filter(
      (item) => item.startDate && item.endDate && item.employeeDisplayName,
    );
    if (resetFilter) {
      resetSelectedDatesFilterToDefault();
    }
  } finally {
    loading.value = false;
  }
}

function resetSelectedDatesFilterToDefault() {
  const start = formatDateOnly(getStartOfYear(selectedYear.value));
  const end = formatDateOnly(getEndOfYear(selectedYear.value));
  filter.selectedDates = [start, end];
}

function filterItem(item: Vacation | EmployeeVacationSummary) {
  let filtered = true;
  filtered =
    filtered &&
    (filter.selectedProjects.length === 0 ||
      Boolean(
        item.employeeCurrentProject &&
          filter.selectedProjects.includes(item.employeeCurrentProject.id),
      ));
  filtered =
    filtered &&
    (filter.selectedProjectRoles.length === 0 ||
      Boolean(
        item.employeeCurrentProject?.role &&
          filter.selectedProjectRoles.includes(
            item.employeeCurrentProject.role,
          ),
      ));

  if ("status" in item) {
    filtered =
      filtered &&
      (filter.selectedStatuses.length === 0 ||
        filter.selectedStatuses.includes(item.status));
    filtered = filtered && isDateInRange(item.startDate, filter.selectedDates);
  }

  if (filter.search) {
    const search = filter.search.toLowerCase();
    const combined = `${item.employeeDisplayName ?? ""} ${
      item.employeeCurrentProject?.role ?? ""
    }`.toLowerCase();
    filtered = filtered && combined.includes(search);
  }

  return filtered;
}

async function exportToExcel() {
  loading.value = true;
  try {
    await exportVacations([selectedYear.value]);
    snackbarMessage.value = t("Экспорт успешно завершён. Файл скачен.");
    snackbarNotification.value = true;
  } finally {
    loading.value = false;
  }
}

function openVacationDialog(vacation: Vacation | null) {
  selectedVacation.value = vacation;
  vacationDialog.value = true;
  nextTick(() => {
    vacationEditForm.value?.resetForm();
  });
}

function handleVacationDialogClose() {
  vacationDialog.value = false;
  fetchData(false).catch(() => undefined);
}

function selectEmployee(item: { employeeDisplayName: string }) {
  filter.search = item.employeeDisplayName;
  selectedTab.value = 0;
}

function onVacationRowClick(
  _event: Event,
  payload: { item?: { raw?: Vacation } | Vacation } | Vacation,
) {
  const row = extractRow<Vacation>(payload);
  if (!row || !canEditVacations.value) {
    return;
  }
  openVacationDialog(row);
}

function onSummaryRowClick(
  _event: Event,
  payload:
    | { item?: { raw?: EmployeeVacationSummary } | EmployeeVacationSummary }
    | EmployeeVacationSummary,
) {
  const row = extractRow<EmployeeVacationSummary>(payload);
  if (!row) {
    return;
  }
  selectEmployee(row);
}

function extractRow<T>(
  payload: { item?: { raw?: T } | T } | T,
): T | null {
  if (!payload) {
    return null;
  }
  if (typeof payload === "object" && "item" in payload) {
    const item = payload.item as { raw?: T } | T | undefined;
    if (!item) {
      return null;
    }
    if (typeof item === "object" && item !== null && "raw" in item) {
      return (item as { raw?: T }).raw ?? null;
    }
    return item as T;
  }
  return payload as T;
}

function statusesForForm(vacation?: Vacation | null) {
  if (vacation && vacation.status === "REQUESTED") {
    return allStatuses.value;
  }
  return allStatuses.value.filter((status) => status.value !== "REQUESTED");
}

function timeLineYearChanged(year: number) {
  selectedYear.value = year;
  fetchData(true).catch(() => undefined);
}

async function copyToClipboard(vacation: Vacation | null) {
  if (!vacation) {
    return;
  }
  const text = `${vacation.employeeDisplayName} ${
    vacation.employeeCurrentProject?.name ?? ""
  } ${formatDate(vacation.startDate)}-${formatDate(vacation.endDate)} ${
    vacation.daysNumber
  } ${t(`VACATION_STATUS_ENUM.${vacation.status}`)}`.replace(/\s\s+/g, " ");

  try {
    await navigator.clipboard.writeText(text);
    snackbarMessage.value = t("Скопировано в буфер обмена");
    snackbarNotification.value = true;
  } catch (error) {
    snackbarMessage.value = errorUtils.shortMessage(error);
    snackbarNotification.value = true;
  }
}
</script>

<style scoped>
.vacation-employee-cell {
  display: inline-flex;
  align-items: center;
}

.vacation-employee-cell__copy-slot {
  display: inline-flex;
  width: 20px;
  margin-right: 4px;
}

.vacation-employee-cell__copy {
  visibility: hidden;
}

.vacations-list-table :deep(tbody tr:hover) .vacation-employee-cell__copy,
.vacations-list-table :deep(tbody tr:focus-within) .vacation-employee-cell__copy {
  visibility: visible;
}

@media (hover: none) {
  .vacation-employee-cell__copy {
    visibility: visible;
  }
}
</style>
