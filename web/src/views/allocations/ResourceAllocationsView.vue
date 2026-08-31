<template>
  <TableFirstPageLayout test-id="resource-allocations-view" @activated="reload">
    <TablePageCard test-id="resource-allocations-card">
      <div class="d-flex flex-wrap align-center ga-3 mb-3">
        <v-btn icon="mdi-refresh" variant="text" :loading="loading" :aria-label="t('Обновить данные')" @click="reload" />
        <PeriodSwitcherControl
          :label="periodLabel"
          :is-current="periodId === currentPeriodId"
          :disabled="loading || saving"
          label-test-id="resource-allocations-period"
          @prev="changePeriod(-1)"
          @next="changePeriod(1)"
          @go-current="goToCurrentPeriod"
        />
        <v-spacer />
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
      </div>

      <div class="d-flex flex-wrap align-stretch ga-3 mb-4">
        <v-sheet border rounded class="resource-allocation-filter-group pa-2 d-flex flex-wrap align-center ga-2">
          <span class="font-weight-medium">{{ t("Сотрудники") }}</span>
          <v-text-field
            :model-value="search"
            @update:model-value="search = normalizeSearchInput($event)"
            density="compact"
            clearable
            hide-details
            variant="outlined"
            prepend-inner-icon="mdi-magnify"
            :label="t('Поиск по ФИО')"
            class="resource-allocation-search"
            data-testid="resource-allocations-employee-search"
          />
          <v-btn-toggle v-model="employeeScope" mandatory density="compact" color="primary">
            <v-btn value="all" data-testid="resource-allocations-employees-all">{{ t("Все") }}</v-btn>
            <v-btn value="mine" data-testid="resource-allocations-employees-mine">{{ t("С моих проектов") }}</v-btn>
          </v-btn-toggle>
          <v-select
            v-model="employeeBaIds"
            :items="businessAccounts"
            item-title="name"
            item-value="id"
            multiple
            chips
            clearable
            hide-details
            density="compact"
            variant="outlined"
            :label="t('БА сотрудников')"
            class="resource-allocation-ba-filter"
            data-testid="resource-allocations-employee-ba"
          />
        </v-sheet>

        <v-sheet border rounded class="resource-allocation-filter-group pa-2 d-flex flex-wrap align-center ga-2">
          <span class="font-weight-medium">{{ t("Проекты") }}</span>
          <v-text-field
            :model-value="projectSearch"
            @update:model-value="projectSearch = normalizeSearchInput($event)"
            density="compact"
            clearable
            hide-details
            variant="outlined"
            prepend-inner-icon="mdi-magnify"
            :label="t('Поиск по проектам')"
            class="resource-allocation-search"
            data-testid="resource-allocations-project-search"
          />
          <v-btn-toggle v-model="projectScope" mandatory density="compact" color="primary">
            <v-btn value="mine" data-testid="resource-allocations-scope-mine">{{ t("Мои") }}</v-btn>
            <v-btn value="all" data-testid="resource-allocations-scope-all">{{ t("Все") }}</v-btn>
          </v-btn-toggle>
          <v-select
            v-model="projectBaIds"
            :items="businessAccounts"
            item-title="name"
            item-value="id"
            multiple
            chips
            clearable
            hide-details
            density="compact"
            variant="outlined"
            :label="t('БА проектов')"
            class="resource-allocation-ba-filter"
            data-testid="resource-allocations-project-ba"
          />
          <v-checkbox
            v-model="onlyAllocatedCells"
            hide-details
            density="compact"
            :label="t('Только с аллокациями')"
            data-testid="resource-allocations-only-allocated"
          />
        </v-sheet>
      </div>

      <v-alert v-if="error" type="error" variant="tonal" class="mb-3" closable @click:close="error = ''">
        {{ error }}
      </v-alert>
      <v-alert v-if="saved" type="success" variant="tonal" class="mb-3" closable @click:close="saved = false">
        {{ t("Изменения сохранены") }}
      </v-alert>

      <template #table>
        <div
          v-if="onlyAllocatedCells && (rows.length === 0 || visibleProjects.length === 0)"
          class="d-flex align-center justify-center h-100"
          data-testid="resource-allocations-empty-allocated"
        >
          <v-empty-state
            icon="mdi-chart-box-outline"
            :title="t('Нет аллокаций по выбранным фильтрам')"
            :text="t('Отключите фильтр «Только с аллокациями», чтобы добавить значения')"
          />
        </div>
        <HREasyTableBase
          v-else
          :headers="headers"
          :items="rows"
          :loading="loading"
          :loading-text="t('Загрузка_данных')"
          :no-data-text="t('Отсутствуют данные')"
          fixed-header
          height="fill"
          density="compact"
          item-key="id"
          table-class="resource-allocation-table"
          :table-width="tableWidth"
          data-testid="resource-allocations-table"
        >
          <template #[`item.total`]="{ item }">
            <v-chip :color="totalColor(item.total)" size="small" variant="tonal">
              {{ item.total }}%
            </v-chip>
          </template>
          <template #[`item.employee`]="{ item }">
            <div class="resource-allocation-employee-cell d-flex align-center ga-2 min-width-0">
              <span class="text-truncate">{{ item.employee }}</span>
              <div class="resource-allocation-row-action d-inline-flex align-center justify-center flex-shrink-0">
                <v-btn
                  v-if="canClearEmployee(item.id)"
                  icon="mdi-delete"
                  size="x-small"
                  variant="text"
                  color="error"
                  class="resource-allocation-row-delete"
                  :disabled="saving"
                  :aria-label="t('Очистить аллокации сотрудника')"
                  :title="t('Очистить аллокации сотрудника')"
                  :data-testid="`resource-allocation-clear-row-${item.id}`"
                  @click.stop="clearEmployee(item)"
                />
              </div>
            </div>
          </template>
          <template
            v-for="project in visibleProjects"
            :key="project.id"
            #[`item.project_${project.id}`]="{ item }"
          >
            <div
              v-if="!onlyAllocatedCells || cellValue(item.id, project.id) > 0"
              class="resource-allocation-cell"
            >
              <input
                v-if="isActiveCell(item.id, project.id)"
                :ref="setActiveInput"
                v-model="activeCellValue"
                class="resource-allocation-input"
                type="number"
                min="0"
                max="1000"
                step="1"
                :aria-label="`${item.employee} — ${project.name}`"
                :data-testid="`resource-allocation-cell-${item.id}-${project.id}`"
                @blur="commitActiveCell(item.id, project.id)"
                @keydown="handleCellKeydown(item.id, project.id, $event)"
              />
              <button
                v-else
                class="resource-allocation-value"
                type="button"
                :disabled="!project.editable || saving"
                :aria-label="`${item.employee} — ${project.name}`"
                :data-testid="`resource-allocation-cell-${item.id}-${project.id}`"
                @click="activateCell(item.id, project.id)"
              >
                {{ cellValue(item.id, project.id) || "" }}
              </button>
              <div class="resource-allocation-cell-action d-inline-flex align-center justify-center flex-shrink-0">
                <button
                  v-if="project.editable && cellValue(item.id, project.id)"
                  class="resource-allocation-cell-clear"
                  type="button"
                  :disabled="saving"
                  :aria-label="t('Очистить ячейку')"
                  :title="t('Очистить ячейку')"
                  :data-testid="`resource-allocation-clear-cell-${item.id}-${project.id}`"
                  @click="updateCell(item.id, project.id, 0)"
                >
                  <span class="mdi mdi-close" aria-hidden="true" />
                </button>
              </div>
            </div>
          </template>
        </HREasyTableBase>
      </template>
    </TablePageCard>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, shallowRef, type ComponentPublicInstance } from "vue";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import PeriodSwitcherControl from "@/components/shared/PeriodSwitcherControl.vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";
