<template>
  <v-container class="py-6" data-testid="overtimes-view">
    <v-alert
      v-if="!canViewAllOvertimes"
      type="warning"
      variant="tonal"
      border="start"
    >
      {{ t("Не достаточно прав") }}
    </v-alert>

    <v-card v-else data-testid="overtimes-card">
      <v-card-title>
        <v-row align="center" class="w-100">
          <v-col cols="12" md="8" class="d-flex align-center ga-2">
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
              label-test-id="overtimes-period-label"
              next-test-id="overtimes-next-period"
              @prev="decrementPeriod"
              @next="incrementPeriod"
              @go-current="goToCurrentPeriod"
            />
            <v-icon
              v-if="periodClosed"
              color="primary"
              icon="mdi-lock"
              :title="t('Период закрыт для внесения изменений')"
            />
          </v-col>
          <v-col cols="12" md="4" class="d-flex justify-end ga-2">
            <table-toolbar-actions
              v-if="canExportOvertimes"
              :disabled="loading"
              show-export
              :export-label="t('Экспорт в Excel')"
              @export="exportToExcel"
            />

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
                      ? "Переоткрыть период. Вернуть возможность вносить изменения"
                      : "Закрыть период. Запретить внесение изменений.",
                  )
                }}
              </span>
            </v-tooltip>
          </v-col>
        </v-row>
      </v-card-title>

      <v-card-text>
        <v-row align="center">
          <v-col cols="12" md="3" class="pb-0">
            <v-text-field
              v-model="filter.search"
              data-testid="overtimes-filter-search"
              density="compact"
              clearable
              :disabled="loading"
              :label="t('ФИО Сотрудника')"
            />
          </v-col>
          <v-col cols="12" md="3" class="pb-0">
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
            />
          </v-col>
          <v-col cols="12" md="3" class="pb-0">
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
            />
          </v-col>
          <v-col cols="12" md="3" class="d-flex align-center pb-0">
            <div class="d-flex align-center ga-2">
              <v-checkbox-btn
                v-model="filter.showEmpty"
                :disabled="loading"
                density="compact"
              />
              <span class="text-body-2">{{ t("Сотрудники без овертаймов") }}</span>
            </div>
          </v-col>
        </v-row>

        <div class="mb-3">
          {{ t("Итого (с учётом фильтров)") }}: {{ t("hours", totalHours) }}
        </div>

        <HREasyTableBase
          :headers="headers"
          :items="filteredOvertimes"
          height="60vh"
          fixed-header
          :loading="loading"
          :loading-text="t('Загрузка_данных')"
          :no-data-text="t('Отсутствуют данные')"
          :sort-by="[{ key: 'totalHours', order: 'desc' }]"
          density="compact"
          hover
          @click:row="onRowClick"
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
  </v-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import EmployeeOvertimeCard from "@/components/overtimes/EmployeeOvertimeCard.vue";
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
