<template>
  <div
    class="mt-4 d-flex flex-column flex-grow-1 min-h-0"
    data-testid="resource-allocations-input-view"
  >
    <TablePageCard test-id="resource-allocations-card">
      <AdaptiveFilterBar
        :items="toolbarFilterItems"
        :has-right-actions="true"
        class="mb-4"
      >
        <template #left-actions>
          <div class="d-flex align-center ga-2">
            <TableToolbarActions
              show-refresh
              :disabled="loading || saving"
              :refresh-label="t('Обновить данные')"
              @refresh="reload"
            />
            <PeriodSwitcherControl
              :label="String(inputYear)"
              :is-current="inputYear === currentYear"
              :disabled="loading || saving"
              label-test-id="resource-allocations-year"
              @prev="changeYear(-1)"
              @next="changeYear(1)"
              @go-current="goToCurrentYear"
            />
          </div>
        </template>

        <template #filter-project>
          <v-autocomplete
            :model-value="inputProjectId"
            :items="managedProjects"
            item-title="name"
            item-value="id"
            hide-details
            density="compact"
            variant="outlined"
            :disabled="loading || saving"
            :label="t('Проект')"
            data-testid="resource-allocations-input-project"
            @update:model-value="changeInputProject"
          />
        </template>

        <template #right-actions>
          <v-btn
            variant="text"
            prepend-icon="mdi-undo-variant"
            :disabled="!hasPendingChanges || loading || saving"
            data-testid="resource-allocations-cancel"
            @click="discardChanges"
          >
            {{ t("Отменить изменения") }}
          </v-btn>
          <v-btn
            color="primary"
            prepend-icon="mdi-content-save"
            :disabled="!hasPendingChanges || loading"
            :loading="saving"
            data-testid="resource-allocations-save"
            @click="save"
          >
            {{ t("Сохранить") }}
          </v-btn>
        </template>
      </AdaptiveFilterBar>

      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        class="mb-3 flex-grow-0"
        closable
        @click:close="error = ''"
      >
        {{ error }}
      </v-alert>
      <v-alert
        v-if="warning"
        type="warning"
        variant="tonal"
        class="mb-3 flex-grow-0"
        :title="t('Не все изменения сохранены')"
        closable
        data-testid="resource-allocations-conflict"
        @click:close="warning = ''"
      >
        {{ warning }}
      </v-alert>

      <template #table>
        <div class="h-100 min-h-0" data-testid="resource-allocations-input-table">
          <template v-if="inputSheet && inputProject">
            <Grid
              ref="inputGrid"
              :columns="inputGridColumns"
              :source="inputGridRows"
              :additional-data="inputGridAdditionalData"
              :readonly="saving || !inputProject.editable"
              :range="true"
              :use-clipboard="true"
              :apply-on-close="true"
              :resize="true"
              :hide-attribution="true"
              theme="compact"
              class="h-100 w-100"
              data-testid="resource-allocations-input-grid"
              @beforeedit="handleInputBeforeEdit"
              @afteredit="handleInputAfterEdit"
            />
          </template>
          <v-empty-state
            v-else
            icon="mdi-briefcase-outline"
            :title="t('Нет доступных проектов')"
          />
        </div>
      </template>
    </TablePageCard>

    <ConfirmDeleteDialog
      :open="discardDialog"
      :title="t('Несохранённые изменения')"
      :message="t('Если продолжить, внесённые изменения будут потеряны.')"
      :cancel-label="t('Отмена')"
      :confirm-label="t('Продолжить')"
      data-testid="resource-allocations-discard-dialog"
      @close="cancelDiscard"
      @confirm="confirmDiscard"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onActivated, onMounted, ref, shallowRef, type ComponentPublicInstance } from "vue";
import { useI18n } from "vue-i18n";
import { onBeforeRouteLeave } from "vue-router";
import Grid, { VGridVueTemplate } from "@revolist/vue3-datagrid";
import type { AfterEditEvent, BeforeSaveDataDetails, ColumnRegular } from "@revolist/revogrid";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import PeriodSwitcherControl from "@/components/shared/PeriodSwitcherControl.vue";
import ConfirmDeleteDialog from "@/components/shared/ConfirmDeleteDialog.vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import ResourceAllocationEmployeeCell from "@/views/allocations/ResourceAllocationEmployeeCell.vue";
import { BusinessError, errorUtils } from "@/lib/errors";
import { ReportPeriod } from "@/services/overtime.service";
import {
  fetchResourceAllocationProjectInput,
  saveResourceAllocations,
  type ResourceAllocationChange,
  type ResourceAllocationProjectInput,
} from "@/services/resource-allocation.service";