import { errorUtils } from "@/lib/errors";
import { normalizeSearchInput } from "@/lib/search";
import { ReportPeriod } from "@/services/overtime.service";
import {
  fetchResourceAllocations,
  saveResourceAllocations,
  type ResourceAllocationSheet,
} from "@/services/resource-allocation.service";

defineOptions({ name: "ResourceAllocationsView" });

interface AllocationRow {
  id: number;
  employee: string;
  total: number;
}

interface BusinessAccountOption {
  id: number;
  name: string;
}

const { t } = useI18n();
const currentPeriodId = ReportPeriod.currentPeriod().id;
const periodId = ref(currentPeriodId);
const sheet = shallowRef<ResourceAllocationSheet | null>(null);
const edits = ref(new Map<string, number>());
const search = ref("");
const projectSearch = ref("");
const employeeScope = ref<"all" | "mine">("all");
const employeeBaIds = ref<number[]>([]);
const projectScope = ref<"mine" | "all">("mine");
const projectBaIds = ref<number[]>([]);
const onlyAllocatedCells = ref(false);
const loading = ref(false);
const saving = ref(false);
const saved = ref(false);
const error = ref("");
const activeCell = ref<string | null>(null);
const activeCellValue = ref("");
const activeInput = ref<HTMLInputElement | null>(null);

