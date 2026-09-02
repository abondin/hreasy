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
        <div class="resource-allocation-mode-controls d-flex flex-wrap align-center ga-2 border rounded pa-1">
          <span class="font-weight-medium px-1">{{ t("Режим") }}</span>
          <v-btn-toggle :model-value="activePreset" density="compact" color="primary">
            <v-btn value="mine" data-testid="resource-allocations-preset-mine" @click="applyPreset('mine')">
              {{ t("Мои проекты") }}
            </v-btn>
            <v-btn value="ba" data-testid="resource-allocations-preset-ba" @click="applyPreset('ba')">
              {{ t("Бизнес-аккаунты") }}
            </v-btn>
            <v-btn value="company" data-testid="resource-allocations-preset-company" @click="applyPreset('company')">
              {{ t("Компания") }}
            </v-btn>
          </v-btn-toggle>
          <v-autocomplete
            v-if="activePreset === 'ba'"
            :model-value="sharedBaIds"
            :items="businessAccounts"
            item-title="name"
            item-value="id"
            multiple
            clearable
            hide-details
            density="compact"
            variant="outlined"
            :label="t('Бизнес-аккаунты')"
            class="resource-allocation-multi-filter"
            data-testid="resource-allocations-preset-ba-values"
            @update:model-value="applySharedBusinessAccounts"
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="sharedBaIds.length"
                :label="getFilterSelectionLabel(item)"
              />
            </template>
          </v-autocomplete>
          <v-text-field
            :model-value="search"
            @update:model-value="search = normalizeSearchInput($event)"
            clearable
            hide-details
            density="compact"
            variant="outlined"
            prepend-inner-icon="mdi-magnify"
            :label="t('Поиск по ФИО')"
            class="resource-allocation-quick-search"
            data-testid="resource-allocations-employee-search"
          />
          <v-checkbox
            v-model="onlyAllocatedCells"
            hide-details
            density="compact"
            :label="t('Только с аллокациями')"
            data-testid="resource-allocations-only-allocated"
          />
        </div>
        <v-spacer />
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
      </div>

      <div class="d-flex flex-wrap align-stretch ga-3 mb-4">
        <v-sheet border rounded class="resource-allocation-filter-group pa-2 d-flex flex-wrap align-center ga-2">
          <span class="font-weight-medium">{{ t("Кого распределяем") }}</span>
          <v-btn-group density="compact" data-testid="resource-allocations-employee-scope">
            <v-btn
              :color="employeeScope === 'mine' ? 'primary' : undefined"
              data-testid="resource-allocations-employees-mine"
              @click="changeEmployeeScope('mine')"
            >
              {{ t("С моих проектов") }}
            </v-btn>
            <v-btn
              :color="employeeScope === 'selected' ? 'primary' : undefined"
              data-testid="resource-allocations-employees-selected"
              @click="changeEmployeeScope('selected')"
            >
              {{ t("Выбрать") }}
            </v-btn>
            <v-btn
              :color="employeeScope === 'all' ? 'primary' : undefined"
              data-testid="resource-allocations-employees-all"
              @click="changeEmployeeScope('all')"
            >
              {{ t("Все") }}
            </v-btn>
          </v-btn-group>
          <v-autocomplete
            v-if="employeeScope === 'selected'"
            v-model="employeeBaIds"
            :items="businessAccounts"
            item-title="name"
            item-value="id"
            multiple
            clearable
            hide-details
            density="compact"
            variant="outlined"
            :label="t('БА сотрудников')"
            class="resource-allocation-multi-filter"
            data-testid="resource-allocations-employee-ba"
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="employeeBaIds.length"
                :label="getFilterSelectionLabel(item)"
              />
            </template>
          </v-autocomplete>
          <v-autocomplete
            v-if="employeeScope === 'selected'"
            v-model="employeeProjectIds"
            :items="employeeProjectOptions"
            item-title="name"
            item-value="id"
            multiple
            clearable
            hide-details
            density="compact"
            variant="outlined"
            :label="t('Проекты сотрудников')"
            class="resource-allocation-multi-filter"
            data-testid="resource-allocations-employee-projects"
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="employeeProjectIds.length"
                :label="getFilterSelectionLabel(item)"
              />
            </template>
          </v-autocomplete>
        </v-sheet>

        <v-sheet border rounded class="resource-allocation-filter-group pa-2 d-flex flex-wrap align-center ga-2">
          <span class="font-weight-medium">{{ t("Куда распределяем") }}</span>
          <v-btn-group density="compact" data-testid="resource-allocations-project-scope">
            <v-btn
              :color="projectScope === 'mine' ? 'primary' : undefined"
              data-testid="resource-allocations-scope-mine"
              @click="changeProjectScope('mine')"
            >
              {{ t("Мои проекты") }}
            </v-btn>
            <v-btn
              :color="projectScope === 'selected' ? 'primary' : undefined"
              data-testid="resource-allocations-scope-selected"
              @click="changeProjectScope('selected')"
            >
              {{ t("Выбрать") }}
            </v-btn>
            <v-btn
              :color="projectScope === 'all' ? 'primary' : undefined"
              data-testid="resource-allocations-scope-all"
              @click="changeProjectScope('all')"
            >
              {{ t("Все") }}
            </v-btn>
          </v-btn-group>
          <v-autocomplete
            v-if="projectScope === 'selected'"
            v-model="projectBaIds"
            :items="businessAccounts"
            item-title="name"
            item-value="id"
            multiple
            clearable
            hide-details
            density="compact"
            variant="outlined"
            :label="t('БА проектов')"
            class="resource-allocation-multi-filter"
            data-testid="resource-allocations-project-ba"
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="projectBaIds.length"
                :label="getFilterSelectionLabel(item)"
              />
            </template>
          </v-autocomplete>
          <v-autocomplete
            v-if="projectScope === 'selected'"
            v-model="columnProjectIds"
            :items="columnProjectOptions"
            item-title="name"
            item-value="id"
            multiple
            clearable
            hide-details
            density="compact"
            variant="outlined"
            :label="t('Проекты')"
            class="resource-allocation-multi-filter"
            data-testid="resource-allocations-project-projects"
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="columnProjectIds.length"
                :label="getFilterSelectionLabel(item)"
              />
            </template>
          </v-autocomplete>
        </v-sheet>
      </div>

      <v-alert v-if="error" type="error" variant="tonal" class="mb-3" closable @click:close="error = ''">
        {{ error }}
      </v-alert>
      <v-alert v-if="saved" type="success" variant="tonal" class="mb-3" closable @click:close="saved = false">
        {{ t("Изменения сохранены") }}
      </v-alert>
      <v-alert v-if="warning" type="warning" variant="tonal" class="mb-3" closable @click:close="warning = ''">
        {{ warning }}
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
        <Grid
          v-else
          ref="grid"
          :columns="gridColumns"
          :source="gridRows"
          :readonly="saving"
          :row-size="36"
          :range="true"
          :use-clipboard="true"
          :apply-on-close="true"
          :hide-attribution="true"
          theme="compact"
          class="resource-allocation-grid"
          data-testid="resource-allocations-table"
          @beforeedit="handleBeforeEdit"
          @beforeeditstart="gridEditing = true"
          @afteredit="handleAfterEdit"
          @afterfocus="focusedCell = $event.detail"
          @closeedit="gridEditing = false"
        />
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
    <ConfirmDeleteDialog
      :open="clearEmployeeDialog"
      :title="t('Очистить аллокации сотрудника?')"
      :message="t('Все доступные аллокации сотрудника {employee} будут очищены после сохранения.', {
        employee: employeeToClear?.employee ?? '',
      })"
      :cancel-label="t('Отмена')"
      :confirm-label="t('Очистить')"
      data-testid="resource-allocations-clear-employee-dialog"
      @close="cancelClearEmployee"
      @confirm="confirmClearEmployee"
    />
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watch, type ComponentPublicInstance } from "vue";
import { useI18n } from "vue-i18n";
import { onBeforeRouteLeave } from "vue-router";
import Grid from "@revolist/vue3-datagrid";
import type {
  AfterEditEvent,
  BeforeSaveDataDetails,
  ColumnGrouping,
  ColumnRegular,
  FocusAfterRenderEvent,
} from "@revolist/revogrid";
import PeriodSwitcherControl from "@/components/shared/PeriodSwitcherControl.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import ConfirmDeleteDialog from "@/components/shared/ConfirmDeleteDialog.vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";
import { BusinessError, errorUtils } from "@/lib/errors";
import { normalizeSearchInput } from "@/lib/search";
import { ReportPeriod } from "@/services/overtime.service";
import {
  fetchResourceAllocations,
  saveResourceAllocations,
  type ResourceAllocationChange,
  type ResourceAllocationProject,
  type ResourceAllocationSheet,
  type ResourceAllocationValue,
} from "@/services/resource-allocation.service";