defineOptions({ name: "ResourceAllocationInputView" });

interface InputGridRow {
  id: number;
  employee: string;
  dateOfEmployment: string;
  dateOfDismissal: string;
  dismissedLabel?: string;
  otherProject?: string;
  addEmployee?: boolean;
  employeesAvailableToAdd?: ResourceAllocationProjectInput["employees"];
  conflictingPeriods?: Set<number>;
  [key: string]: unknown;
}

const { t } = useI18n();
const currentPeriodId = ReportPeriod.currentPeriod().id;
const currentYear = Math.trunc(currentPeriodId / 100);
const inputYear = ref(currentYear);
const inputSheet = shallowRef<ResourceAllocationProjectInput | null>(null);
const inputEdits = ref(new Map<string, number>());
const inputInitialValues = ref(new Map<string, number>());
const inputInitialRevisionIds = ref(new Map<string, number>());
const inputProjectId = ref<number | null>(null);
const addedInputEmployeeIds = ref(new Set<number>());
const loading = ref(false);
const saving = ref(false);
const error = ref("");
const warning = ref("");
const conflictCellKeys = ref(new Set<string>());
const inputGrid = ref<ComponentPublicInstance | null>(null);
const discardDialog = ref(false);
let pendingDiscardAction: (() => void) | null = null;
let resolveRouteLeave: ((allow: boolean) => void) | null = null;
let activated = false;
const employeeCellTemplate = VGridVueTemplate(ResourceAllocationEmployeeCell);
const inputGridAdditionalData = { addEmployee: addInputEmployee };