const periodLabel = computed(() => ReportPeriod.fromPeriodId(periodId.value).toString());
const normalizedEmployeeSearch = computed(() => search.value.toLocaleLowerCase());
const normalizedProjectSearch = computed(() => projectSearch.value.toLocaleLowerCase());
const initialValues = computed(() => new Map(
  (sheet.value?.allocations ?? []).map(value => [cellKey(value.employeeId, value.projectId), value.percent]),
));
const hasPendingChanges = computed(() => edits.value.size > 0
  || (activeCell.value != null
    && normalizePercent(activeCellValue.value) !== cellValueByKey(activeCell.value)));
const projectsById = computed(() => new Map(
  (sheet.value?.projects ?? []).map(project => [project.id, project]),
));
const allocationStats = computed(() => {
  const totalsByEmployee = new Map<number, number>();
  const projectIdsByEmployee = new Map<number, Set<number>>();
  const allocatedProjectIds = new Set<number>();
  const clearableEmployeeIds = new Set<number>();

  const add = (employeeId: number, projectId: number, percent: number) => {
    if (percent <= 0) {
      return;
    }
    totalsByEmployee.set(employeeId, (totalsByEmployee.get(employeeId) ?? 0) + percent);
    const employeeProjectIds = projectIdsByEmployee.get(employeeId) ?? new Set<number>();
    employeeProjectIds.add(projectId);
    projectIdsByEmployee.set(employeeId, employeeProjectIds);
    allocatedProjectIds.add(projectId);
    if (projectsById.value.get(projectId)?.editable) {
      clearableEmployeeIds.add(employeeId);
    }
  };

  for (const value of sheet.value?.allocations ?? []) {
    if (!edits.value.has(cellKey(value.employeeId, value.projectId))) {
      add(value.employeeId, value.projectId, value.percent);
    }
  }
  for (const [key, percent] of edits.value) {
    const [employeeId, projectId] = key.split(":").map(Number);
    add(employeeId, projectId, percent);
  }
  return { totalsByEmployee, projectIdsByEmployee, allocatedProjectIds, clearableEmployeeIds };
});
const businessAccounts = computed<BusinessAccountOption[]>(() => [
  ...new Map((sheet.value?.projects ?? [])
    .filter(project => project.baId != null)
    .map(project => [project.baId as number, {
      id: project.baId as number,
      name: project.baName ?? String(project.baId),
    }])).values(),
].sort((left, right) => left.name.localeCompare(right.name)));
const visibleProjects = computed(() => (sheet.value?.projects ?? [])
  .filter(project => !normalizedProjectSearch.value
    || project.name.toLocaleLowerCase().includes(normalizedProjectSearch.value))
  .filter(project => projectScope.value === "all" || project.editable)
  .filter(project => projectBaIds.value.length === 0
    || (project.baId != null && projectBaIds.value.includes(project.baId)))
  .filter(project => !onlyAllocatedCells.value
    || allocationStats.value.allocatedProjectIds.has(project.id))
  .sort((left, right) => Number(right.editable) - Number(left.editable)
    || Number(right.active) - Number(left.active)
    || left.name.localeCompare(right.name)));
