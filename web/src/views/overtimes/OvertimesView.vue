<template>
  <v-container class="py-6">
    <v-alert
      v-if="!canViewAllOvertimes"
      type="warning"
      variant="tonal"
      border="start"
    >
      {{ t("Не достаточно прав") }}
    </v-alert>

    <v-card v-else>
      <v-card-title>
        <v-row align="center" class="w-100">
          <v-col cols="12" md="8" class="d-flex align-center ga-2">
            <v-btn icon="mdi-refresh" variant="text" :disabled="loading" @click="fetchData" />
            <v-btn icon="mdi-chevron-left" variant="text" :disabled="loading" @click="decrementPeriod" />
            <span>{{ selectedPeriod.toString() }}</span>
            <v-icon
              v-if="periodClosed"
              color="primary"
              icon="mdi-lock"
              :title="t('Период закрыт для внесения изменений')"
            />
            <v-btn icon="mdi-chevron-right" variant="text" :disabled="loading" @click="incrementPeriod" />
          </v-col>
          <v-col cols="12" md="4" class="d-flex justify-end ga-2">
            <v-tooltip location="bottom" v-if="canExportOvertimes">
              <template #activator="{ props }">
                <v-btn
                  v-bind="props"
                  icon="mdi-file-excel"
                  variant="text"
                  :disabled="loading"
                  @click="exportToExcel"
                />
              </template>
              <span>{{ t("Экспорт в Excel") }}</span>
            </v-tooltip>

            <v-tooltip location="bottom" v-if="canAdminOvertimes">
              <template #activator="{ props }">
                <v-btn
                  v-bind="props"
                  :icon="periodClosed ? 'mdi-lock-open' : 'mdi-lock'"
                  variant="text"
                  :disabled="loading"
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
        <v-row>
          <v-col cols="12" md="3">
            <v-text-field
              v-model="filter.search"
              density="compact"
              clearable
              :disabled="loading"
              :label="t('ФИО Сотрудника')"
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-autocomplete
              v-model="filter.selectedEmployeeCurrentProjects"
              density="compact"
              clearable
              multiple
              :disabled="loading"
              :items="activeProjects"
              item-title="name"
              item-value="id"
              :label="t('Текущий проект сотрудника')"
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-autocomplete
              v-model="filter.selectedProjectsWithOvertimes"
              density="compact"
              clearable
              multiple
              :disabled="loading"
              :items="projectsWithOvertimes"
              item-title="name"
              item-value="id"
              :label="t('Проекты, в которые списаны овертаймы')"
            />
          </v-col>
          <v-col cols="12" md="3" class="d-flex align-center">
            <v-checkbox
              v-model="filter.showEmpty"
              :disabled="loading"
              :label="t('Сотрудники без овертаймов')"
              hide-details
            />
          </v-col>
        </v-row>

        <div class="mb-3">
          {{ t("Итого (с учётом фильтров)") }}: {{ t("hours", totalHours) }}
        </div>

        <v-data-table
          :headers="headers"
          :items="filteredOvertimes"
          :loading="loading"
          :loading-text="t('Загрузка_данных')"
          :no-data-text="t('Отсутствуют данные')"
          :sort-by="[{ key: 'totalHours', order: 'desc' }]"
          density="compact"
          :items-per-page="50"
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
        </v-data-table>
      </v-card-text>
    </v-card>

    <v-dialog v-model="employeeDialog" max-width="1280">
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
import { computed, onMounted, reactive, ref } from "vue";
import { useI18n } from "vue-i18n";
import { usePermissions } from "@/lib/permissions";
import { listEmployees, type Employee } from "@/services/employee.service";
import {
  closeOvertimePeriod,
  reopenOvertimePeriod,
} from "@/services/admin-overtime.service";
import { fetchProjects, type SimpleDict } from "@/services/projects.service";
import {
  exportOvertimes,
  fetchClosedOvertimes,
  fetchOvertimesSummary,
  ReportPeriod,
  type ClosedOvertimePeriod,
  type CommonApprovalStatus,
  type OvertimeEmployeeSummary,
} from "@/services/overtime.service";
import EmployeeOvertimeCard from "@/components/overtimes/EmployeeOvertimeCard.vue";

interface EmployeeRef {
  id: number;
  name: string;
}

interface OvertimeRow {
  employee: EmployeeRef;
  totalHours: number;
  commonApprovalStatus: CommonApprovalStatus;
  overtimeProjectIds: number[];
}

const { t } = useI18n();
const permissions = usePermissions();

const loading = ref(false);
const exportCompleted = ref(false);

const selectedPeriodId = ref(ReportPeriod.currentPeriod().periodId());
const selectedPeriod = computed(() => ReportPeriod.fromPeriodId(selectedPeriodId.value));

const closedPeriods = ref<ClosedOvertimePeriod[]>([]);
const employees = ref<Employee[]>([]);
const projects = ref<SimpleDict[]>([]);
const summary = ref<OvertimeEmployeeSummary[]>([]);

const filter = reactive({
  search: "",
  showEmpty: true,
  selectedProjectsWithOvertimes: [] as number[],
  selectedEmployeeCurrentProjects: [] as number[],
});

