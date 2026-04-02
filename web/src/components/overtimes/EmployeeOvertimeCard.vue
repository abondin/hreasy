<template>
  <v-card>
    <v-card-item>
      <template #title>
        <div class="d-flex align-center flex-wrap ga-2">
          <span>{{ t("Овертаймы") }}</span>
          <period-switcher-control
            v-if="props.changePeriodAllowed"
            :label="selectedPeriodLabel"
            :is-current="isCurrentPeriod"
            :disabled="loading"
            :period-closed="isPeriodClosed"
            variant="plain"
            @prev="decrementPeriod"
            @next="incrementPeriod"
            @go-current="goToCurrentPeriod"
          />
        </div>
      </template>

      <template #append>
        <div class="d-flex align-center flex-wrap ga-2 justify-end">
          <span>
            {{ t("Всего") }}: {{ t("hours", totalHours) }}
          </span>
          <add-overtime-item-dialog
            :employee-id="props.employeeId"
            :period-id="selectedPeriodId"
            :default-project="defaultProjectId"
            :all-projects="allProjects"
            :period-closed="isPeriodClosed"
            @submitted="onReportSubmitted"
          />
          <approve-overtime-report-dialog
            v-if="canApproveOvertime"
            :employee-id="props.employeeId"
            :period-id="selectedPeriodId"
            :period-closed="isPeriodClosed"
            :previous-decision="myApproval"
            @submitted="onReportSubmitted"
          />
        </div>
      </template>
    </v-card-item>

    <v-card-subtitle v-if="approvals.length > 0">
      <div class="d-flex flex-wrap ga-2">
        <overtime-approval-chip
          v-for="approval in approvals"
          :key="approval.id"
          :approval="approval"
          :report-last-update-time="report.lastUpdate"
        />
      </div>
    </v-card-subtitle>

    <HREasyTableBase
      :headers="headers"
      :items="items"
      height="240"
      fixed-header
      :loading="loading"
      :loading-text="t('Загрузка_данных')"
      :no-data-text="t('Отсутствуют данные')"
      :sort-by="[{ key: 'createdAt', order: 'desc' }]"
      density="compact"
    >
      <template #[`item.date`]="{ item }">
        <v-btn
          icon="mdi-delete"
          variant="text"
          color="secondary"

          @click="openDeleteDialog(item)"
        />
        <span>{{ formatDate(item.date) }}</span>
      </template>

      <template #[`item.projectId`]="{ item }">
        {{ projectName(item.projectId) }}
      </template>

      <template #[`item.createdAt`]="{ item }">
        {{ formatDateTime(item.createdAt) }}
      </template>
    </HREasyTableBase>
  </v-card>

  <v-dialog v-model="deleteDialog" width="520">
    <v-card>
      <v-card-title>{{ t("Удаление") }}</v-card-title>
      <v-card-text>
        {{ t("Вы уверены, что хотите удалить запись?") }}
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn @click="deleteDialog = false">{{ t("Нет") }}</v-btn>
        <v-btn color="primary" :loading="deleting" @click="deleteItem">{{ t("Да") }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { formatDate, formatDateTime } from "@/lib/datetime";
import { useAuthStore } from "@/stores/auth";
import { usePermissions } from "@/lib/permissions";
import { errorUtils } from "@/lib/errors";
import { findEmployee } from "@/services/employee.service";
import { fetchProjects, type SimpleDict } from "@/services/projects.service";
import {
  deleteOvertimeItem,
  fetchClosedOvertimes,
  fetchOvertimeReport,
  ReportPeriod,
  totalOvertimeHours,
  type ApprovalDecision,
  type ClosedOvertimePeriod,
  type OvertimeItem,
  type OvertimeReport,
} from "@/services/overtime.service";
import AddOvertimeItemDialog from "@/components/overtimes/AddOvertimeItemDialog.vue";
import ApproveOvertimeReportDialog from "@/components/overtimes/ApproveOvertimeReportDialog.vue";
import OvertimeApprovalChip from "@/components/overtimes/OvertimeApprovalChip.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import PeriodSwitcherControl from "@/components/shared/PeriodSwitcherControl.vue";

const props = withDefaults(
  defineProps<{
    employeeId: number;
    changePeriodAllowed?: boolean;
    fixedPeriodId?: number | null;
    closedPeriods?: ClosedOvertimePeriod[] | null;
  }>(),
  {
    changePeriodAllowed: true,
    fixedPeriodId: null,
    closedPeriods: null,
  },
);

const emit = defineEmits<{
  updated: [];
}>();

const { t } = useI18n();
const authStore = useAuthStore();
const permissions = usePermissions();

const loading = ref(false);
const deleting = ref(false);
const deleteDialog = ref(false);
const itemToDelete = ref<OvertimeItem | null>(null);

const selectedPeriodId = ref(props.fixedPeriodId ?? ReportPeriod.currentPeriod().periodId());
const selectedPeriod = computed(() => ReportPeriod.fromPeriodId(selectedPeriodId.value));
const currentPeriodId = ReportPeriod.currentPeriod().periodId();
const selectedPeriodLabel = computed(() => selectedPeriod.value.toString());
const isCurrentPeriod = computed(() => selectedPeriodId.value === currentPeriodId);

const allProjects = ref<SimpleDict[]>([]);
const internalClosedPeriods = ref<ClosedOvertimePeriod[]>([]);
const defaultProjectId = ref<number | null>(null);
const report = ref<OvertimeReport>({
  employeeId: props.employeeId,
  period: selectedPeriodId.value,
  items: [],
  approvals: [],
  lastUpdate: null,
});

const headers = computed(() => [
  { title: t("Дата"), key: "date" },
  { title: t("Проект"), key: "projectId" },
  { title: t("Часы"), key: "hours" },
  { title: t("Комментарий"), key: "notes" },
  { title: t("Запись добавлена"), key: "createdAt" },
]);

const items = computed(() => report.value.items);
const approvals = computed(() => report.value.approvals);

const myApproval = computed<ApprovalDecision | null>(() => {
  const currentUserEmployeeId = authStore.employeeId;
  if (!currentUserEmployeeId) {
    return null;
  }
  return (
    approvals.value.find((approval) => approval.approver === currentUserEmployeeId) ??
    null
  );
});

const totalHours = computed(() => totalOvertimeHours(report.value));

const effectiveClosedPeriods = computed(() => {
  if (props.closedPeriods) {
    return props.closedPeriods;
  }
  return internalClosedPeriods.value;
});

const isPeriodClosed = computed(() => {
  return effectiveClosedPeriods.value.some((period) => period.period === selectedPeriodId.value);
});

const canApproveOvertime = computed(() => {
  if (items.value.length === 0) {
    return false;
  }
  return permissions.canApproveOvertimeReport(report.value.employeeId);
});

watch(
  () => props.fixedPeriodId,
  (periodId) => {
    if (periodId != null) {
      selectedPeriodId.value = periodId;
    }
  },
);

watch(
  () => props.employeeId,
  () => {
    loadDefaults().catch(() => undefined);
    loadReport().catch(() => undefined);
  },
);

watch(selectedPeriodId, () => {
  loadReport().catch(() => undefined);
});

onMounted(async () => {
  await loadDefaults();
  await loadReport();
});

async function loadDefaults(): Promise<void> {
  const [projects, employee, closedOvertimes] = await Promise.all([
    fetchProjects(),
    findEmployee(props.employeeId),
    !props.closedPeriods ? fetchClosedOvertimes() : Promise.resolve(null),
  ]);

  allProjects.value = projects;
  defaultProjectId.value = employee.currentProject?.id ?? null;
  if (!props.closedPeriods) {
    internalClosedPeriods.value = closedOvertimes ?? [];
  }
}

async function loadReport(): Promise<void> {
  loading.value = true;
  try {
    const loaded = await fetchOvertimeReport(props.employeeId, selectedPeriodId.value);
    report.value = loaded ?? {
      employeeId: props.employeeId,
      period: selectedPeriodId.value,
      items: [],
      approvals: [],
      lastUpdate: null,
    };
  } catch (error) {
    const message = errorUtils.shortMessage(error);
    report.value = {
      employeeId: props.employeeId,
      period: selectedPeriodId.value,
      items: [],
      approvals: [],
      lastUpdate: null,
    };
    console.error(message);
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
}

function incrementPeriod(): void {
  shiftPeriod(1);
}

function decrementPeriod(): void {
  shiftPeriod(-1);
}

function goToCurrentPeriod(): void {
  selectedPeriodId.value = currentPeriodId;
}

function projectName(projectId?: number): string {
  if (!projectId) {
    return "";
  }
  const project = allProjects.value.find((item) => item.id === projectId);
  if (project) {
    return project.name;
  }
  return t("Неизвестный проект N", { projectId });
}

function onReportSubmitted(updatedReport: OvertimeReport): void {
  report.value = updatedReport;
  emit("updated");
}

function openDeleteDialog(item: OvertimeItem): void {
  itemToDelete.value = item;
  deleteDialog.value = true;
}

async function deleteItem(): Promise<void> {
  if (!itemToDelete.value?.id) {
    return;
  }
  deleting.value = true;
  try {
    const updatedReport = await deleteOvertimeItem(
      report.value.employeeId,
      report.value.period,
      itemToDelete.value.id,
    );
    report.value = updatedReport;
    deleteDialog.value = false;
    itemToDelete.value = null;
    emit("updated");
  } finally {
    deleting.value = false;
  }
}
</script>