const toolbarFilterItems = [{ id: "project", minWidth: 360 }];
const hasPendingChanges = computed(() => inputEdits.value.size > 0);
const managedProjects = computed(() => inputSheet.value?.projects ?? []);
const inputProject = computed(
  () =>
    managedProjects.value.find(
      (project) => project.id === inputProjectId.value,
    ) ?? null,
);
const inputEmployeeIds = computed(() => {
  const ids = new Set(addedInputEmployeeIds.value);
  for (const employee of inputSheet.value?.employees ?? []) {
    if (employee.currentProjectId === inputProjectId.value) {
      ids.add(employee.id);
    }
  }
  for (const allocation of inputSheet.value?.allocations ?? []) {
    ids.add(allocation.employeeId);
  }
  return ids;
});
const inputEmployees = computed(() =>
  (inputSheet.value?.employees ?? [])
    .filter((employee) => inputEmployeeIds.value.has(employee.id))
    .sort((left, right) => left.displayName.localeCompare(right.displayName)),
);
const employeesAvailableToAdd = computed(() =>
  (inputSheet.value?.employees ?? [])
    .filter((employee) => !inputEmployeeIds.value.has(employee.id))
    .sort((left, right) => left.displayName.localeCompare(right.displayName)),
);
const inputGridRows = computed<InputGridRow[]>(() => {
  const conflicts = conflictCellKeys.value;
  const rows = inputEmployees.value.map((employee) => {
    const row: InputGridRow = {
      id: employee.id,
      employee: employee.displayName,
      dateOfEmployment: employee.dateOfEmployment ?? "",
      dateOfDismissal: employee.dateOfDismissal ?? "",
      dismissedLabel: employee.dismissed
        ? `${t("Уволен")} ${formatDate(employee.dateOfDismissal ?? "")}`
        : undefined,
      otherProject:
        !employee.dismissed &&
        employee.currentProjectName &&
        employee.currentProjectId !== inputProjectId.value
          ? employee.currentProjectName
          : undefined,
      conflictingPeriods: new Set(
        (inputSheet.value?.months ?? [])
          .map((month) => month.period)
          .filter((period) =>
            conflicts.has(inputCellKey(period, employee.id)),
          ),
      ),
    };
    for (const month of inputSheet.value?.months ?? []) {
      row[inputMonthProp(month.period)] =
        inputCellValue(month.period, employee.id) || "";
    }
    return row;
  });
  if (employeesAvailableToAdd.value.length) {
    rows.push({
      id: -1,
      employee: "",
      dateOfEmployment: "",
      dateOfDismissal: "",
      addEmployee: true,
      employeesAvailableToAdd: employeesAvailableToAdd.value,
    });
  }
  return rows;
});
const inputGridColumns = computed<ColumnRegular[]>(() => [
  {
    name: t("Сотрудник"),
    prop: "employee",
    size: 600,
    pin: "colPinStart",
    readonly: true,
    cellTemplate: employeeCellTemplate,
    cellProperties: ({ model }) => ({
      "data-testid": `resource-allocation-input-employee-${(model as InputGridRow).id}`,
    }),
  },
  ...(inputSheet.value?.months ?? []).map(
    (month) =>
      ({
        name: `${month.closed ? "🔒 " : ""}${formatMonth(month.period)}`,
        prop: inputMonthProp(month.period),
        size: 110,
        minSize: 110,
        maxSize: 110,
        sortable: false,
        readonly: ({ model: sourceModel }) =>
          month.closed ||
          !inputProject.value?.editable ||
          !employeeMonthEditable(sourceModel as InputGridRow, month.period),
        cellProperties: ({ model: sourceModel }) => {
          const model = sourceModel as InputGridRow;
          const conflict = model.conflictingPeriods?.has(month.period);
          return {
            "data-testid": `resource-allocation-input-${model.id}-${month.period}`,
            "data-conflict": conflict || undefined,
            style: conflict
              ? {
                  backgroundColor: "rgba(var(--v-theme-error), 0.16)",
                  boxShadow: "inset 0 0 0 1px rgb(var(--v-theme-error))",
                }
              : undefined,
          };
        },
      }) satisfies ColumnRegular,
  ),
]);
onMounted(() => {
  void load();
});
onActivated(() => {
  if (activated) {
    reload();
  }
  activated = true;
});
onBeforeRouteLeave(() => {
  if (!hasPendingChanges.value) {
    return true;
  }
  discardDialog.value = true;
  return new Promise<boolean>((resolve) => {
    resolveRouteLeave = resolve;
  });
});

async function load(): Promise<void> {
  warning.value = "";
  conflictCellKeys.value = new Set();
  await loadView(true);
}

async function save(): Promise<void> {
  if (inputProjectId.value == null) {
    return;
  }
  await nextTick();
  saving.value = true;
  error.value = "";
  try {
    await saveResourceAllocations(
      inputYear.value,
      inputProjectId.value,
      [...inputEdits.value].map(([key, percent]) => {
        const [period, employeeId] = key.split(":").map(Number);
        return {
          period,
          employeeId,
          percent,
          expectedRevisionId: inputInitialRevisionIds.value.get(key) ?? null,
        };
      }),
    );
    resetLoadedData();
    await load();
  } catch (saveError) {
    if (
      saveError instanceof BusinessError &&
      saveError.code === "errors.resource_allocation.conflict"
    ) {
      applyConflict(saveError);
    } else {
      error.value = errorUtils.shortMessage(saveError);
    }
  } finally {
    saving.value = false;
  }
}

function applyConflict(conflictError: BusinessError): void {
  const allocations = Array.isArray(conflictError.attrs?.allocations)
    ? conflictError.attrs.allocations.filter(isInputAllocationValue)
    : null;
  const changes = Array.isArray(conflictError.attrs?.changes)
    ? conflictError.attrs.changes.filter(isAllocationChange)
    : null;
  const conflicts = Array.isArray(conflictError.attrs?.conflicts)
    ? conflictError.attrs.conflicts.filter(isConflictCell)
    : null;
  if (!inputSheet.value || !allocations || !changes || !conflicts) {
    error.value = conflictError.message;
    return;
  }
  inputInitialValues.value = new Map(
    allocations.map((value) => [
      inputCellKey(value.period, value.employeeId),
      value.percent,
    ]),
  );
  inputInitialRevisionIds.value = new Map(
    allocations.map((value) => [
      inputCellKey(value.period, value.employeeId),
      value.revisionId,
    ]),
  );
  inputSheet.value = { ...inputSheet.value, allocations };
  inputEdits.value = new Map(
    changes.map((change) => [
      inputCellKey(change.period, change.employeeId),
      change.percent,
    ]),
  );
  conflictCellKeys.value = new Set(
    conflicts.map((conflict) =>
      inputCellKey(conflict.period, conflict.employeeId),
    ),
  );
  warning.value = t(
    "Данные в выделенных ячейках уже изменил другой пользователь. Загружены актуальные значения. Проверьте красные ячейки и при необходимости измените их повторно.",
  );
}

