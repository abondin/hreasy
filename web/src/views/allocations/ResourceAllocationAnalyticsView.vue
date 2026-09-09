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
            show-refresh
            :disabled="loading"
            :refresh-label="t('Обновить данные')"
            @refresh="load"
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
          :stretch="true"
          :hide-attribution="true"
          theme="compact"
          class="h-100 w-100"
          data-testid="resource-allocations-table"
        />
      </template>
    </TablePageCard>

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
import { useI18n } from "vue-i18n";
import Grid from "@revolist/vue3-datagrid";
import {
  PSEUDO_GROUP_ITEM_VALUE,
  type ColumnRegular,
  type GroupCellTemplateFunc,
  type GroupingOptions,
} from "@revolist/revogrid";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import PeriodSwitcherControl from "@/components/shared/PeriodSwitcherControl.vue";
import SearchTextField from "@/components/shared/SearchTextField.vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import { errorUtils } from "@/lib/errors";
import { usePermissions } from "@/lib/permissions";
import { createSearchSettings, matchesSearch } from "@/lib/search";
import { ReportPeriod } from "@/services/overtime.service";
import {
  fetchClosedResourceAllocationPeriods,
  fetchResourceAllocationAnalytics,
  saveClosedResourceAllocationPeriods,
  type ResourceAllocationAnalytics,
  type ResourceAllocationEmployee,
} from "@/services/resource-allocation.service";

defineOptions({ name: "ResourceAllocationAnalyticsView" });

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
}

const { t } = useI18n();
const permissions = usePermissions();
const currentPeriod = ReportPeriod.currentPeriod().periodId();
const currentYear = Math.trunc(currentPeriod / 100);
const year = ref(currentYear);
const mode = ref<AnalyticsMode>("projects");
const sheet = shallowRef<ResourceAllocationAnalytics | null>(null);
const businessAccountIds = ref<number[]>([]);
const projectIds = ref<number[]>([]);
const search = ref("");
const searchSettings = ref(createSearchSettings());
const loading = ref(false);
const error = ref("");
const closedPeriods = ref(new Set<number>());
const closedPeriodsDraft = ref<number[]>([]);
const periodDialog = ref(false);
const periodUpdating = ref(false);
const hierarchyIndent = 16;
const terminalCellClass = "resource-allocation-terminal-cell";
let activated = false;