const visibleProjectIds = computed(() => new Set(visibleProjects.value.map(project => project.id)));
const employeeColumnDivider = {
  style: { borderRight: "1px solid rgba(var(--v-border-color), var(--v-border-opacity))" },
};
const headers = computed(() => [
  {
    title: t("Сотрудник"),
    key: "employee",
    width: 320,
    minWidth: 320,
    maxWidth: 320,
    fixed: true,
    headerProps: employeeColumnDivider,
    cellProps: employeeColumnDivider,
  },
  { title: t("Итого"), key: "total", width: 90, minWidth: 90, maxWidth: 90, fixed: true },
  ...visibleProjects.value.map(project => ({
    title: project.name,
    key: `project_${project.id}`,
    width: 130,
    minWidth: 130,
    maxWidth: 130,
    sortable: false,
  })),
]);
const tableWidth = computed(() => headers.value.reduce((sum, header) => sum + Number(header.width), 0));
const rows = computed<AllocationRow[]>(() => (sheet.value?.employees ?? [])
  .filter(employee => !normalizedEmployeeSearch.value
    || employee.displayName.toLocaleLowerCase().includes(normalizedEmployeeSearch.value))
  .filter(employee => employeeScope.value === "all"
    || (employee.currentProjectId != null && projectsById.value.get(employee.currentProjectId)?.editable))
  .filter((employee) => {
    if (employeeBaIds.value.length === 0) {
      return true;
    }
    const baId = employee.currentProjectId == null
      ? null
      : projectsById.value.get(employee.currentProjectId)?.baId;
    return baId != null && employeeBaIds.value.includes(baId);
  })
  .filter(employee => !onlyAllocatedCells.value
    || [...(allocationStats.value.projectIdsByEmployee.get(employee.id) ?? [])]
      .some(projectId => visibleProjectIds.value.has(projectId)))
  .map(employee => ({
    id: employee.id,
    employee: employee.displayName,
    total: allocationStats.value.totalsByEmployee.get(employee.id) ?? 0,
  })));

