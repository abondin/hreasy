<!--
  Vacations management view with list, summary, and timeline tabs.
-->
<template>
  <v-container class="py-6" data-testid="vacations-view">
    <v-alert
      v-if="!canViewVacations"
      type="warning"
      variant="tonal"
      border="start"
    >
      {{ t("Не достаточно прав") }}
    </v-alert>

    <v-card v-else data-testid="vacations-card">
      <v-tabs v-model="selectedTab" data-testid="vacations-tabs">
        <v-tab data-testid="vacations-tab-list">{{ t("Все отпуска") }}</v-tab>
        <v-tab data-testid="vacations-tab-summary">{{ t("Сводная по сотрудникам") }}</v-tab>
        <v-tab data-testid="vacations-tab-timeline">{{ t("График отпусков") }}</v-tab>
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
              data-testid="vacations-filter-year"
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
              data-testid="vacations-filter-search"
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
          <HREasyTableBase
            class="vacations-list-table text-truncate"
            height="60vh"
            fixed-header
            :loading="loading"
            :loading-text="t('Загрузка_данных')"
            :no-data-text="t('Отсутствуют данные')"
            :headers="headers"
            :items="filteredItems"
            :sort-by="[{ key: 'employeeDisplayName', order: 'asc' }]"
            density="compact"
            multi-sort
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
          </HREasyTableBase>
        </v-window-item>

        <v-window-item>
          <HREasyTableBase
            height="60vh"
            fixed-header
            :loading="loading"
            :loading-text="t('Загрузка_данных')"
            :no-data-text="t('Отсутствуют данные')"
            :headers="summaryHeaders"
            :items="filteredSummaryItems"
            :sort-by="[{ key: 'employeeDisplayName', order: 'asc' }]"
            density="compact"
            multi-sort
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
          </HREasyTableBase>
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
import { nextTick, ref } from "vue";
import { useI18n } from "vue-i18n";
import MyDateRangeComponent from "@/components/shared/MyDateRangeComponent.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import VacationEditForm from "@/components/vacations/VacationEditForm.vue";
import VacationsTimeline from "@/components/vacations/VacationsTimeline.vue";
import type { EmployeeVacationSummary } from "@/components/vacations/employeeVacationSummaryService";
import { formatDate } from "@/lib/datetime";
import { extractDataTableRow } from "@/lib/data-table";
import { type Vacation } from "@/services/vacation.service";
import { useVacationsManagement } from "@/composables/useVacationsManagement";

const { t } = useI18n();
const {
  canViewVacations,
  canEditVacations,
  canExportVacations,
  selectedTab,
  headers,
  summaryHeaders,
  allStatuses,
  allYears,
  selectedYear,
  filter,
  loading,
  projectOptions,
  projectRoles,
  allEmployees,
  daysNotIncludedInVacations,
  snackbarNotification,
  snackbarMessage,
  filteredItems,
  filteredSummaryItems,
  fetchData,
  exportToExcel,
  selectEmployee,
  statusesForForm,
  timeLineYearChanged,
  copyToClipboard,
  init,
} = useVacationsManagement(t);

const vacationDialog = ref(false);
const selectedVacation = ref<Vacation | null>(null);
const vacationEditForm = ref<InstanceType<typeof VacationEditForm> | null>(null);

init();

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

function onVacationRowClick(
  _event: Event,
  payload: unknown,
) {
  const row = extractRow<Vacation>(payload);
  if (!row || !canEditVacations.value) {
    return;
  }
  openVacationDialog(row);
}

function onSummaryRowClick(
  _event: Event,
  payload: unknown,
) {
  const row = extractRow<EmployeeVacationSummary>(payload);
  if (!row) {
    return;
  }
  selectEmployee(row);
}

function extractRow<T>(payload: unknown): T | null {
  return extractDataTableRow<T>(payload);
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
