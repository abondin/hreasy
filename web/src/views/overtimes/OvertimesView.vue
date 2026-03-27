<template>
  <TableFirstPageLayout test-id="overtimes-view">
    <TableFirstPageState
      v-if="!canViewAllOvertimes"
      test-id="overtimes-no-access"
      :title="t('Недостаточно прав')"
      :action-text="t('На главную')"
      :to="{ name: 'profile-main' }"
      icon="mdi-alert-outline"
      color="warning"
    />

        <v-card v-else class="d-flex flex-column h-100" data-testid="overtimes-card">
      <v-card-text class="pt-4 pb-2 d-flex flex-column flex-grow-1 min-h-0">
        <AdaptiveFilterBar
          :items="filterBarItems"
          :has-right-actions="canAdminOvertimes"
        >
          <template #left-actions>
            <div class="d-flex align-center ga-2">
              <table-toolbar-actions
                :disabled="loading"
                show-refresh
                :refresh-label="t('Обновить данные')"
                @refresh="fetchData"
              />
              <period-switcher-control
                :label="selectedPeriod.toString()"
                :is-current="isCurrentPeriod"
                :go-current-label="t('Перейти к текущему')"
                :disabled="loading"
                :status-icon="periodClosed ? 'mdi-lock' : undefined"
                status-icon-color="primary"
                :status-icon-title="periodClosed ? t('Период закрыт для внесения изменений') : undefined"
                label-test-id="overtimes-period-label"
                next-test-id="overtimes-next-period"
                @prev="decrementPeriod"
                @next="incrementPeriod"
                @go-current="goToCurrentPeriod"
              />
              <table-toolbar-actions
                v-if="canExportOvertimes"
                :disabled="loading"
                show-export
                :export-label="t('Экспорт в Excel')"
                @export="exportToExcel"
              />
            </div>
          </template>

          <template #filter-search>
            <v-text-field
              v-model="filter.search"
              data-testid="overtimes-filter-search"
              density="compact"
              clearable
              :disabled="loading"
              :label="t('ФИО Сотрудника')"
              prepend-inner-icon="mdi-magnify"
              variant="outlined"
              hide-details
            />
          </template>

          <template #filter-current-project>
            <v-autocomplete
              v-model="filter.selectedEmployeeCurrentProjects"
              density="compact"
              clearable
              multiple
              :disabled="loading"
              :items="activeProjects"
              item-title="name"
              item-value="id"
              :label="t('Текущий проект')"
              variant="outlined"
              hide-details
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="filter.selectedEmployeeCurrentProjects.length"
                  :label="getFilterSelectionLabel(item)"
                />
              </template>
            </v-autocomplete>
          </template>

          <template #filter-overtime-project>
            <v-autocomplete
              v-model="filter.selectedProjectsWithOvertimes"
              density="compact"
              clearable
              multiple
              :disabled="loading"
              :items="projectsWithOvertimes"
              item-title="name"
              item-value="id"
              :label="t('Проект овертайма')"
              variant="outlined"
              hide-details
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="filter.selectedProjectsWithOvertimes.length"
                  :label="getFilterSelectionLabel(item)"
                />
              </template>
            </v-autocomplete>
          </template>

          <template #filter-show-empty>
            <v-checkbox
              v-model="filter.showEmpty"
              :disabled="loading"
              density="compact"
              :label="t('Сотрудники без овертаймов')"
              hide-details
            />
          </template>

          <template #right-actions>
            <v-tooltip location="bottom" v-if="canAdminOvertimes">
              <template #activator="{ props }">
                <v-btn
                  v-bind="props"
                  :icon="periodClosed ? 'mdi-lock-open' : 'mdi-lock'"
                  variant="text"
                  :disabled="loading"
                  data-testid="overtimes-period-toggle"
                  @click="periodClosed ? reopenPeriod() : closePeriod()"
                />
              </template>
              <span>
                {{
                  t(
                    periodClosed
                      ? 'Переоткрыть период. Вернуть возможность вносить изменения'
                      : 'Закрыть период. Запретить внесение изменений.',
                  )
                }}
              </span>
            </v-tooltip>
          </template>
        </AdaptiveFilterBar>

        <div class="mb-3 mt-3">
          {{ t("Итого (с учётом фильтров)") }}: {{ t("hours", totalHours) }}
        </div>

        <div class="flex-grow-1 min-h-0">
        <HREasyTableBase
          :headers="headers"
          :items="filteredOvertimes"
          fixed-header
          :loading="loading"
          :loading-text="t('Загрузка_данных')"
          :no-data-text="t('Отсутствуют данные')"
          :sort-by="[{ key: 'totalHours', order: 'desc' }]"
          density="compact"
          hover
          @click:row="onRowClick"
          height="fill"
        >
          <template #[`item.commonApprovalStatus`]="{ item }">
            <v-chip v-if="item.commonApprovalStatus === 'DECLINED'" variant="outlined">
              <v-icon class="status-icon declined" icon="mdi-do-not-disturb" />
              {{ t("APPROVAL_DECISION_ENUM.DECLINED") }}
            </v-chip>
            <v-chip
              v-else-if="item.commonApprovalStatus === 'APPROVED_NO_DECLINED'"
              variant="outlined"
            >
              <v-icon class="status-icon approved" icon="mdi-checkbox-marked-circle" />
              {{ t("APPROVAL_DECISION_ENUM.APPROVED") }}
            </v-chip>
            <v-chip
              v-else-if="item.commonApprovalStatus === 'APPROVED_OUTDATED'"
              variant="outlined"
            >
              <v-icon class="status-icon outdated" icon="mdi-clock-alert" />
              {{ t("Изменения после согласования") }}
            </v-chip>
            <span v-else>{{ t("Отсутствуют") }}</span>
          </template>
        </HREasyTableBase>
        </div>
      </v-card-text>
    </v-card>

    <v-dialog v-model="employeeDialog" max-width="1280" data-testid="overtimes-employee-dialog">
      <v-card v-if="selectedEmployee">
        <v-card-title class="d-flex align-center">
          {{
            t("overtimes_for_employee_for_period", {
              employee: selectedEmployee.name,
              period: selectedPeriod.toString(),
            })
          }}
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" @click="closeEmployeeDialog" />
        </v-card-title>
        <v-card-text>
          <employee-overtime-card
            :employee-id="selectedEmployee.id"
            :change-period-allowed="false"
            :fixed-period-id="selectedPeriodId"
            :closed-periods="closedPeriods"
            @updated="fetchData"
          />
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="exportCompleted" timeout="5000">
      {{ t("Экспорт успешно завершён. Файл скачен.") }}
      <template #actions>
        <v-btn icon="mdi-close-circle-outline" @click="exportCompleted = false" />
      </template>
    </v-snackbar>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed } from "vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import TableFirstPageState from "@/components/shared/TableFirstPageState.vue";