function isConflictCell(
  value: unknown,
): value is { period: number; employeeId: number } {
  return (
    !!value &&
    typeof value === "object" &&
    "period" in value &&
    typeof value.period === "number" &&
    "employeeId" in value &&
    typeof value.employeeId === "number"
  );
}

function isInputAllocationValue(
  value: unknown,
): value is ResourceAllocationProjectInput["allocations"][number] {
  return (
    !!value &&
    typeof value === "object" &&
    "period" in value &&
    typeof value.period === "number" &&
    "employeeId" in value &&
    typeof value.employeeId === "number" &&
    "percent" in value &&
    typeof value.percent === "number" &&
    "revisionId" in value &&
    typeof value.revisionId === "number"
  );
}

function isAllocationChange(value: unknown): value is ResourceAllocationChange {
  return (
    !!value &&
    typeof value === "object" &&
    "period" in value &&
    typeof value.period === "number" &&
    "employeeId" in value &&
    typeof value.employeeId === "number" &&
    "percent" in value &&
    typeof value.percent === "number" &&
    "expectedRevisionId" in value &&
    (value.expectedRevisionId === null ||
      typeof value.expectedRevisionId === "number")
  );
}

function inputCellKey(period: number, employeeId: number): string {
  return `${period}:${employeeId}`;
}

function inputCellValue(period: number, employeeId: number): number {
  const key = inputCellKey(period, employeeId);
  return inputEdits.value.get(key) ?? inputInitialValues.value.get(key) ?? 0;
}

function inputMonthProp(period: number): string {
  return `month_${period}`;
}

function periodFromInputProp(prop: string | number): number | null {
  const match = /^month_(\d+)$/.exec(String(prop));
  return match ? Number(match[1]) : null;
}

function formatMonth(period: number): string {
  return ReportPeriod.fromPeriodId(period)
    .toString()
    .replace(/\s+\d{4}$/, "");
}

function formatDate(value: string): string {
  return value
    ? new Intl.DateTimeFormat("ru-RU").format(new Date(`${value}T00:00:00`))
    : "";
}

function normalizePercent(value: string): number {
  return Math.min(1000, Math.max(0, Math.round(Number(value) || 0)));
}

function updateInputCell(
  period: number,
  employeeId: number,
  percent: number,
): void {
  const key = inputCellKey(period, employeeId);
  if (conflictCellKeys.value.has(key)) {
    const conflicts = new Set(conflictCellKeys.value);
    conflicts.delete(key);
    conflictCellKeys.value = conflicts;
    if (conflicts.size === 0) {
      warning.value = "";
    }
  }
  if (percent === (inputInitialValues.value.get(key) ?? 0)) {
    inputEdits.value.delete(key);
  } else {
    inputEdits.value.set(key, percent);
  }
}

function handleInputBeforeEdit(
  event: CustomEvent<BeforeSaveDataDetails>,
): void {
  if (periodFromInputProp(event.detail.prop) != null) {
    event.detail.val = normalizePercent(String(event.detail.val ?? ""));
  }
}

function handleInputAfterEdit(event: CustomEvent<AfterEditEvent>): void {
  const detail = event.detail;
  if ("prop" in detail) {
    const model = detail.model as InputGridRow;
    const period = periodFromInputProp(detail.prop);
    if (period != null && employeeMonthEditable(model, period)) {
      updateInputCell(
        period,
        model.id,
        normalizePercent(String(detail.val ?? model[detail.prop])),
      );
    }
    return;
  }
  for (const [rowIndex, changedModel] of Object.entries(detail.data)) {
    const model = detail.models[Number(rowIndex)] as InputGridRow | undefined;
    if (!model) {
      continue;
    }
    for (const [prop, value] of Object.entries(changedModel)) {
      const period = periodFromInputProp(prop);
      const month = inputSheet.value?.months.find(
        (item) => item.period === period,
      );
      if (
        period != null &&
        !month?.closed &&
        employeeMonthEditable(model, period)
      ) {
        updateInputCell(
          period,
          model.id,
          normalizePercent(String(value ?? "")),
        );
      }
    }
  }
}