defineOptions({ name: "ResourceAllocationsView" });

interface AllocationRow {
  id: number;
  employee: string;
  total: number;
}

interface AllocationGridRow extends AllocationRow {
  [key: string]: string | number;
  [key: number]: string | number;
}

interface BusinessAccountOption {
  id: number;
  name: string;
}

type AllocationScope = "mine" | "selected" | "all";
type AllocationPreset = "mine" | "ba" | "company";

const { t } = useI18n();
const currentPeriodId = ReportPeriod.currentPeriod().id;
const periodId = ref(currentPeriodId);
const sheet = shallowRef<ResourceAllocationSheet | null>(null);
const edits = ref(new Map<string, number>());
const search = ref("");
const employeeScope = ref<AllocationScope>("mine");
const employeeBaIds = ref<number[]>([]);
const employeeProjectIds = ref<number[]>([]);
const projectScope = ref<AllocationScope>("mine");
const projectBaIds = ref<number[]>([]);
const columnProjectIds = ref<number[]>([]);
const onlyAllocatedCells = ref(false);
const loading = ref(false);
const saving = ref(false);
const saved = ref(false);
const error = ref("");
const warning = ref("");
const gridEditing = ref(false);
const grid = ref<ComponentPublicInstance | null>(null);
const focusedCell = ref<FocusAfterRenderEvent | null>(null);
const discardDialog = ref(false);
const clearEmployeeDialog = ref(false);
const employeeToClear = ref<AllocationRow | null>(null);
let pendingDiscardAction: (() => void) | null = null;
let resolveRouteLeave: ((allow: boolean) => void) | null = null;

