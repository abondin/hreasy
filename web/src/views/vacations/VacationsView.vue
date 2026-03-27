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

      <v-card-title class="pb-0 vacations-filters">
        <v-row class="vacations-filters__row align-center">
          <v-col cols="12" md="auto" class="vacations-filters__actions vacations-filters__actions--leading">
            <table-toolbar-actions
              :disabled="loading"
              show-refresh
              :show-export="canExportVacations"
              :refresh-label="t('Обновить данные')"
              :export-label="t('Экспорт в Excel')"
              @refresh="fetchData(false)"
              @export="exportToExcel"
            />
          </v-col>

          <v-col cols="12" md="2">
            <v-select
              v-model="selectedYear"
              data-testid="vacations-filter-year"
              :items="allYears"
              :disabled="loading"
              :label="t('Год')"
              variant="outlined"
              density="compact"
            />
          </v-col>

          <v-col cols="12" md="3">
            <my-date-range-component
              v-model="filter.selectedDates"
              :disabled="loading"
              :label="t('Дата начала отпуска')"
              variant="outlined"
            />
          </v-col>

          <v-col cols="12" md="3">
            <v-select
              v-model="filter.selectedStatuses"
              :items="allStatuses"
              item-title="title"
              item-value="value"
              :label="t('Статус')"
              variant="outlined"
              density="compact"
              clearable
              multiple
              chips
              :disabled="loading"
            />
          </v-col>

          <v-col cols="12" md="auto" class="vacations-filters__actions vacations-filters__actions--trailing">
            <table-toolbar-actions
              :disabled="loading"
              :show-add="canEditVacations"
              :add-label="t('Добавить отпуск')"
              @add="openVacationDialog(null)"
            />
          </v-col>

          <v-col cols="12" md="4">
            <v-text-field
              v-model="filter.search"
              data-testid="vacations-filter-search"
              :label="t('Поиск')"
              prepend-inner-icon="mdi-magnify"
              variant="outlined"
              density="compact"
              clearable
              :disabled="loading"
            />
          </v-col>

          <v-col cols="12" md="4">
            <v-autocomplete
              v-model="filter.selectedProjects"
              :items="projectOptions"
              item-title="name"
              item-value="id"
              :label="t('Текущий проект')"
              variant="outlined"
              density="compact"
              clearable
              multiple
              chips
              :disabled="loading"
            />
          </v-col>

          <v-col cols="12" md="4">
            <v-autocomplete
              v-model="filter.selectedProjectRoles"
              :items="projectRoles"
              :label="t('Роль на проекте')"
              variant="outlined"
              density="compact"
              clearable
              multiple
              chips
              :disabled="loading"
            />
          </v-col>
        </v-row>
      </v-card-title>

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
              <v-hover v-slot="{ isHovering, props: hoverProps }">
                <div
                  v-bind="hoverProps"
                  class="d-inline-flex align-center ga-1"
                >
                  <span>{{ item.employeeDisplayName }}</span>
                  <v-fade-transition>
                    <v-tooltip
                      v-if="isHovering || smAndDown"
                      location="bottom"
                    >
                      <template #activator="{ props }">
                        <v-btn
                          v-bind="props"
                          icon="mdi-content-copy"
                          size="x-small"
                          variant="text"
                          @click.stop="copyToClipboard(item)"
                        />
                      </template>
                      <span>{{ t("Скопировать в буфер обмена") }}</span>
                    </v-tooltip>
                  </v-fade-transition>
                </div>
              </v-hover>
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
import { useDisplay } from "vuetify";
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
const { smAndDown } = useDisplay();
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
.vacations-filters__row {
  row-gap: 0;
}

.vacations-filters :deep(.v-col) {
  padding-top: 6px;
  padding-bottom: 6px;
}

.vacations-filters__actions {
  display: flex;
  align-items: flex-start;
}

.vacations-filters__actions--trailing {
  justify-content: flex-end;
}
</style>