const toolbarFilterItems = computed(() => [
  { id: "mode", minWidth: 260 },
  { id: "ba", minWidth: 260, active: businessAccountIds.value.length > 0 },
  { id: "projects", minWidth: 280, active: projectIds.value.length > 0 },
  { id: "search", minWidth: 260, active: search.value.trim().length > 0, grow: true },
]);
const employeesById = computed(
  () => new Map((sheet.value?.employees ?? []).map((employee) => [employee.id, employee])),
);
const projectsById = computed(
  () => new Map((sheet.value?.projects ?? []).map((project) => [project.id, project])),
);
const workstreamsById = computed(
  () => new Map((sheet.value?.workstreams ?? []).map((workstream) => [workstream.id, workstream])),
);
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
      (sheet.value?.projects ?? [])
        .filter((project) => project.baId != null)
        .map((project) => [
          project.baId as number,
          { id: project.baId as number, name: project.baName ?? String(project.baId) },
        ]),
    ).values(),
  ].sort((left, right) => left.name.localeCompare(right.name)),
);
const availableProjects = computed(() =>
  (sheet.value?.projects ?? [])
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
const gridRows = computed<AnalyticsGridRow[]>(() => {
  const rows = new Map<string, AnalyticsGridRow>();
  for (const allocation of sheet.value?.allocations ?? []) {
    if (!visibleProjectIds.value.has(allocation.projectId)) continue;
    const employee = employeesById.value.get(allocation.employeeId);
    const project = projectsById.value.get(allocation.projectId);
    const workstream = allocation.workstreamId == null
      ? null
      : workstreamsById.value.get(allocation.workstreamId);
    if (!employee || !project) continue;
    if (!matchesSearch(search.value, [
      employee.displayName,
      employee.email,
      employee.currentProjectRole,
      project.name,
      workstream?.displayName,
    ], searchSettings.value)) continue;
    const key = `${employee.id}:${project.id}:${allocation.workstreamId ?? "project"}`;
    const employeeText = employeeLabel(employee);
    const row: AnalyticsGridRow = rows.get(key) ?? {
      id: key,
      entity: mode.value === "employees"
        ? allocation.workstreamId == null
          ? t("Без направления")
          : workstream?.displayName ?? String(allocation.workstreamId)
        : employeeText,
      employee: employeeText,
      project: project.name,
      businessAccount: project.baName ?? t("Без бизнес-аккаунта"),
      employeeGroup: `employee:${employee.id}`,
      projectGroup: `project:${project.id}`,
      workstreamGroup: allocation.workstreamId == null
        ? `workstream:${project.id}:project`
        : `workstream:${project.id}:${allocation.workstreamId}`,
      businessAccountGroup: project.baId == null ? "ba:none" : `ba:${project.baId}`,
      employeeId: employee.id,
      projectId: project.id,
      workstreamId: allocation.workstreamId ?? null,
      terminalGroup: false,
    };
    row[monthProp(allocation.period)] = Number(row[monthProp(allocation.period)] ?? 0) + allocation.percent;
    rows.set(key, row);
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
const grouping = computed<GroupingOptions>(() => {
  const rows = gridRows.value;
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
  for (const row of gridRows.value) {
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
      for (let month = 0; month < 12; month += 1) {
        const period = year.value * 100 + month;
        const value = row[monthProp(period)];
        if (value != null) summary.months.set(period, (summary.months.get(period) ?? 0) + Number(value));
      }
      summaries.set(path, summary);
    }
  }
  return summaries;
});
const gridColumns = computed<ColumnRegular[]>(() => [
  {
    name: mode.value === "employees" ? t("Проект") : t("Сотрудник"),
    prop: "entity",
    size: 520,
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
      String(props.value ?? ""),
    ),
  },
  ...Array.from({ length: 12 }, (_, month) => {
    const period = year.value * 100 + month;
    return {
      name: formatMonth(period),
      prop: monthProp(period),
      size: 110,
      minSize: 110,
      maxSize: 110,
      sortable: false,
      readonly: true,
      cellTemplate: (createElement, props) => createElement(
        "span",
        {
          class: (props.model as AnalyticsGridRow).terminalGroup ? undefined : terminalCellClass,
          "data-testid": `resource-allocation-analytics-cell-${(props.model as AnalyticsGridRow).id}-${period}`,
        },
        String(props.value ?? ""),
      ),
    } satisfies ColumnRegular;
  }),
]);

watch(availableProjects, (projects) => {
  const availableIds = new Set(projects.map((project) => project.id));
  projectIds.value = projectIds.value.filter((projectId) => availableIds.has(projectId));
});
onMounted(load);
onActivated(() => {
  if (activated) void load();
  activated = true;
});

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  try {
    const [analytics, periods] = await Promise.all([
      fetchResourceAllocationAnalytics(year.value),
      fetchClosedResourceAllocationPeriods(year.value),
    ]);
    sheet.value = analytics;
    closedPeriods.value = new Set(periods);
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
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

const groupCellTemplate: GroupCellTemplateFunc = (createElement, props, summaries) => {
  const path = String(props.model[PSEUDO_GROUP_ITEM_VALUE] ?? "");
  const summary = (summaries as Map<string, GroupSummary>).get(path);
  if (props.group.isLabelColumn) {
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
        summary.label,
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
    return createElement(
      "button",
      {
        type: "button",
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
  }

  const period = Number(String(props.prop).replace("month_", ""));
  const value = summary?.months.get(period);
  if (summary?.terminalRowId) {
    return createElement(
      "span",
      {
        class: terminalCellClass,
        "data-testid": `resource-allocation-analytics-cell-${summary.terminalRowId}-${period}`,
        style: {
          display: "block",
          fontSize: "12px",
          fontWeight: "normal",
          height: "100%",
          width: "100%",
        },
      },
      String(value ?? ""),
    );
  }
  return value != null
    ? createElement(
        "span",
        {
          "data-testid": `resource-allocation-group-${path}-${period}`,
          style: {
            color: "rgb(var(--v-theme-primary))",
            fontSize: "12px",
            fontWeight: "600",
          },
        },
        String(value),
      )
    : "";
};

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
  void load();
}

function goToCurrentYear(): void {
  year.value = currentYear;
  void load();
}

function openPeriodDialog(): void {
  error.value = "";
  closedPeriodsDraft.value = [...closedPeriods.value].sort();
  periodDialog.value = true;
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
  background-color: rgba(var(--v-theme-primary), 0.04);
}
</style>