const periodLabel = computed(() => ReportPeriod.fromPeriodId(periodId.value).toString());
const normalizedSearch = computed(() => search.value.toLocaleLowerCase());
const initialValues = computed(() => new Map(
  (sheet.value?.allocations ?? []).map(value => [cellKey(value.employeeId, value.projectId), value.percent]),
));
const initialRevisionIds = computed(() => new Map(
  (sheet.value?.allocations ?? []).map(value => [cellKey(value.employeeId, value.projectId), value.revisionId]),
));
const previousValues = computed(() => new Map(
  (sheet.value?.previousAllocations ?? []).map(value => [cellKey(value.employeeId, value.projectId), value.percent]),
));
const initiallyAllocatedProjectIds = computed(() => new Set(
  (sheet.value?.allocations ?? []).filter(value => value.percent > 0).map(value => value.projectId),
));
const hasPendingChanges = computed(() => edits.value.size > 0 || gridEditing.value);
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
const selectableProjects = computed(() => (sheet.value?.projects ?? [])
  .filter(project => project.active || initiallyAllocatedProjectIds.value.has(project.id))
  .sort((left, right) => left.name.localeCompare(right.name)));
const activePreset = computed<AllocationPreset | null>(() => {
  if (employeeScope.value === "mine" && projectScope.value === "mine") {
    return "mine";
  }
  if (employeeScope.value === "all" && projectScope.value === "all") {
    return "company";
  }
  if (employeeScope.value === "selected"
    && projectScope.value === "selected"
    && employeeProjectIds.value.length === 0
    && columnProjectIds.value.length === 0
    && sameIds(employeeBaIds.value, projectBaIds.value)) {
    return "ba";
  }
  return null;
});
const sharedBaIds = computed(() => sameIds(employeeBaIds.value, projectBaIds.value) ? employeeBaIds.value : []);
const employeeProjectOptions = computed(() => projectsForBusinessAccounts(employeeBaIds.value));
const columnProjectOptions = computed(() => projectsForBusinessAccounts(projectBaIds.value));
const visibleProjects = computed(() => selectableProjects.value
  .filter(project => projectScope.value !== "mine" || project.managed)
  .filter(project => projectScope.value !== "selected" || projectBaIds.value.length === 0
    || (project.baId != null && projectBaIds.value.includes(project.baId)))
  .filter(project => projectScope.value !== "selected"
    || columnProjectIds.value.length === 0
    || columnProjectIds.value.includes(project.id))
  .filter(project => !onlyAllocatedCells.value
    || allocationStats.value.allocatedProjectIds.has(project.id))
  .sort((left, right) => Number(right.editable) - Number(left.editable)
    || Number(right.active) - Number(left.active)
    || left.name.localeCompare(right.name)));
