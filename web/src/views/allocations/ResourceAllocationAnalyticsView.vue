<template>
  <div
    class="mt-4 d-flex flex-column flex-grow-1 min-h-0"
    data-testid="resource-allocations-analytics-view"
  >
    <TablePageCard test-id="resource-allocations-analytics-card">
      <AdaptiveFilterBar
        :items="toolbarFilterItems"
        :has-right-actions="canAdminPeriods"
        class="mb-4"
      >
        <template #left-actions>
          <TableToolbarActions
            data-testid="resource-allocations-toolbar-actions"
            show-refresh
            show-export
            :disabled="loading || exporting"
            :refresh-label="t('Обновить данные')"
            :export-label="t('Экспорт в Excel за весь год')"
            @refresh="load"
            @export="exportToExcel"
          />
          <PeriodSwitcherControl
            :label="String(year)"
            :is-current="year === currentYear"
            :disabled="loading"
            label-test-id="resource-allocations-analytics-year"
            @prev="changeYear(-1)"
            @next="changeYear(1)"
            @go-current="goToCurrentYear"
          />
        </template>

        <template v-if="canAdminPeriods" #right-actions>
          <v-tooltip location="bottom">
            <template #activator="{ props }">
              <v-btn
                v-bind="props"
                icon="mdi-lock"
                variant="text"
                :disabled="loading"
                data-testid="resource-allocations-period-toggle"
                @click="openPeriodDialog"
              />
            </template>
            <span>{{ t("Настроить блокировку периодов") }}</span>
          </v-tooltip>
        </template>

        <template #filter-mode>
          <v-btn-toggle
            v-model="mode"
            mandatory
            divided
            density="compact"
            variant="outlined"
            data-testid="resource-allocations-analytics-mode"
          >
            <v-btn value="employees">{{ t("Сотрудники") }}</v-btn>
            <v-btn value="projects">{{ t("Проекты") }}</v-btn>
          </v-btn-toggle>
        </template>

        <template #filter-ba>
          <v-autocomplete
            v-model="businessAccountIds"
            :items="businessAccounts"
            item-title="name"
            item-value="id"
            multiple
            clearable
            hide-details
            density="compact"
            variant="outlined"
            :label="t('Бизнес-аккаунты')"
            data-testid="resource-allocations-business-accounts"
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="businessAccountIds.length"
                :label="filterLabel(item)"
              />
            </template>
          </v-autocomplete>
        </template>

        <template #filter-projects>
          <v-autocomplete
            v-model="projectIds"
            :items="availableProjects"
            item-title="name"
            item-value="id"
            multiple
            clearable
            hide-details
            density="compact"
            variant="outlined"
            :label="t('Проекты')"
            data-testid="resource-allocations-projects"
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="projectIds.length"
                :label="filterLabel(item)"
              />
            </template>
          </v-autocomplete>
        </template>

        <template #filter-totals>
          <div class="d-flex align-center h-100">
            <v-checkbox-btn
              v-model="showGroupTotals"
              :label="t('Итоги по группам')"
              density="compact"
              data-testid="resource-allocations-group-totals"
            />
          </div>
        </template>

        <template #filter-display>
          <v-btn-toggle
            v-model="displayUnit"
            mandatory
            divided
            density="compact"
            variant="outlined"
            :aria-label="t('Единицы измерения')"
            data-testid="resource-allocations-display-unit"
          >
            <v-btn value="percent">{{ t("Проценты (%)") }}</v-btn>
            <v-btn value="personMonths">{{ t("Человеко-месяцы") }}</v-btn>
          </v-btn-toggle>
        </template>

        <template #filter-search>
          <SearchTextField
            v-model="search"
            v-model:settings="searchSettings"
            :label="t('Поиск по сотруднику, роли, проекту или направлению работ')"
            test-id="resource-allocations-analytics-search"
          />
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

      <template #table>
        <div
          v-if="!loading && gridRows.length === 0"
          class="d-flex align-center justify-center h-100"
          data-testid="resource-allocations-empty"
        >
          <v-empty-state
            icon="mdi-chart-box-outline"
            :title="t('Нет аллокаций по выбранным фильтрам')"
            :text="t('Измените фильтры, чтобы увидеть другие данные')"
          />
        </div>
        <Grid
          v-else
          :key="mode"
          :columns="gridColumns"
          :source="gridRows"
          :grouping="grouping"
          :additional-data="groupSummaries"
          :readonly="true"
          :resize="true"
          :range="true"
          :use-clipboard="true"
          :hide-attribution="true"
          theme="compact"
          class="h-100 w-100"
          data-testid="resource-allocations-table"
        />
      </template>
    </TablePageCard>

    <ResourceAllocationCommentsPopover
      :open="commentOpen"
      :target="commentTarget"
      :cell="commentCell"
      @update:open="handleCommentOpen"
      @count="updateCommentCount"
    />

    <v-dialog v-model="periodDialog" max-width="560">
      <v-card>
        <v-card-title>{{ t("Блокировка периодов за {year} год", { year }) }}</v-card-title>
        <v-card-text>
          <v-alert v-if="error" type="error" variant="tonal" class="mb-3">
            {{ error }}
          </v-alert>
          <v-row dense>
            <v-col v-for="month in monthOptions" :key="month.id" cols="6" sm="4">
              <v-checkbox
                v-model="closedPeriodsDraft"
                :value="month.id"
                hide-details
                density="compact"
                :disabled="periodUpdating"
                :data-testid="`resource-allocations-period-${month.id}`"
              >
                <template #label>
                  <span :class="{ 'font-weight-bold text-primary': month.id === currentPeriod }">
                    {{ month.name }}
                  </span>
                </template>
              </v-checkbox>
            </v-col>
          </v-row>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn :disabled="periodUpdating" @click="periodDialog = false">
            {{ t("Отмена") }}
          </v-btn>
          <v-btn
            color="primary"
            :loading="periodUpdating"
            data-testid="resource-allocations-periods-save"
            @click="savePeriodSelection"
          >
            {{ t("Сохранить") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onActivated, onMounted, ref, shallowRef, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import Grid from "@revolist/vue3-datagrid";
import {
  PSEUDO_GROUP_ITEM_VALUE,
  type ColumnRegular,
  type CellTemplate,
  type GroupCellTemplateFunc,
  type GroupingOptions,
} from "@revolist/revogrid";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import PeriodSwitcherControl from "@/components/shared/PeriodSwitcherControl.vue";
import SearchTextField from "@/components/shared/SearchTextField.vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import ResourceAllocationCommentsPopover, {
  type ResourceAllocationCommentCellContext,
} from "@/views/allocations/ResourceAllocationCommentsPopover.vue";
import { errorUtils } from "@/lib/errors";
import { usePermissions } from "@/lib/permissions";
import { createSearchSettings, matchesSearch } from "@/lib/search";
import { ReportPeriod } from "@/services/overtime.service";
import {
  exportResourceAllocationAnalytics,
  fetchClosedResourceAllocationPeriods,
  fetchResourceAllocationAnalytics,
  fetchResourceAllocationCommentSummary,
  saveClosedResourceAllocationPeriods,
  type ResourceAllocationAnalytics,
  type ResourceAllocationCommentCellKey,
  type ResourceAllocationCommentRow,
  type ResourceAllocationCommentSummary,
  type ResourceAllocationEmployee,
  type ResourceAllocationDisplayUnit,
  type ResourceAllocationProject,
} from "@/services/resource-allocation.service";

defineOptions({ name: "ResourceAllocationAnalyticsView" });
const emit = defineEmits<{ (event: "open-employee", employeeId: number): void }>();

type AnalyticsMode = "employees" | "projects";

interface BusinessAccountOption {
  id: number;
  name: string;
}

interface AnalyticsGridRow {
  id: string;
  entity: string;
  employee: string;
  project: string;
  businessAccount: string;
  employeeGroup: string;
  projectGroup: string;
  workstreamGroup: string;
  businessAccountGroup: string;
  employeeId: number;
  projectId: number;
  workstreamId: number | null;
  terminalGroup: boolean;
  [key: string]: string | number | boolean | null;
}

interface GroupSummary {
  label: string;
  months: Map<number, number>;
  projects: Set<string>;
  workstreams: Set<string>;
  dimensions: Set<string>;
  terminalRowId: string | null;
  yearTotal: number | null;
}

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const permissions = usePermissions();
const currentPeriod = ReportPeriod.currentPeriod().periodId();
const currentYear = Math.trunc(currentPeriod / 100);
const year = ref(yearFromQuery(route.query.year));
const mode = ref<AnalyticsMode>("projects");
const showGroupTotals = ref(true);
const displayUnit = ref(unitFromQuery(route.query.unit));
const sheet = shallowRef<ResourceAllocationAnalytics | null>(null);
const commentSummary = shallowRef<ResourceAllocationCommentSummary | null>(null);
const commentOpen = ref(false);
const commentTarget = ref<HTMLElement | null>(null);
const commentCell = ref<ResourceAllocationCommentCellContext | null>(null);
const businessAccountIds = ref<number[]>([]);
const projectIds = ref<number[]>([]);
const search = ref("");
const searchSettings = ref(createSearchSettings());
const loading = ref(false);
const exporting = ref(false);
const error = ref("");
const closedPeriods = ref(new Set<number>());
const closedPeriodsDraft = ref<number[]>([]);
const periodDialog = ref(false);
const periodUpdating = ref(false);
const hierarchyIndent = 16;
const terminalCellClass = "resource-allocation-terminal-cell";
const groupCellClass = "resource-allocation-group-cell";
const yearColumnStyle = { borderRight: "1px solid rgba(var(--v-theme-on-surface), 0.2)" };
let activated = false;
let loadRequestId = 0;

const toolbarFilterItems = computed(() => [
  { id: "mode", minWidth: 210 },
  { id: "totals", minWidth: 190, active: !showGroupTotals.value },
  { id: "display", minWidth: 320, active: displayUnit.value !== "personMonths" },
  { id: "ba", minWidth: 260, active: businessAccountIds.value.length > 0 },
  { id: "projects", minWidth: 280, active: projectIds.value.length > 0 },
  { id: "search", minWidth: 260, active: search.value.trim().length > 0, grow: true },
]);
const employeesById = computed(() => {
  const employees = new Map((sheet.value?.employees ?? []).map((employee) => [employee.id, employee]));
  for (const row of commentSummary.value?.rows ?? []) {
    if (!employees.has(row.employee.id)) employees.set(row.employee.id, row.employee);
  }
  return employees;
});
const projectsById = computed(() => {
  const projects = new Map((sheet.value?.projects ?? []).map((project) => [project.id, project]));
  for (const row of commentSummary.value?.rows ?? []) {
    if (!projects.has(row.project.id)) projects.set(row.project.id, {
      ...row.project,
      active: true,
      editable: false,
    });
  }
  return projects;
});
const workstreamsById = computed(() => {
  const workstreams = new Map((sheet.value?.workstreams ?? []).map((workstream) => [workstream.id, workstream]));
  for (const row of commentSummary.value?.rows ?? []) {
    if (row.workstream?.id != null && !workstreams.has(row.workstream.id)) {
      workstreams.set(row.workstream.id, row.workstream);
    }
  }
  return workstreams;
});
const canAdminPeriods = computed(() => permissions.canAdminResourceAllocations());
const monthOptions = computed(() =>
  Array.from({ length: 12 }, (_, month) => {
    const id = year.value * 100 + month;
    return { id, name: formatMonth(id) };
  }),
);
const businessAccounts = computed<BusinessAccountOption[]>(() =>
  [
    ...new Map(
      [...projectsById.value.values()]
        .filter((project) => project.baId != null)
        .map((project) => [
          project.baId as number,
          { id: project.baId as number, name: project.baName ?? String(project.baId) },
        ]),
    ).values(),
  ].sort((left, right) => left.name.localeCompare(right.name)),
);
const availableProjects = computed(() =>
  [...projectsById.value.values()]
    .filter(
      (project) =>
        businessAccountIds.value.length === 0 ||
        (project.baId != null && businessAccountIds.value.includes(project.baId)),
    )
    .sort((left, right) => left.name.localeCompare(right.name)),
);
const visibleProjectIds = computed(() =>
  new Set(
    availableProjects.value
      .filter((project) => projectIds.value.length === 0 || projectIds.value.includes(project.id))
      .map((project) => project.id),
  ),
);
const allocationRows = computed<AnalyticsGridRow[]>(() => {
  const rows = new Map<string, AnalyticsGridRow>();
  for (const allocation of sheet.value?.allocations ?? []) {
    const row = getOrCreateRow(rows, allocation.employeeId, allocation.projectId,
      allocation.workstreamId ?? null);
    if (!row) continue;
    row[monthProp(allocation.period)] = Number(row[monthProp(allocation.period)] ?? 0) + allocation.percent;
  }
  for (const commentRow of commentSummary.value?.rows ?? []) {
    getOrCreateRow(rows, commentRow.employee.id, commentRow.project.id,
      commentRow.workstream?.id ?? null);
  }
  const result = [...rows.values()];
  if (mode.value === "projects") {
    const streamedProjectIds = new Set(
      result.filter((row) => row.workstreamId != null).map((row) => row.projectId),
    );
    for (const row of result) {
      if (row.workstreamId == null && !streamedProjectIds.has(row.projectId)) {
        row.workstreamGroup = `employee:${row.employeeId}`;
        row.terminalGroup = true;
      }
    }
  } else {
    const streamedScopes = new Set(
      result.filter((row) => row.workstreamId != null).map((row) => `${row.employeeId}:${row.projectId}`),
    );
    for (const row of result) {
      row.terminalGroup = row.workstreamId == null && !streamedScopes.has(`${row.employeeId}:${row.projectId}`);
    }
  }
  return result.sort((left, right) => {
    const leftGroup = mode.value === "employees" ? left.employee : left.project;
    const rightGroup = mode.value === "employees" ? right.employee : right.project;
    return leftGroup.localeCompare(rightGroup) || left.entity.localeCompare(right.entity);
  });
});
// Keep the API snapshot in percentages; grid values also drive clipboard copying.
const gridRows = computed(() => allocationRows.value.map((row) => {
  const displayed = { ...row };
  let yearTotal = 0;
  let hasValue = false;
  for (let month = 0; month < 12; month += 1) {
    const prop = monthProp(year.value * 100 + month);
    const value = row[prop];
    if (value != null) {
      hasValue = true;
      yearTotal += Number(value);
      displayed[prop] = toDisplayValue(Number(value));
    }
  }
  displayed.yearTotal = hasValue ? toDisplayValue(yearTotal) : null;
  return displayed;
}));

const allocationRowsById = computed(() =>
  new Map(allocationRows.value.map(row => [row.id, row])),
);
const commentCounts = computed(() => {
  const counts = new Map<string, number>();
  for (const row of commentSummary.value?.rows ?? []) {
    for (const cell of row.cells) {
      counts.set(commentCellKey(cell.period, row.employee.id, row.project.id,
        row.workstream?.id ?? null), cell.commentCount);
    }
  }
  return counts;
});

const grouping = computed<GroupingOptions>(() => {
  const rows = allocationRows.value;
  const streamedScopes = new Set(
    rows.filter((row) => row.workstreamId != null).map((row) =>
      mode.value === "projects" ? String(row.projectId) : `${row.employeeId}:${row.projectId}`),
  );
  const prevExpanded: Record<string, boolean> = {};
  for (const row of rows) {
    const projectPath = mode.value === "projects"
      ? `${row.businessAccountGroup},${row.projectGroup}`
      : `${row.employeeGroup},${row.projectGroup}`;
    if (mode.value === "projects") prevExpanded[row.businessAccountGroup] = true;
    const scope = mode.value === "projects" ? String(row.projectId) : `${row.employeeId}:${row.projectId}`;
    if (!streamedScopes.has(scope)) {
      if (mode.value === "projects" || !row.terminalGroup) prevExpanded[projectPath] = true;
      if (mode.value === "projects" && !row.terminalGroup) {
        prevExpanded[`${projectPath},${row.workstreamGroup}`] = true;
      }
    }
  }
  return {
    props: mode.value === "projects"
      ? ["businessAccountGroup", "projectGroup", "workstreamGroup"]
      : ["employeeGroup", "projectGroup"],
    expandedAll: false,
    prevExpanded,
    groupCellTemplate,
  };
});
const groupSummaries = computed(() => {
  const summaries = new Map<string, GroupSummary>();
  for (const row of allocationRows.value) {
    const groups =
      mode.value === "projects"
        ? [
            [row.businessAccountGroup, row.businessAccount],
            [`${row.businessAccountGroup},${row.projectGroup}`, row.project],
            [
              `${row.businessAccountGroup},${row.projectGroup},${row.workstreamGroup}`,
              row.workstreamGroup.startsWith("employee:")
                ? row.employee
                : row.workstreamId == null
                  ? t("Без направления")
                  : workstreamsById.value.get(row.workstreamId)?.displayName ?? String(row.workstreamId),
            ],
          ]
        : [
            [row.employeeGroup, row.employee],
            [`${row.employeeGroup},${row.projectGroup}`, row.project],
          ];
    for (const [path, label] of groups) {
      const summary = summaries.get(path) ?? {
        label,
        months: new Map<number, number>(),
        projects: new Set<string>(),
        workstreams: new Set<string>(),
        dimensions: new Set<string>(),
        terminalRowId: null,
        yearTotal: null,
      };
      const terminalPath = mode.value === "projects"
        ? `${row.businessAccountGroup},${row.projectGroup},${row.workstreamGroup}`
        : `${row.employeeGroup},${row.projectGroup}`;
      if (path === terminalPath && row.terminalGroup) {
        summary.terminalRowId = row.id;
      }
      if (mode.value === "employees") summary.projects.add(row.project);
      summary.dimensions.add(row.workstreamId == null ? "project" : String(row.workstreamId));
      if (row.workstreamId != null) {
        const workstreamName = workstreamsById.value.get(row.workstreamId)?.displayName;
        if (workstreamName) summary.workstreams.add(workstreamName);
      }
      // Terminal groups represent actual data rows, including explicit zeros.
      if (showGroupTotals.value || summary.terminalRowId != null) {
        for (let month = 0; month < 12; month += 1) {
          const period = year.value * 100 + month;
          const value = row[monthProp(period)];
          if (value != null) summary.months.set(period, (summary.months.get(period) ?? 0) + Number(value));
        }
      }
      summaries.set(path, summary);
    }
  }
  for (const summary of summaries.values()) {
    if (summary.months.size > 0) {
      summary.yearTotal = toDisplayValue([...summary.months.values()].reduce((sum, value) => sum + value, 0));
      for (const [period, value] of summary.months) {
        summary.months.set(period, toDisplayValue(value));
      }
    }
  }
  return summaries;
});
const entityColumnWidth = Math.max(520, Math.min(800, Math.round(window.innerWidth * 0.36)));
const gridColumns = computed<ColumnRegular[]>(() => [
  {
    name: mode.value === "employees" ? t("Проект") : t("Сотрудник"),
    prop: "entity",
    size: entityColumnWidth,
    pin: "colPinStart",
    readonly: true,
    cellTemplate: (createElement, props) => createElement(
      "span",
      {
        class: (props.model as AnalyticsGridRow).terminalGroup ? undefined : terminalCellClass,
        "data-testid": `resource-allocation-analytics-row-${(props.model as AnalyticsGridRow).id}`,
        style: {
          display: "block",
          paddingLeft: `${(mode.value === "projects" ? 3 : 2) * hierarchyIndent}px`,
        },
      },
      [
        ...(mode.value === "projects"
          ? [employeeDetailsButton(createElement, (props.model as AnalyticsGridRow).employeeId)]
          : []),
        createElement("span", null, String(props.value ?? "")),
      ],
    ),
  },
  ...[
    { prop: "yearTotal", name: t("За год"), testId: "year-total", closed: false },
    ...Array.from({ length: 12 }, (_, month) => {
      const period = year.value * 100 + month;
      return {
        prop: monthProp(period),
        name: formatMonth(period),
        testId: String(period),
        closed: closedPeriods.value.has(period),
      };
    }),
  ].map(({ prop, name, testId, closed }) => ({
    name,
    prop,
    size: 110,
    minSize: 110,
    maxSize: 110,
    sortable: false,
    readonly: true,
    columnProperties: testId === "year-total" ? () => ({ style: yearColumnStyle }) : undefined,
    cellProperties: testId === "year-total" ? () => ({ style: yearColumnStyle }) : undefined,
    columnTemplate: closed
      ? (createElement) => createElement("span", null, [
          createElement("i", { class: "mdi mdi-lock mr-1", "aria-hidden": "true" }),
          createElement("span", null, name),
        ])
      : undefined,
    cellTemplate: (createElement, props) => {
      const row = props.model as AnalyticsGridRow;
      if (testId !== "year-total") {
        return commentCellTemplate(createElement, row, Number(testId), props.value);
      }
      return allocationValueCell(createElement, props.value, {
          class: row.terminalGroup ? undefined : terminalCellClass,
          "data-testid": `resource-allocation-analytics-cell-${row.id}-${testId}`,
      });
    },
  } satisfies ColumnRegular)),
]);

watch(availableProjects, (projects) => {
  const availableIds = new Set(projects.map((project) => project.id));
  projectIds.value = projectIds.value.filter((projectId) => availableIds.has(projectId));
});
watch(
  () => [route.name, route.query.year, route.query.unit],
  () => {
    if (route.name !== "resource-allocations-analytics") return;
    year.value = yearFromQuery(route.query.year);
    displayUnit.value = unitFromQuery(route.query.unit);
  },
);
watch([year, displayUnit], syncQuery);
watch(year, () => {
  commentOpen.value = false;
  void load();
});
onMounted(() => {
  syncQuery();
  void load();
});
onActivated(() => {
  if (activated) void load();
  activated = true;
});

async function load(): Promise<void> {
  const requestId = ++loadRequestId;
  loading.value = true;
  error.value = "";
  try {
    const [analytics, periods, comments] = await Promise.all([
      fetchResourceAllocationAnalytics(year.value),
      fetchClosedResourceAllocationPeriods(year.value),
      fetchResourceAllocationCommentSummary(year.value),
    ]);
    if (requestId !== loadRequestId) return;
    sheet.value = analytics;
    closedPeriods.value = new Set(periods);
    commentSummary.value = comments;
  } catch (loadError) {
    if (requestId !== loadRequestId) return;
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    if (requestId === loadRequestId) loading.value = false;
  }
}

function monthProp(period: number): string {
  return `month_${period}`;
}

function employeeLabel(employee: ResourceAllocationEmployee): string {
  return employee.currentProjectRole
    ? `${employee.displayName} · ${employee.currentProjectRole}`
    : employee.displayName;
}

function getOrCreateRow(
  rows: Map<string, AnalyticsGridRow>,
  employeeId: number,
  projectId: number,
  workstreamId: number | null,
): AnalyticsGridRow | null {
  if (!visibleProjectIds.value.has(projectId)) return null;
  const employee = employeesById.value.get(employeeId);
  const project = projectsById.value.get(projectId);
  const workstream = workstreamId == null ? null : workstreamsById.value.get(workstreamId);
  if (!employee || !project) return null;
  if (!matchesSearch(search.value, [
    employee.displayName,
    employee.email,
    employee.currentProjectRole,
    project.name,
    workstream?.displayName,
  ], searchSettings.value)) return null;
  const key = `${employee.id}:${project.id}:${workstreamId ?? "project"}`;
  const existing = rows.get(key);
  if (existing) return existing;
  const employeeText = employeeLabel(employee);
  const row: AnalyticsGridRow = {
    id: key,
    entity: mode.value === "employees"
      ? workstreamId == null
        ? t("Без направления")
        : workstream?.displayName ?? String(workstreamId)
      : employeeText,
    employee: employeeText,
    project: project.name,
    businessAccount: project.baName ?? t("Без бизнес-аккаунта"),
    employeeGroup: `employee:${employee.id}`,
    projectGroup: `project:${project.id}`,
    workstreamGroup: workstreamId == null
      ? `workstream:${project.id}:project`
      : `workstream:${project.id}:${workstreamId}`,
    businessAccountGroup: project.baId == null ? "ba:none" : `ba:${project.baId}`,
    employeeId: employee.id,
    projectId: project.id,
    workstreamId,
    terminalGroup: false,
  };
  rows.set(key, row);
  return row;
}

function commentCellTemplate(
  createElement: Parameters<CellTemplate>[0],
  row: AnalyticsGridRow,
  period: number,
  value: unknown,
) {
  const count = commentCounts.value.get(commentCellKey(period, row.employeeId, row.projectId,
    row.workstreamId)) ?? 0;
  const stopPropagation = (event: Event) => event.stopPropagation();
  const stopPointer = (event: Event) => {
    event.preventDefault();
    event.stopPropagation();
  };
  const button = createElement(
    "button",
    {
      type: "button",
      class: ["resource-allocation-comment-button", count > 0 ? "has-comments text-primary" : ""],
      title: t("Открыть комментарии"),
      "aria-label": t("Открыть комментарии: {count}", { count }),
      "data-testid": `resource-allocation-comments-${row.id}-${period}`,
      style: {
        background: "transparent",
        border: "0",
        color: "inherit",
        cursor: "pointer",
        font: "inherit",
        fontSize: "11px",
        opacity: count > 0 ? "1" : "0",
        padding: "0",
      },
      onPointerDown: stopPointer,
      onMouseDown: stopPointer,
      onTouchStart: stopPropagation,
      onKeyDown: stopPropagation,
      onMouseEnter: (event: MouseEvent) => setCommentButtonVisible(event.currentTarget, true, count),
      onMouseLeave: (event: MouseEvent) => setCommentButtonVisible(event.currentTarget, false, count),
      onFocus: (event: FocusEvent) => setCommentButtonVisible(event.currentTarget, true, count),
      onBlur: (event: FocusEvent) => setCommentButtonVisible(event.currentTarget, false, count),
      onDblClick: stopPropagation,
      onClick: (event: MouseEvent) => {
        event.stopPropagation();
        openComments(row, period, event.currentTarget as HTMLElement);
      },
    },
    [
      createElement("i", {
        class: count > 0 ? "mdi mdi-comment-text-outline" : "mdi mdi-comment-plus-outline",
        "aria-hidden": "true",
      }),
    ],
  );
  return allocationValueCell(createElement, value, {
    class: terminalCellClass,
    "data-testid": `resource-allocation-analytics-cell-${row.id}-${period}`,
    onMouseEnter: (event: MouseEvent) => setCellCommentButtonVisible(event.currentTarget, true, count),
    onMouseLeave: (event: MouseEvent) => setCellCommentButtonVisible(event.currentTarget, false, count),
  }, button);
}

function setCellCommentButtonVisible(target: EventTarget | null, visible: boolean, count: number): void {
  setCommentButtonVisible((target as HTMLElement | null)
    ?.querySelector<HTMLElement>(".resource-allocation-comment-button") ?? null, visible, count);
}

function setCommentButtonVisible(target: EventTarget | null, visible: boolean, count: number): void {
  if (target && count === 0) (target as HTMLElement).style.opacity = visible ? "1" : "0";
}

function allocationValueCell(
  createElement: Parameters<CellTemplate>[0],
  value: unknown,
  attributes: Record<string, unknown> = {},
  action?: ReturnType<Parameters<CellTemplate>[0]>,
) {
  return createElement("div", {
    ...attributes,
    style: {
      alignItems: "center",
      boxSizing: "border-box",
      display: "flex",
      height: "100%",
      justifyContent: "center",
      padding: "0 24px",
      position: "relative",
      width: "100%",
      ...(attributes.style as Record<string, unknown> | undefined),
    },
  }, [
    createElement("span", null, formatAllocationValue(value)),
    createElement("span", {
      style: {
        alignItems: "center",
        display: "flex",
        height: "100%",
        justifyContent: "center",
        position: "absolute",
        right: "2px",
        top: "0",
        width: "20px",
      },
    }, action == null ? [] : [action]),
  ]);
}

function openComments(row: AnalyticsGridRow, period: number, target: HTMLElement): void {
  const employee = employeesById.value.get(row.employeeId);
  const project = projectsById.value.get(row.projectId);
  if (!employee || !project) return;
  commentCell.value = {
    period,
    employeeId: row.employeeId,
    projectId: row.projectId,
    workstreamId: row.workstreamId,
    employeeName: employee.displayName,
    projectName: project.name,
    workstreamName: row.workstreamId == null
      ? null
      : workstreamsById.value.get(row.workstreamId)?.displayName ?? null,
  };
  commentTarget.value = target;
  commentOpen.value = true;
}

function updateCommentCount(cell: ResourceAllocationCommentCellKey, count: number): void {
  const summary = commentSummary.value;
  if (!summary) return;
  const rows = summary.rows.map(row => ({ ...row, cells: [...row.cells] }));
  let row = rows.find(item => item.employee.id === cell.employeeId
    && item.project.id === cell.projectId
    && (item.workstream?.id ?? null) === cell.workstreamId);
  if (!row && count > 0) {
    const employee = employeesById.value.get(cell.employeeId);
    const project = projectsById.value.get(cell.projectId);
    if (!employee || !project) return;
    row = commentSummaryRow(employee, project, cell.workstreamId);
    rows.push(row);
  }
  if (!row) return;
  row.cells = row.cells.filter(item => item.period !== cell.period);
  row.cells.push({ period: cell.period, commentCount: count });
  commentSummary.value = {
    ...summary,
    rows,
  };
}

function handleCommentOpen(open: boolean): void {
  commentOpen.value = open;
  if (open || !commentSummary.value) return;
  commentSummary.value = {
    ...commentSummary.value,
    rows: commentSummary.value.rows
      .map(row => ({ ...row, cells: row.cells.filter(cell => cell.commentCount > 0) }))
      .filter(row => row.cells.length > 0),
  };
}

function commentSummaryRow(
  employee: ResourceAllocationEmployee,
  project: ResourceAllocationProject,
  workstreamId: number | null,
): ResourceAllocationCommentRow {
  return {
    employee,
    project: {
      id: project.id,
      name: project.name,
      departmentId: project.departmentId,
      departmentName: project.departmentName,
      baId: project.baId,
      baName: project.baName,
    },
    workstream: workstreamId == null ? null : {
      id: workstreamId,
      displayName: workstreamsById.value.get(workstreamId)?.displayName ?? String(workstreamId),
    },
    cells: [],
  };
}

function commentCellKey(period: number, employeeId: number, projectId: number,
                         workstreamId: number | null): string {
  return `${period}:${employeeId}:${projectId}:${workstreamId ?? "project"}`;
}

function employeeDetailsButton(createElement: Parameters<CellTemplate>[0], employeeId: number) {
  const stopPropagation = (event: Event) => event.stopPropagation();
  return createElement("button", {
    type: "button",
    class: "mdi mdi-information-outline resource-allocation-employee-details-button",
    title: t("Открыть карточку сотрудника"),
    "aria-label": t("Открыть карточку сотрудника"),
    "data-testid": `resource-allocation-open-employee-${employeeId}`,
    style: {
      background: "transparent",
      border: "0",
      color: "inherit",
      cursor: "pointer",
      fontSize: "14px",
      width: "24px",
      height: "24px",
      marginRight: "4px",
      flexShrink: "0",
    },
    onPointerDown: stopPropagation,
    onMouseDown: stopPropagation,
    onTouchStart: stopPropagation,
    onKeyDown: stopPropagation,
    onKeyUp: stopPropagation,
    onDblClick: stopPropagation,
    onClick: (event: MouseEvent) => {
      event.stopPropagation();
      emit("open-employee", employeeId);
    },
  });
}

const groupCellTemplate: GroupCellTemplateFunc = (createElement, props, summaries) => {
  const path = String(props.model[PSEUDO_GROUP_ITEM_VALUE] ?? "");
  const summary = (summaries as Map<string, GroupSummary>).get(path);
  if (props.group.isLabelColumn) {
    const employeeGroup = path.split(",").at(-1);
    const employeeId = employeeGroup?.startsWith("employee:")
      ? Number(employeeGroup.slice("employee:".length))
      : null;
    if (summary?.terminalRowId) {
      return createElement(
        "span",
        {
          class: terminalCellClass,
          "data-testid": `resource-allocation-analytics-row-${summary.terminalRowId}`,
          style: {
            display: "block",
            fontWeight: "normal",
            height: "100%",
            paddingLeft: `${props.group.depth * hierarchyIndent}px`,
            width: "100%",
          },
        },
        [
          ...(employeeId == null ? [] : [employeeDetailsButton(createElement, employeeId)]),
          createElement("span", null, summary.label),
        ],
      );
    }
    const projectCount = summary?.projects.size ?? 0;
    const workstreamCount = summary?.workstreams.size ?? 0;
    const dimensionCount = summary?.dimensions.size ?? 0;
    const pathDepth = path.split(",").length;
    const detail = mode.value === "employees" && pathDepth === 1 && projectCount > 0
        ? projectCount === 1
          ? [...summary!.projects][0]
          : t("projectCounts", { plural: projectCount, n: projectCount })
        : pathDepth === 2 && workstreamCount > 0
          ? dimensionCount === 1
            ? [...summary!.workstreams][0]
            : t("workstreamCounts", { plural: dimensionCount, n: dimensionCount })
          : "";
    const expandButton = createElement(
      "button",
      {
        type: "button",
        class: groupCellClass,
        onClick: props.group.onExpand,
        "aria-expanded": String(props.group.expanded),
        "data-testid": `resource-allocation-group-${path}-label`,
        style: {
          alignItems: "center",
          background: "transparent",
          border: "0",
          color: "inherit",
          cursor: "pointer",
          display: "flex",
          font: "inherit",
          height: "100%",
          paddingLeft: `${props.group.depth * hierarchyIndent}px`,
          width: "100%",
        },
      },
      [
        createElement(
          "span",
          null,
          `${props.group.expanded ? "▾" : "▸"} ${summary?.label ?? props.group.name}`,
        ),
        detail
          ? createElement(
              "span",
              {
                style: {
                  color: "rgba(var(--v-theme-on-surface), var(--v-medium-emphasis-opacity))",
                  fontSize: "11px",
                  marginLeft: "8px",
                },
              },
              detail,
            )
          : null,
      ],
    );
    // Keep the profile action beside, never inside, the group expansion button.
    return employeeId == null ? expandButton : createElement(
      "div",
      { class: groupCellClass, style: { display: "flex", alignItems: "center", height: "100%" } },
      [employeeDetailsButton(createElement, employeeId), expandButton],
    );
  }

  const period = props.prop === "yearTotal" ? "year-total" : Number(String(props.prop).replace("month_", ""));
  const value = period === "year-total" ? summary?.yearTotal : summary?.months.get(period);
  if (summary?.terminalRowId) {
    const row = allocationRowsById.value.get(summary.terminalRowId);
    if (period !== "year-total" && row) {
      return commentCellTemplate(createElement, row, period, value);
    }
    return allocationValueCell(createElement, value, {
        class: terminalCellClass,
        "data-testid": `resource-allocation-analytics-cell-${summary.terminalRowId}-${period}`,
        style: {
          fontSize: "12px",
          fontWeight: "normal",
        },
    });
  }
  return allocationValueCell(createElement, value, {
    class: groupCellClass,
    ...(value != null
      ? {
          "data-testid": `resource-allocation-group-${path}-${period}`,
          style: {
            color: "rgb(var(--v-theme-primary))",
            fontSize: "12px",
            fontWeight: "600",
          },
        }
      : {}),
  });
};

function toDisplayValue(percent: number): number {
  return displayUnit.value === "personMonths" ? percent / 100 : percent;
}

function formatAllocationValue(value: unknown): string {
  if (value == null || value === "") return "";
  const number = Number(value).toLocaleString("ru-RU", { maximumFractionDigits: 2 });
  return displayUnit.value === "percent" ? `${number}%` : number;
}

function formatMonth(period: number): string {
  return ReportPeriod.fromPeriodId(period).toString().replace(/\s+\d{4}$/, "");
}

function filterLabel(item: unknown): string {
  if (typeof item === "string") return item;
  if (item && typeof item === "object" && "title" in item && typeof item.title === "string") {
    return item.title;
  }
  if (item && typeof item === "object" && "name" in item && typeof item.name === "string") {
    return item.name;
  }
  return "";
}

function changeYear(delta: number): void {
  year.value += delta;
}

function goToCurrentYear(): void {
  year.value = currentYear;
}

function yearFromQuery(value: unknown): number {
  const parsed = typeof value === "string" && /^\d{4}$/.test(value) ? Number(value) : NaN;
  return Number.isInteger(parsed) && parsed > 0 ? parsed : currentYear;
}

function unitFromQuery(value: unknown): ResourceAllocationDisplayUnit {
  return value === "percent" ? "percent" : "personMonths";
}

function syncQuery(): void {
  if (route.name !== "resource-allocations-analytics") return;
  const query = { ...route.query, year: String(year.value), unit: displayUnit.value };
  if (route.query.year === query.year && route.query.unit === query.unit) return;
  void router.replace({ query }).catch(() => undefined);
}

function openPeriodDialog(): void {
  error.value = "";
  closedPeriodsDraft.value = [...closedPeriods.value].sort();
  periodDialog.value = true;
}

async function exportToExcel(): Promise<void> {
  if (exporting.value) return;
  exporting.value = true;
  error.value = "";
  try {
    await exportResourceAllocationAnalytics(year.value, displayUnit.value);
  } catch (exportError) {
    error.value = errorUtils.shortMessage(exportError);
  } finally {
    exporting.value = false;
  }
}

async function savePeriodSelection(): Promise<void> {
  periodUpdating.value = true;
  error.value = "";
  try {
    const saved = await saveClosedResourceAllocationPeriods(year.value, closedPeriodsDraft.value);
    closedPeriods.value = new Set(saved);
    periodDialog.value = false;
  } catch (updateError) {
    error.value = errorUtils.shortMessage(updateError);
  } finally {
    periodUpdating.value = false;
  }
}
</script>

<style scoped>
:deep(.rgCell:has(.resource-allocation-terminal-cell)) {
  background-color: rgb(var(--v-theme-surface));
}

:deep(.rgCell:has(.resource-allocation-group-cell)) {
  background-color: rgba(var(--v-theme-on-surface), 0.04);
}

</style>
