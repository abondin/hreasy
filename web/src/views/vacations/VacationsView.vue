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

      <v-card-text class="pt-4 pb-2">
        <div class="vacations-filter-bar d-flex flex-wrap flex-lg-nowrap ga-4">
          <div class="vacations-filter-bar__actions vacations-filter-bar__actions--left d-flex align-center">
            <v-btn-group divided variant="text">
              <v-tooltip location="bottom">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    data-testid="toolbar-refresh"
                    icon="mdi-refresh"
                    size="small"
                    density="comfortable"
                    :disabled="loading"
                    @click="fetchData(false)"
                  />
                </template>
                <span>{{ t("Обновить данные") }}</span>
              </v-tooltip>

              <v-tooltip v-if="canExportVacations" location="bottom">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    data-testid="toolbar-export"
                    icon="mdi-file-excel"
                    size="small"
                    density="comfortable"
                    :disabled="loading"
                    @click="exportToExcel"
                  />
                </template>
                <span>{{ t("Экспорт в Excel") }}</span>
              </v-tooltip>
            </v-btn-group>
          </div>

          <div class="vacations-filter-bar__filters flex-grow-1">
            <div class="vacations-filter-bar__filters-grid d-flex flex-wrap ga-4">
              <div class="vacations-filter-bar__field vacations-filter-bar__field--year">
                <v-select
                  v-model="selectedYear"
                  data-testid="vacations-filter-year"
                  :items="allYears"
                  :disabled="loading"
                  :label="t('Год')"
                  variant="outlined"
                  density="compact"
                  hide-details
                />
              </div>

              <div class="vacations-filter-bar__field vacations-filter-bar__field--date">
                <my-date-range-component
                  v-model="filter.selectedDates"
                  :disabled="loading"
                  :label="t('Дата начала отпуска')"
                  variant="outlined"
                  hide-details
                />
              </div>

              <div class="vacations-filter-bar__field vacations-filter-bar__field--status">
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
                  :disabled="loading"
                  hide-details
                >
                  <template #selection="{ item, index }">
                    <template v-if="index === 0">
                      <v-chip size="small" label>
                        {{ getFilterSelectionLabel(item) }}
                      </v-chip>
                    </template>
                    <span v-else-if="index === 1" class="text-caption text-medium-emphasis ml-2">
                      (+{{ filter.selectedStatuses.length - 1 }})
                    </span>
                  </template>
                </v-select>
              </div>

              <div class="vacations-filter-bar__field vacations-filter-bar__field--search">
                <v-text-field
                  v-model="filter.search"
                  data-testid="vacations-filter-search"
                  :label="t('Поиск')"
                  prepend-inner-icon="mdi-magnify"
                  variant="outlined"
                  density="compact"
                  clearable
                  :disabled="loading"
                  hide-details
                />
              </div>

              <div class="vacations-filter-bar__field vacations-filter-bar__field--project">
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
                  :disabled="loading"
                  hide-details
                >
                  <template #selection="{ item, index }">
                    <template v-if="index === 0">
                      <v-chip size="small" label>
                        {{ getFilterSelectionLabel(item) }}
                      </v-chip>
                    </template>
                    <span v-else-if="index === 1" class="text-caption text-medium-emphasis ml-2">
                      (+{{ filter.selectedProjects.length - 1 }})
                    </span>
                  </template>
                </v-autocomplete>
              </div>

              <div class="vacations-filter-bar__field vacations-filter-bar__field--role">
                <v-autocomplete
                  v-model="filter.selectedProjectRoles"
                  :items="projectRoles"
                  :label="t('Роль на проекте')"
                  variant="outlined"
                  density="compact"
                  clearable
                  multiple
                  :disabled="loading"
                  hide-details
                >
                  <template #selection="{ item, index }">
                    <template v-if="index === 0">
                      <v-chip size="small" label>
                        {{ getFilterSelectionLabel(item) }}
                      </v-chip>
                    </template>
                    <span v-else-if="index === 1" class="text-caption text-medium-emphasis ml-2">
                      (+{{ filter.selectedProjectRoles.length - 1 }})
                    </span>
                  </template>
                </v-autocomplete>
              </div>
            </div>
          </div>

          <div class="vacations-filter-bar__actions vacations-filter-bar__actions--right d-flex align-center justify-end">
            <v-btn
              v-if="canEditVacations"
              color="primary"
              variant="outlined"
              prepend-icon="mdi-plus"
              size="small"
              :disabled="loading"
              @click="openVacationDialog(null)"
            >
              {{ t("Добавить отпуск") }}
            </v-btn>
          </div>
        </div>
      </v-card-text>

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

function getFilterSelectionLabel(item: unknown): string {
  if (typeof item === "string") {
    return item;
  }
  if (item && typeof item === "object") {
    if ("title" in item && typeof item.title === "string") {
      return item.title;
    }
    if ("name" in item && typeof item.name === "string") {
      return item.name;
    }
  }
  return "";
}

function extractRow<T>(payload: unknown): T | null {
  return extractDataTableRow<T>(payload);
}
</script>



<style scoped>
.vacations-filter-bar {
  align-items: flex-start;
}

.vacations-filter-bar__actions {
  flex: 0 0 auto;
}

.vacations-filter-bar__actions--left,
.vacations-filter-bar__actions--right {
  align-self: center;
}

.vacations-filter-bar__filters {
  min-width: 0;
}

.vacations-filter-bar__filters-grid {
  width: 100%;
}

.vacations-filter-bar__field {
  flex: 1 1 220px;
  min-width: 220px;
  max-width: 100%;
}

.vacations-filter-bar__field--year {
  flex-basis: 140px;
  min-width: 140px;
}

.vacations-filter-bar__field--date {
  flex-basis: 320px;
  min-width: 320px;
}

.vacations-filter-bar__field--status {
  flex-basis: 260px;
  min-width: 260px;
}

.vacations-filter-bar__field--search,
.vacations-filter-bar__field--project,
.vacations-filter-bar__field--role {
  flex-basis: 240px;
  min-width: 240px;
}

@media (min-width: 1280px) {
  .vacations-filter-bar {
    align-items: center;
  }
}

@media (max-width: 1279px) {
  .vacations-filter-bar__filters {
    flex-basis: 100%;
    order: 3;
  }

  .vacations-filter-bar__actions--right {
    margin-left: auto;
  }
}
</style>