const visibleProjectIds = computed(() => new Set(visibleProjects.value.map(project => project.id)));
const rows = computed<AllocationRow[]>(() => (sheet.value?.employees ?? [])
  .filter(employee => !normalizedSearch.value
    || employee.displayName.toLocaleLowerCase().includes(normalizedSearch.value))
  .filter(employee => employeeScope.value !== "mine"
    || (employee.currentProjectId != null && projectsById.value.get(employee.currentProjectId)?.managed))
  .filter((employee) => {
    if (employeeScope.value !== "selected" || employeeBaIds.value.length === 0) {
      return true;
    }
    const baId = employee.currentProjectId == null
      ? null
      : projectsById.value.get(employee.currentProjectId)?.baId;
    return baId != null && employeeBaIds.value.includes(baId);
  })
  .filter(employee => employeeScope.value !== "selected" || employeeProjectIds.value.length === 0
    || (employee.currentProjectId != null && employeeProjectIds.value.includes(employee.currentProjectId)))
  .filter(employee => !onlyAllocatedCells.value
    || [...(allocationStats.value.projectIdsByEmployee.get(employee.id) ?? [])]
      .some(projectId => visibleProjectIds.value.has(projectId)))
  .map(employee => ({
    id: employee.id,
    employee: employee.displayName,
    total: allocationStats.value.totalsByEmployee.get(employee.id) ?? 0,
  })));
