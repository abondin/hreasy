<!--
  Vacations management view with list, summary, and timeline tabs.
-->
<template>
  <TableFirstPageLayout test-id="vacations-view">
    <TableFirstPageState
      v-if="!canViewVacations"
      test-id="vacations-no-access"
      :title="t('Недостаточно прав')"
      :action-text="t('На главную')"
      :to="{ name: 'profile-main' }"
      icon="mdi-alert-outline"
      color="warning"
    />

    <template v-else>
      <v-tabs v-model="selectedTab" class="flex-shrink-0" data-testid="vacations-tabs">
        <v-tab :value="0" data-testid="vacations-tab-list">{{ t("Все отпуска") }}</v-tab>
        <v-tab :value="1" data-testid="vacations-tab-summary">{{ t("Сводная по сотрудникам") }}</v-tab>
        <v-tab :value="2" data-testid="vacations-tab-timeline">{{ t("График отпусков") }}</v-tab>
      </v-tabs>

      <div class="vacations-page-content mt-4 d-flex flex-column flex-grow-1 min-h-0">
        <v-card class="d-flex flex-column h-100" data-testid="vacations-card">
          <v-card-text class="vacations-card-content pt-4 pb-2 d-flex flex-column flex-grow-1 min-h-0 overflow-hidden">
            <AdaptiveFilterBar
              :items="filterBarItems"
              :has-right-actions="canEditVacations"
            >
              <template #left-actions>
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
              </template>

              <template #filter-year>
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
              </template>

              <template #filter-date>
                <my-date-range-component
                  v-model="filter.selectedDates"
                  :disabled="loading"
                  :label="t('Дата начала отпуска')"
                  variant="outlined"
                  hide-details
                />
              </template>

              <template #filter-search>
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
              </template>

              <template #filter-status>
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
                    <CollapsedSelectionContent
                      :index="index"
                      :total="filter.selectedStatuses.length"
                      :label="getFilterSelectionLabel(item)"
                    />
                  </template>
                </v-select>
              </template>

              <template #filter-project>
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
                    <CollapsedSelectionContent
                      :index="index"
                      :total="filter.selectedProjects.length"
                      :label="getFilterSelectionLabel(item)"
                    />
                  </template>
                </v-autocomplete>
              </template>

              <template #filter-department>
                <v-autocomplete
                  v-model="filter.selectedDepartments"
                  :items="departmentOptions"
                  item-title="name"
                  item-value="id"
                  :label="t('Отдел')"
                  variant="outlined"
                  density="compact"
                  clearable
                  multiple
                  :disabled="loading"
                  hide-details
                >
                  <template #selection="{ item, index }">
                    <CollapsedSelectionContent
                      :index="index"
                      :total="filter.selectedDepartments.length"
                      :label="getFilterSelectionLabel(item)"
                    />
                  </template>
                </v-autocomplete>
              </template>

              <template #filter-business-account>
                <v-autocomplete
                  v-model="filter.selectedBusinessAccounts"
                  :items="businessAccountOptions"
                  item-title="name"
                  item-value="id"
                  :label="t('Бизнес аккаунт')"
                  variant="outlined"
                  density="compact"
                  clearable
                  multiple
                  :disabled="loading"
                  hide-details
                >
                  <template #selection="{ item, index }">
                    <CollapsedSelectionContent
                      :index="index"
                      :total="filter.selectedBusinessAccounts.length"
                      :label="getFilterSelectionLabel(item)"
                    />
                  </template>
                </v-autocomplete>
              </template>

              <template #filter-role>
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
                    <CollapsedSelectionContent
                      :index="index"
                      :total="filter.selectedProjectRoles.length"
                      :label="getFilterSelectionLabel(item)"
                    />
                  </template>
                </v-autocomplete>
              </template>

              <template #right-actions>
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
              </template>
            </AdaptiveFilterBar>

            <VacationsListTab
              v-if="selectedTab === 0"
              class="flex-grow-1 min-h-0"
              :loading="loading"
              :headers="headers"
              :items="filteredItems"
              :on-row-click="onVacationRowClick"
              :copy-to-clipboard="copyToClipboard"
            />

            <VacationsSummaryTab
              v-else-if="selectedTab === 1"
              class="flex-grow-1 min-h-0"
              :loading="loading"
              :headers="summaryHeaders"
              :items="filteredSummaryItems"
              :on-row-click="onSummaryRowClick"
            />

            <VacationsTimelineTab
              v-else
              class="flex-grow-1 min-h-0"
              :items="filteredItems"
              :year="selectedYear"
              @year-navigation="timeLineYearChanged"
            />
          </v-card-text>

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
      </div>
    </template>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from "vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import TableFirstPageState from "@/components/shared/TableFirstPageState.vue";
import { useI18n } from "vue-i18n";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import MyDateRangeComponent from "@/components/shared/MyDateRangeComponent.vue";
import VacationEditForm from "@/components/vacations/VacationEditForm.vue";
import type { EmployeeVacationSummary } from "@/components/vacations/employeeVacationSummaryService";
import VacationsListTab from "@/views/vacations/components/VacationsListTab.vue";
import VacationsSummaryTab from "@/views/vacations/components/VacationsSummaryTab.vue";
import VacationsTimelineTab from "@/views/vacations/components/VacationsTimelineTab.vue";
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
  allEmployees,
  projectRoles,
  employeeDirectory,
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
const departmentOptions = computed(() => {
  const map = new Map<number, string>();
  employeeDirectory.value.forEach((employee) => {
    if (employee.department?.id && employee.department?.name) {
      map.set(employee.department.id, employee.department.name);
    }
  });
  return Array.from(map.entries()).map(([id, name]) => ({ id, name }));
});
const businessAccountOptions = computed(() => {
  const map = new Map<number, string>();
  employeeDirectory.value.forEach((employee) => {
    if (employee.ba?.id && employee.ba?.name) {
      map.set(employee.ba.id, employee.ba.name);
    }
  });
  return Array.from(map.entries()).map(([id, name]) => ({ id, name }));
});

const filterBarItems = computed(() => [
  { id: "year", minWidth: 140, active: Boolean(selectedYear.value) },
  { id: "date", minWidth: 320, active: filter.selectedDates.length > 0 },
  { id: "search", minWidth: 380, active: filter.search.trim().length > 0, grow: true },
  { id: "status", minWidth: 260, active: filter.selectedStatuses.length > 0 },
  { id: "project", minWidth: 240, active: filter.selectedProjects.length > 0 },
  { id: "department", minWidth: 240, active: filter.selectedDepartments.length > 0 },
  { id: "business-account", minWidth: 240, active: filter.selectedBusinessAccounts.length > 0 },
  { id: "role", minWidth: 240, active: filter.selectedProjectRoles.length > 0 },
]);

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
.vacations-page-content {
  min-height: 0;
}

.vacations-card-content {
  min-height: 0;
}
</style>