onMounted(load);

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  saved.value = false;
  try {
    sheet.value = await fetchResourceAllocations(periodId.value);
    edits.value.clear();
    activeCell.value = null;
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

async function save(): Promise<void> {
  saving.value = true;
  error.value = "";
  try {
    await saveResourceAllocations(periodId.value, [...edits.value].map(([key, percent]) => {
      const [employeeId, projectId] = key.split(":").map(Number);
      return { employeeId, projectId, percent };
    }));
    await load();
    saved.value = true;
  } catch (saveError) {
    error.value = errorUtils.shortMessage(saveError);
  } finally {
    saving.value = false;
  }
}

function cellKey(employeeId: number, projectId: number): string {
  return `${employeeId}:${projectId}`;
}

function cellValue(employeeId: number, projectId: number): number {
  return cellValueByKey(cellKey(employeeId, projectId));
}

function cellValueByKey(key: string): number {
  return edits.value.get(key) ?? initialValues.value.get(key) ?? 0;
}

function isActiveCell(employeeId: number, projectId: number): boolean {
  return activeCell.value === cellKey(employeeId, projectId);
}

function setActiveInput(element: Element | ComponentPublicInstance | null): void {
  activeInput.value = element instanceof HTMLInputElement ? element : null;
}

async function activateCell(employeeId: number, projectId: number): Promise<void> {
  if (!projectsById.value.get(projectId)?.editable || saving.value) {
    return;
  }
  activeCell.value = cellKey(employeeId, projectId);
  const value = cellValue(employeeId, projectId);
  activeCellValue.value = value ? String(value) : "";
  await nextTick();
  activeInput.value?.focus();
  activeInput.value?.select();
}

function commitActiveCell(employeeId: number, projectId: number): void {
  if (!isActiveCell(employeeId, projectId)) {
    return;
  }
  updateCell(employeeId, projectId, normalizePercent(activeCellValue.value));
  activeCell.value = null;
}

function handleCellKeydown(employeeId: number, projectId: number, event: KeyboardEvent): void {
  if (event.key !== "Tab" && event.key !== "Enter") {
    return;
  }
  event.preventDefault();
  moveActiveCell(
    employeeId,
    projectId,
    event.key === "Enter" ? (event.shiftKey ? -1 : 1) : 0,
    event.key === "Tab" ? (event.shiftKey ? -1 : 1) : 0,
  );
}

function moveActiveCell(employeeId: number, projectId: number, rowDelta: number, projectDelta: number): void {
  const editableProjects = visibleProjects.value.filter(project => project.editable);
  const rowIndex = rows.value.findIndex(row => row.id === employeeId);
  const projectIndex = editableProjects.findIndex(project => project.id === projectId);
  const targetRow = rows.value[rowIndex + rowDelta];
  const targetProject = editableProjects[projectIndex + projectDelta];
  commitActiveCell(employeeId, projectId);
  if (targetRow && targetProject) {
    void activateCell(targetRow.id, targetProject.id);
  }
}

function normalizePercent(value: string): number {
  return Math.min(1000, Math.max(0, Math.round(Number(value) || 0)));
}

function updateCell(employeeId: number, projectId: number, percent: number): void {
  const key = cellKey(employeeId, projectId);
  if (percent === (initialValues.value.get(key) ?? 0)) {
    edits.value.delete(key);
  } else {
    edits.value.set(key, percent);
  }
}

function canClearEmployee(employeeId: number): boolean {
  return allocationStats.value.clearableEmployeeIds.has(employeeId);
}

function clearEmployee(employee: AllocationRow): void {
  if (!window.confirm(t("Очистить все доступные аллокации сотрудника {employee}?", { employee: employee.employee }))) {
    return;
  }
  for (const project of sheet.value?.projects ?? []) {
    if (project.editable) {
      updateCell(employee.id, project.id, 0);
    }
  }
}

function totalColor(total: number): string {
  return total === 100 ? "success" : total > 100 ? "error" : "warning";
}

function reload(): void {
  if (discardEdits()) {
    void load();
  }
}

function changePeriod(delta: number): void {
  if (!discardEdits()) {
    return;
  }
  const next = ReportPeriod.fromPeriodId(periodId.value);
  if (delta > 0) {
    next.increment();
  } else {
    next.decrement();
  }
  periodId.value = next.id;
  void load();
}

function goToCurrentPeriod(): void {
  if (!discardEdits()) {
    return;
  }
  periodId.value = currentPeriodId;
  void load();
}

function discardEdits(): boolean {
  return edits.value.size === 0 || window.confirm(t("Несохранённые изменения будут потеряны. Продолжить?"));
}
</script>

<style scoped>
.resource-allocation-search {
  min-width: 220px;
  max-width: 320px;
}

.resource-allocation-filter-group {
  flex: 1 1 620px;
}

.resource-allocation-ba-filter {
  min-width: 220px;
  max-width: 300px;
}

.resource-allocation-input,
.resource-allocation-value {
  width: 0;
  min-width: 0;
  height: 32px;
  box-sizing: border-box;
  flex: 1 1 auto;
  padding: 6px 8px;
  border: 1px solid rgb(var(--v-theme-outline));
  border-radius: 4px;
  background: rgba(var(--v-theme-primary), 0.04);
  cursor: text;
  color: rgb(var(--v-theme-on-surface));
  font: inherit;
  text-align: right;
}

.resource-allocation-input:focus,
.resource-allocation-value:not(:disabled):hover,
.resource-allocation-value:not(:disabled):focus-visible {
  border-color: rgb(var(--v-theme-primary));
  box-shadow: 0 0 0 1px rgb(var(--v-theme-primary));
  outline: none;
}

.resource-allocation-value:disabled {
  border-color: transparent;
  background: transparent;
  cursor: default;
  opacity: 0.55;
}

.resource-allocation-cell,
.resource-allocation-employee-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: space-between;
}

.resource-allocation-cell {
  width: 100%;
  min-width: 0;
}

.resource-allocation-cell-action {
  width: 24px;
}

.resource-allocation-row-action {
  width: 24px;
}

:deep(tbody tr .resource-allocation-row-delete) {
  opacity: 0;
  pointer-events: none;
  transition: opacity 120ms ease-in-out;
}

:deep(tbody tr:hover .resource-allocation-row-delete),
:deep(tbody tr:focus-within .resource-allocation-row-delete) {
  opacity: 1;
  pointer-events: auto;
}

.resource-allocation-cell-clear {
  width: 24px;
  height: 24px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: rgb(var(--v-theme-on-surface));
  cursor: pointer;
  opacity: 0;
}

.resource-allocation-cell-clear:hover,
.resource-allocation-cell-clear:focus-visible {
  background: rgba(var(--v-theme-on-surface), 0.08);
}

.resource-allocation-cell:hover .resource-allocation-cell-clear,
.resource-allocation-cell-clear:focus-visible {
  opacity: 1;
}

</style>