const gridRows = computed<AllocationGridRow[]>(() => rows.value.map((row) => {
  const gridRow: AllocationGridRow = { ...row };
  for (const project of visibleProjects.value) {
    gridRow[projectProp(project.id)] = cellValue(row.id, project.id) || "";
  }
  return gridRow;
}));
const gridColumns = computed<(ColumnRegular | ColumnGrouping)[]>(() => {
  const projectGroups = new Map<number | null, { name: string; projects: ResourceAllocationProject[] }>();
  for (const project of visibleProjects.value) {
    const group = projectGroups.get(project.baId) ?? {
      name: project.baName ?? t("Без БА"),
      projects: [],
    };
    group.projects.push(project);
    projectGroups.set(project.baId, group);
  }

  return [
  {
    name: t("Сотрудник"),
    prop: "employee",
    size: 320,
    minSize: 320,
    maxSize: 320,
    pin: "colPinStart",
    readonly: true,
    cellProperties: ({ model: sourceModel }) => {
      const model = sourceModel as AllocationGridRow;
      return {
        class: "resource-allocation-employee-grid-cell",
        "data-testid": `resource-allocation-employee-${model.id}`,
      };
    },
    cellTemplate: (h, { model: sourceModel }) => {
      const model = sourceModel as AllocationGridRow;
      return h("div", { class: "resource-allocation-employee-cell" }, [
      h("span", { class: "resource-allocation-employee-name", title: model.employee }, model.employee),
      h("span", { class: "resource-allocation-row-action" }, [canClearEmployee(model.id)
        ? h("button", {
          type: "button",
          class: "resource-allocation-row-delete mdi mdi-delete",
          disabled: saving.value,
          "aria-label": t("Очистить аллокации сотрудника"),
          title: t("Очистить аллокации сотрудника"),
          "data-testid": `resource-allocation-clear-row-${model.id}`,
          onMouseDown: (event: MouseEvent) => event.stopPropagation(),
          onClick: (event: MouseEvent) => {
            event.stopPropagation();
            requestClearEmployee(model);
          },
        })
        : null]),
      ]);
    },
  },
  {
    name: t("Итого"),
    prop: "total",
    size: 90,
    minSize: 90,
    maxSize: 90,
    pin: "colPinStart",
    readonly: true,
    cellProperties: ({ model: sourceModel }) => ({
      "data-testid": `resource-allocation-total-${(sourceModel as AllocationGridRow).id}`,
    }),
    cellTemplate: (h, { model: sourceModel }) => {
      const model = sourceModel as AllocationGridRow;
      return h("span", {
        class: `resource-allocation-total resource-allocation-total--${totalColor(model.total)}`,
      }, `${model.total}%`);
    },
  },
  ...[...projectGroups.values()].map(group => ({
    name: group.name,
    children: group.projects.map((project) => {
    const prop = projectProp(project.id);
    return {
      name: project.name,
      prop,
      size: 130,
      minSize: 130,
      maxSize: 130,
      sortable: false,
      columnProperties: () => ({ title: project.name }),
      readonly: ({ model: sourceModel }) => {
        const model = sourceModel as AllocationGridRow;
        return saving.value || !project.editable || (onlyAllocatedCells.value && !Number(model[prop]));
      },
      cellProperties: ({ model: sourceModel }) => {
        const model = sourceModel as AllocationGridRow;
        return {
          class: project.editable
            ? "resource-allocation-project-grid-cell"
            : "resource-allocation-project-grid-cell resource-allocation-project-grid-cell--readonly",
          "aria-label": `${model.employee} — ${project.name}`,
          "data-testid": `resource-allocation-cell-${model.id}-${project.id}`,
        };
      },
      cellTemplate: (h, { model: sourceModel }) => {
        const model = sourceModel as AllocationGridRow;
        const value = Number(model[prop]) || 0;
        if (onlyAllocatedCells.value && value === 0) {
          return "";
        }
        const key = cellKey(model.id, project.id);
        const previousValue = edits.value.has(key) ? 0 : previousValues.value.get(key) ?? 0;
        return h("div", { class: "resource-allocation-cell" }, [
          h("span", {
            class: previousValue && !value
              ? "resource-allocation-value resource-allocation-value--previous"
              : "resource-allocation-value",
            title: previousValue && !value ? t("В прошлом месяце: {value}%", { value: previousValue }) : undefined,
          }, value ? String(value) : previousValue ? String(previousValue) : ""),
        ]);
      },
    } satisfies ColumnRegular;
    }),
  })),
  ];
});