const selectedEmployee = ref<EmployeeRef | null>(null);
const employeeDialog = ref(false);

const canViewAllOvertimes = computed(() => permissions.canViewAllOvertimes());
const canExportOvertimes = computed(() => permissions.canExportOvertimes());
const canAdminOvertimes = computed(() => permissions.canAdminOvertimes());

const headers = computed(() => [
  { title: t("Сотрудник"), key: "employee.name" },
  { title: t("Всего"), key: "totalHours" },
  { title: t("Статус согласования"), key: "commonApprovalStatus" },
]);

const activeProjects = computed(() => projects.value.filter((project) => project.active !== false));

const periodClosed = computed(() => {
  return closedPeriods.value.some((period) => period.period === selectedPeriodId.value);
});

const baseRows = computed<OvertimeRow[]>(() => {
  const overtimeByEmployee = new Map<number, OvertimeEmployeeSummary>();
  summary.value.forEach((item) => overtimeByEmployee.set(item.employeeId, item));

  return employees.value.map((employee) => {
    const employeeSummary = overtimeByEmployee.get(employee.id);
    const items = employeeSummary?.items ?? [];
    return {
      employee: {
        id: employee.id,
        name: employee.displayName,
      },
      totalHours: items.reduce((sum, item) => sum + item.hours, 0),
      commonApprovalStatus: employeeSummary?.commonApprovalStatus ?? "NO_DECISIONS",
      overtimeProjectIds: [...new Set(items.map((item) => item.projectId))],
    };
  });
});

const projectsWithOvertimes = computed(() => {
  const ids = new Set<number>();
  baseRows.value.forEach((row) => {
    row.overtimeProjectIds.forEach((id) => ids.add(id));
  });
  return projects.value.filter((project) => ids.has(project.id));
});

const filteredOvertimes = computed(() => {
  return baseRows.value.filter((row) => {
    if (
      filter.search &&
      !row.employee.name.toLowerCase().includes(filter.search.toLowerCase())
    ) {
      return false;
    }

    const employee = employees.value.find((item) => item.id === row.employee.id);
    if (
      filter.selectedEmployeeCurrentProjects.length > 0 &&
      !filter.selectedEmployeeCurrentProjects.includes(employee?.currentProject?.id ?? -1)
    ) {
      return false;
    }

    if (filter.selectedProjectsWithOvertimes.length > 0) {
      const hasAnyProject = row.overtimeProjectIds.some((projectId) =>
        filter.selectedProjectsWithOvertimes.includes(projectId),
      );
      if (!hasAnyProject) {
        return false;
      }
    }

    if (!filter.showEmpty && row.totalHours <= 0) {
      return false;
    }

    return true;
  });
});

const totalHours = computed(() => {
  return filteredOvertimes.value.reduce((sum, row) => sum + row.totalHours, 0);
});

onMounted(() => {
  fetchData().catch(() => undefined);
});

async function fetchData(): Promise<void> {
  if (!canViewAllOvertimes.value) {
    return;
  }
  loading.value = true;
  try {
    const [employeesData, projectsData, closedPeriodsData, summaryData] = await Promise.all([
      listEmployees(),
      fetchProjects(),
      fetchClosedOvertimes(),
      fetchOvertimesSummary(selectedPeriodId.value),
    ]);

    employees.value = employeesData;
    projects.value = projectsData;
    closedPeriods.value = closedPeriodsData;
    summary.value = summaryData;
  } finally {
    loading.value = false;
  }
}

function shiftPeriod(delta: number): void {
  const period = ReportPeriod.fromPeriodId(selectedPeriodId.value);
  if (delta > 0) {
    period.increment();
  } else {
    period.decrement();
  }
  selectedPeriodId.value = period.periodId();
  fetchData().catch(() => undefined);
}

function incrementPeriod(): void {
  shiftPeriod(1);
}

function decrementPeriod(): void {
  shiftPeriod(-1);
}

function showEmployeeDialog(employee: EmployeeRef): void {
  selectedEmployee.value = employee;
  employeeDialog.value = true;
}

function closeEmployeeDialog(): void {
  employeeDialog.value = false;
  selectedEmployee.value = null;
}

function onRowClick(
  _event: Event,
  payload: { item?: OvertimeRow } | OvertimeRow,
): void {
  const row = (payload as { item?: OvertimeRow }).item ?? (payload as OvertimeRow);
  if (row?.employee) {
    showEmployeeDialog(row.employee);
  }
}

async function exportToExcel(): Promise<void> {
  loading.value = true;
  try {
    await exportOvertimes(selectedPeriodId.value);
    exportCompleted.value = true;
  } finally {
    loading.value = false;
  }
}

async function closePeriod(): Promise<void> {
  loading.value = true;
  try {
    await closeOvertimePeriod(selectedPeriodId.value);
    await fetchData();
  } finally {
    loading.value = false;
  }
}

async function reopenPeriod(): Promise<void> {
  loading.value = true;
  try {
    await reopenOvertimePeriod(selectedPeriodId.value);
    await fetchData();
  } finally {
    loading.value = false;
  }
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