function employeeMonthEditable(
  employee: InputGridRow,
  period: number,
): boolean {
  if (employee.addEmployee) {
    return false;
  }
  const { start, end } = periodBounds(period);
  return (
    (!employee.dateOfEmployment || employee.dateOfEmployment <= end) &&
    (!employee.dateOfDismissal || employee.dateOfDismissal >= start)
  );
}

function periodBounds(period: number): { start: string; end: string } {
  const year = Math.trunc(period / 100);
  const month = period % 100;
  const monthNumber = String(month + 1).padStart(2, "0");
  const lastDay = new Date(Date.UTC(year, month + 1, 0)).getUTCDate();
  return {
    start: `${year}-${monthNumber}-01`,
    end: `${year}-${monthNumber}-${String(lastDay).padStart(2, "0")}`,
  };
}

function addInputEmployee(employeeId: number | null): void {
  if (employeeId == null) {
    return;
  }
  addedInputEmployeeIds.value = new Set([
    ...addedInputEmployeeIds.value,
    employeeId,
  ]);
}

function changeInputProject(projectId: number | null): void {
  if (projectId == null || projectId === inputProjectId.value) {
    return;
  }
  runAfterDiscard(() => {
    inputProjectId.value = projectId;
    inputSheet.value = null;
    void loadView(true);
  });
}

async function loadView(clearDraft: boolean): Promise<void> {
  loading.value = true;
  error.value = "";
  try {
    const response = await fetchResourceAllocationProjectInput(
      inputYear.value,
      inputProjectId.value ?? undefined,
    );
    rememberInputBaseline(response);
    inputSheet.value = response;
    inputProjectId.value = response.selectedProjectId;
    await nextTick();
    await (
      inputGrid.value?.$el as HTMLRevoGridElement | undefined
    )?.scrollToRow?.(0);
    if (clearDraft) {
      inputEdits.value.clear();
    }
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

function rememberInputBaseline(response: ResourceAllocationProjectInput): void {
  inputInitialValues.value = new Map(
    response.allocations.map((value) => [
      inputCellKey(value.period, value.employeeId),
      value.percent,
    ]),
  );
  inputInitialRevisionIds.value = new Map(
    response.allocations.map((value) => [
      inputCellKey(value.period, value.employeeId),
      value.revisionId,
    ]),
  );
}

function reload(): void {
  runAfterDiscard(() => void load());
}

function discardChanges(): void {
  runAfterDiscard(() => undefined);
}

function changeYear(delta: number): void {
  runAfterDiscard(() => {
    inputYear.value += delta;
    inputProjectId.value = null;
    resetLoadedData();
    void load();
  });
}

function goToCurrentYear(): void {
  runAfterDiscard(() => {
    inputYear.value = currentYear;
    inputProjectId.value = null;
    resetLoadedData();
    void load();
  });
}

function resetLoadedData(): void {
  inputSheet.value = null;
  inputInitialValues.value = new Map();
  inputInitialRevisionIds.value = new Map();
  addedInputEmployeeIds.value = new Set();
  conflictCellKeys.value = new Set();
  warning.value = "";
}

function runAfterDiscard(action: () => void): void {
  if (!hasPendingChanges.value) {
    action();
    return;
  }
  pendingDiscardAction = action;
  discardDialog.value = true;
}

function cancelDiscard(): void {
  discardDialog.value = false;
  pendingDiscardAction = null;
  resolveRouteLeave?.(false);
  resolveRouteLeave = null;
}

function confirmDiscard(): void {
  discardDialog.value = false;
  inputEdits.value.clear();
  addedInputEmployeeIds.value = new Set();
  const action = pendingDiscardAction;
  pendingDiscardAction = null;
  resolveRouteLeave?.(true);
  resolveRouteLeave = null;
  action?.();
}
</script>