watch(employeeProjectOptions, projects => pruneProjectSelection(employeeProjectIds, projects));
watch(columnProjectOptions, projects => pruneProjectSelection(columnProjectIds, projects));
onMounted(() => {
  document.addEventListener("mouseover", prepareAutofill, true);
  void load();
});
onBeforeUnmount(() => document.removeEventListener("mouseover", prepareAutofill, true));
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
  loading.value = true;
  error.value = "";
  warning.value = "";
  saved.value = false;
  try {
    sheet.value = await fetchResourceAllocations(periodId.value);
    edits.value.clear();
    gridEditing.value = false;
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

async function save(): Promise<void> {
  await nextTick();
  saving.value = true;
  error.value = "";
  warning.value = "";
  try {
    await saveResourceAllocations(periodId.value, [...edits.value].map(([key, percent]) => {
      const [employeeId, projectId] = key.split(":").map(Number);
      return { employeeId, projectId, percent, expectedRevisionId: initialRevisionIds.value.get(key) ?? null };
    }));
    await load();
    saved.value = true;
  } catch (saveError) {
    if (saveError instanceof BusinessError && saveError.code === "errors.resource_allocation.conflict") {
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
    ? conflictError.attrs.allocations.filter(isAllocationValue)
    : null;
  const changes = Array.isArray(conflictError.attrs?.changes)
    ? conflictError.attrs.changes.filter(isAllocationChange)
    : null;
  const conflicts = Array.isArray(conflictError.attrs?.conflicts) ? conflictError.attrs.conflicts : null;
  if (!sheet.value || !allocations || !changes || !conflicts) {
    error.value = conflictError.message;
    return;
  }
  sheet.value = { ...sheet.value, allocations };
  edits.value = new Map(changes.map(change => [cellKey(change.employeeId, change.projectId), change.percent]));
  gridEditing.value = false;
  warning.value = t(
    "Данные обновлены. Конфликтующих ячеек: {count}. Остальные несохранённые изменения сохранены.",
    { count: conflicts.length },
  );
}

function isAllocationValue(value: unknown): value is ResourceAllocationValue {
  return !!value && typeof value === "object"
    && "employeeId" in value && typeof value.employeeId === "number"
    && "projectId" in value && typeof value.projectId === "number"
    && "percent" in value && typeof value.percent === "number"
    && "revisionId" in value && typeof value.revisionId === "number";
}

function isAllocationChange(value: unknown): value is ResourceAllocationChange {
  return !!value && typeof value === "object"
    && "employeeId" in value && typeof value.employeeId === "number"
    && "projectId" in value && typeof value.projectId === "number"
    && "percent" in value && typeof value.percent === "number"
    && "expectedRevisionId" in value
    && (value.expectedRevisionId === null || typeof value.expectedRevisionId === "number");
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

function normalizePercent(value: string): number {
  return Math.min(1000, Math.max(0, Math.round(Number(value) || 0)));
}

function projectProp(projectId: number): string {
  return `project_${projectId}`;
}

function applyPreset(preset: AllocationPreset): void {
  search.value = "";
  clearEmployeeSelection();
  clearProjectSelection();
  employeeScope.value = preset === "mine" ? "mine" : preset === "company" ? "all" : "selected";
  projectScope.value = employeeScope.value;
}

function applySharedBusinessAccounts(value: number[] | null): void {
  const ids = value ?? [];
  employeeBaIds.value = [...ids];
  projectBaIds.value = [...ids];
}

function changeEmployeeScope(scope: AllocationScope): void {
  employeeScope.value = scope;
  if (scope !== "selected") {
    clearEmployeeSelection();
  }
}

function changeProjectScope(scope: AllocationScope): void {
  projectScope.value = scope;
  if (scope !== "selected") {
    clearProjectSelection();
  }
}

function clearEmployeeSelection(): void {
  employeeBaIds.value = [];
  employeeProjectIds.value = [];
}

function clearProjectSelection(): void {
  projectBaIds.value = [];
  columnProjectIds.value = [];
}

function sameIds(left: number[], right: number[]): boolean {
  return left.length === right.length && left.every(id => right.includes(id));
}

function getFilterSelectionLabel(item: unknown): string {
  if (typeof item === "string") {
    return item;
  }
  if (item && typeof item === "object" && "title" in item && typeof item.title === "string") {
    return item.title;
  }
  if (item && typeof item === "object" && "name" in item && typeof item.name === "string") {
    return item.name;
  }
  return "";
}

function projectsForBusinessAccounts(baIds: number[]): ResourceAllocationProject[] {
  return baIds.length === 0
    ? selectableProjects.value
    : selectableProjects.value.filter(project => project.baId != null && baIds.includes(project.baId));
}

function pruneProjectSelection(selection: { value: number[] }, projects: ResourceAllocationProject[]): void {
  const availableIds = new Set(projects.map(project => project.id));
  selection.value = selection.value.filter(projectId => availableIds.has(projectId));
}

function projectIdFromProp(prop: string | number): number | null {
  const match = /^project_(\d+)$/.exec(String(prop));
  return match ? Number(match[1]) : null;
}

function handleBeforeEdit(event: CustomEvent<BeforeSaveDataDetails>): void {
  if (projectIdFromProp(event.detail.prop) != null) {
    event.detail.val = normalizePercent(String(event.detail.val ?? ""));
  }
}

function handleAfterEdit(event: CustomEvent<AfterEditEvent>): void {
  const detail = event.detail;
  if ("prop" in detail) {
    const model = detail.model as AllocationGridRow;
    const projectId = projectIdFromProp(detail.prop);
    if (projectId != null) {
      updateCell(model.id, projectId, normalizePercent(String(detail.val ?? model[detail.prop])));
    }
    return;
  }
  for (const [rowIndex, changedModel] of Object.entries(detail.data)) {
    const model = detail.models[Number(rowIndex)] as AllocationGridRow | undefined;
    if (!model) {
      continue;
    }
    for (const [prop, value] of Object.entries(changedModel)) {
      const projectId = projectIdFromProp(prop);
      if (projectId != null && projectsById.value.get(projectId)?.editable) {
        updateCell(model.id, projectId, normalizePercent(String(value ?? "")));
      }
    }
  }
}

async function prepareAutofill(event: MouseEvent): Promise<void> {
  const isHandle = event.composedPath()
    .some(target => target instanceof Element && target.classList.contains("autofill-handle"));
  const projectId = projectIdFromProp(focusedCell.value?.column?.prop ?? "");
  const gridElement = grid.value?.$el as HTMLRevoGridElement | undefined;
  if (!isHandle || !focusedCell.value || projectId == null || !projectsById.value.get(projectId)?.editable || !gridElement) {
    return;
  }
  const cell = { x: focusedCell.value.colIndex, y: focusedCell.value.rowIndex };
  await gridElement.setCellsFocus(cell, cell, focusedCell.value.colType, focusedCell.value.rowType);
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

function requestClearEmployee(employee: AllocationRow): void {
  employeeToClear.value = employee;
  clearEmployeeDialog.value = true;
}

function cancelClearEmployee(): void {
  clearEmployeeDialog.value = false;
  employeeToClear.value = null;
}

function confirmClearEmployee(): void {
  const employee = employeeToClear.value;
  cancelClearEmployee();
  if (!employee) {
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
  runAfterDiscard(() => void load());
}

function discardChanges(): void {
  runAfterDiscard(() => undefined);
}

function changePeriod(delta: number): void {
  runAfterDiscard(() => {
    const next = ReportPeriod.fromPeriodId(periodId.value);
    if (delta > 0) {
      next.increment();
    } else {
      next.decrement();
    }
    periodId.value = next.id;
    void load();
  });
}

function goToCurrentPeriod(): void {
  runAfterDiscard(() => {
    periodId.value = currentPeriodId;
    void load();
  });
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
  edits.value.clear();
  gridEditing.value = false;
  const action = pendingDiscardAction;
  pendingDiscardAction = null;
  resolveRouteLeave?.(true);
  resolveRouteLeave = null;
  action?.();
}
</script>

<style scoped>
.resource-allocation-quick-search {
  min-width: 280px;
  max-width: 520px;
}

.resource-allocation-mode-controls {
  flex: 1 1 900px;
}

.resource-allocation-filter-group {
  flex: 1 1 620px;
}

.resource-allocation-multi-filter {
  min-width: 220px;
  max-width: 300px;
}

.resource-allocation-grid {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 360px;
  --revo-grid-primary: rgb(var(--v-theme-primary));
  --revo-grid-primary-transparent: rgba(var(--v-theme-primary), 0.16);
  --revo-grid-background: rgb(var(--v-theme-surface));
  --revo-grid-foreground: rgb(var(--v-theme-on-surface));
  --revo-grid-text: rgb(var(--v-theme-on-surface));
  --revo-grid-border: rgba(var(--v-border-color), var(--v-border-opacity));
  --revo-grid-cell-border: rgba(var(--v-border-color), var(--v-border-opacity));
  --revo-grid-header-bg: rgb(var(--v-theme-surface));
  --revo-grid-header-color: rgb(var(--v-theme-on-surface));
  --revo-grid-header-border: rgba(var(--v-border-color), var(--v-border-opacity));
  --revo-grid-row-hover: rgba(var(--v-theme-on-surface), 0.04);
  --revo-grid-cell-disabled-bg: rgb(var(--v-theme-surface));
}

:deep(.resource-allocation-cell),
:deep(.resource-allocation-employee-cell) {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: space-between;
  width: 100%;
  height: 100%;
}

:deep(.resource-allocation-employee-grid-cell) {
  border-right: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
}

:deep(.resource-allocation-employee-name) {
  overflow: hidden;
  min-width: 0;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.resource-allocation-row-action) {
  width: 24px;
  flex: 0 0 24px;
}

:deep(.resource-allocation-project-grid-cell) {
  background: rgba(var(--v-theme-primary), 0.035);
  cursor: cell;
}

:deep(.resource-allocation-project-grid-cell--readonly) {
  background: rgb(var(--v-theme-surface));
  cursor: default;
  opacity: 0.58;
}

:deep(.resource-allocation-value) {
  min-width: 0;
  flex: 1 1 auto;
  text-align: right;
}

:deep(.resource-allocation-value--previous) {
  opacity: 0.35;
}

:deep(.resource-allocation-row-delete) {
  width: 24px;
  height: 24px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: rgb(var(--v-theme-on-surface));
  cursor: pointer;
  opacity: 0;
  transition: opacity 120ms ease-in-out;
}

:deep(.resource-allocation-row-delete) {
  color: rgb(var(--v-theme-error));
}

:deep(.resource-allocation-row-delete:hover),
:deep(.resource-allocation-row-delete:focus-visible) {
  background: rgba(var(--v-theme-on-surface), 0.08);
  opacity: 1;
  outline: none;
}

:deep(.resource-allocation-employee-cell:hover .resource-allocation-row-delete) {
  opacity: 1;
}

:deep(.resource-allocation-total) {
  display: inline-flex;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 500;
}

:deep(.resource-allocation-total--success) {
  color: rgb(var(--v-theme-success));
  background: rgba(var(--v-theme-success), 0.12);
}

:deep(.resource-allocation-total--warning) {
  color: rgb(var(--v-theme-warning));
  background: rgba(var(--v-theme-warning), 0.12);
}

:deep(.resource-allocation-total--error) {
  color: rgb(var(--v-theme-error));
  background: rgba(var(--v-theme-error), 0.12);
}

:deep(revogr-edit input) {
  width: 100%;
  height: 100%;
  padding: 0 8px;
  border: 1px solid rgb(var(--v-theme-primary));
  border-radius: 3px;
  color: rgb(var(--v-theme-on-surface));
  background: rgb(var(--v-theme-surface));
  font: inherit;
  text-align: right;
  outline: none;
}

</style>