import { useI18n } from "vue-i18n";
import EmployeeOvertimeCard from "@/components/overtimes/EmployeeOvertimeCard.vue";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import PeriodSwitcherControl from "@/components/shared/PeriodSwitcherControl.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import { useOvertimesSummary } from "@/composables/useOvertimesSummary";
import { ReportPeriod } from "@/services/overtime.service";

const { t } = useI18n();
const {
  loading,
  exportCompleted,
  selectedPeriodId,
  selectedPeriod,
  closedPeriods,
  filter,
  selectedEmployee,
  employeeDialog,
  canViewAllOvertimes,
  canExportOvertimes,
  canAdminOvertimes,
  headers,
  activeProjects,
  periodClosed,
  projectsWithOvertimes,
  filteredOvertimes,
  totalHours,
  fetchData,
  incrementPeriod,
  decrementPeriod,
  closeEmployeeDialog,
  onRowClick,
  exportToExcel,
  closePeriod,
  reopenPeriod,
} = useOvertimesSummary(t);

const isCurrentPeriod = computed(() => selectedPeriodId.value === ReportPeriod.currentPeriod().periodId());
const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: filter.search.trim().length > 0, grow: true },
  { id: "current-project", minWidth: 320, active: filter.selectedEmployeeCurrentProjects.length > 0 },
  { id: "overtime-project", minWidth: 320, active: filter.selectedProjectsWithOvertimes.length > 0 },
  { id: "show-empty", minWidth: 260, active: filter.showEmpty },
]);

function getFilterSelectionLabel(item: unknown): string {
  if (typeof item === "string") {
    return item;
  }
  if (item && typeof item === "object" && "name" in item && typeof item.name === "string") {
    return item.name;
  }
  return "";
}

async function goToCurrentPeriod(): Promise<void> {
  const currentPeriodId = ReportPeriod.currentPeriod().periodId();
  if (selectedPeriodId.value === currentPeriodId) {
    return;
  }
  selectedPeriodId.value = currentPeriodId;
  await fetchData();
}
</script>

<style scoped>
.status-icon.approved {
  color: green;
}

.status-icon.declined {
  color: red;
}

.status-icon.outdated {
  color: orange;
}
</style>
