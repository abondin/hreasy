<template>
  <div
    class="mt-4 d-flex flex-column flex-grow-1 min-h-0"
    data-testid="resource-allocations-analytics-view"
  >
    <TablePageCard test-id="resource-allocations-analytics-card">
      <AdaptiveFilterBar :items="toolbarFilterItems" class="mb-4">
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
          <v-text-field
            :model-value="search"
            clearable
            hide-details
            density="compact"
            variant="outlined"
            prepend-inner-icon="mdi-magnify"
            :label="t('Поиск по сотруднику или проекту')"
            data-testid="resource-allocations-analytics-search"
            @update:model-value="search = normalizeSearchInput($event)"
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
import TablePageCard from "@/components/shared/TablePageCard.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import { errorUtils } from "@/lib/errors";
import { normalizeSearchInput } from "@/lib/search";
import { ReportPeriod } from "@/services/overtime.service";
import {
  fetchResourceAllocationAnalytics,
  type ResourceAllocationAnalytics,
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
  businessAccountGroup: string;
  employeeId: number;
  projectId: number;
  [key: string]: string | number;
}

interface GroupSummary {
  label: string;
  months: Map<number, number>;
  projects: Set<string>;
}

const { t } = useI18n();
const currentYear = Math.trunc(ReportPeriod.currentPeriod().id / 100);
const year = ref(currentYear);
const mode = ref<AnalyticsMode>("projects");
const sheet = shallowRef<ResourceAllocationAnalytics | null>(null);
const businessAccountIds = ref<number[]>([]);
const projectIds = ref<number[]>([]);
const search = ref("");
const loading = ref(false);
const error = ref("");
let activated = false;

const grouping = computed<GroupingOptions>(() => ({
  props:
    mode.value === "projects"
      ? ["businessAccountGroup", "projectGroup"]
      : ["employeeGroup"],
  expandedAll: mode.value === "projects",
  groupCellTemplate,
}));
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
const normalizedSearch = computed(() => search.value.trim().toLocaleLowerCase());
const gridRows = computed<AnalyticsGridRow[]>(() => {
  const rows = new Map<string, AnalyticsGridRow>();
  for (const allocation of sheet.value?.allocations ?? []) {
    if (allocation.percent <= 0 || !visibleProjectIds.value.has(allocation.projectId)) continue;
    const employee = employeesById.value.get(allocation.employeeId);
    const project = projectsById.value.get(allocation.projectId);
    if (!employee || !project) continue;
    if (
      normalizedSearch.value &&
      !employee.displayName.toLocaleLowerCase().includes(normalizedSearch.value) &&
      !project.name.toLocaleLowerCase().includes(normalizedSearch.value)
    ) continue;
    const key = `${employee.id}:${project.id}`;
    const row = rows.get(key) ?? {
      id: key,
      entity: mode.value === "employees" ? project.name : employee.displayName,
      employee: employee.displayName,
      project: project.name,
      businessAccount: project.baName ?? t("Без бизнес-аккаунта"),
      employeeGroup: `employee:${employee.id}`,
      projectGroup: `project:${project.id}`,
      businessAccountGroup: project.baId == null ? "ba:none" : `ba:${project.baId}`,
      employeeId: employee.id,
      projectId: project.id,
    };
    row[monthProp(allocation.period)] = allocation.percent;
    rows.set(key, row);
  }
  return [...rows.values()].sort((left, right) => {
    const leftGroup = mode.value === "employees" ? left.employee : left.project;
    const rightGroup = mode.value === "employees" ? right.employee : right.project;
    return leftGroup.localeCompare(rightGroup) || left.entity.localeCompare(right.entity);
  });
});
const groupSummaries = computed(() => {
  const summaries = new Map<string, GroupSummary>();
  for (const row of gridRows.value) {
    const groups =
      mode.value === "projects"
        ? [
            [row.businessAccountGroup, row.businessAccount],
            [`${row.businessAccountGroup},${row.projectGroup}`, row.project],
          ]
        : [[row.employeeGroup, row.employee]];
    for (const [path, label] of groups) {
      const summary = summaries.get(path) ?? {
        label,
        months: new Map<number, number>(),
        projects: new Set<string>(),
      };
      if (mode.value === "employees") summary.projects.add(row.project);
      for (let month = 0; month < 12; month += 1) {
        const period = year.value * 100 + month;
        const value = Number(row[monthProp(period)] ?? 0);
        if (value) summary.months.set(period, (summary.months.get(period) ?? 0) + value);
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
    cellProperties: ({ model }) => ({
      "data-testid": `resource-allocation-analytics-row-${(model as AnalyticsGridRow).id}`,
    }),
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
      cellProperties: ({ model }) => ({
        "data-testid": `resource-allocation-analytics-cell-${(model as AnalyticsGridRow).id}-${period}`,
      }),
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
    sheet.value = await fetchResourceAllocationAnalytics(year.value);
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

function monthProp(period: number): string {
  return `month_${period}`;
}

const groupCellTemplate: GroupCellTemplateFunc = (createElement, props, summaries) => {
  const path = String(props.model[PSEUDO_GROUP_ITEM_VALUE] ?? "");
  const summary = (summaries as Map<string, GroupSummary>).get(path);
  if (props.group.isLabelColumn) {
    const projectCount = summary?.projects.size ?? 0;
    const detail =
      mode.value === "employees" && projectCount > 0
        ? projectCount === 1
          ? [...summary!.projects][0]
          : t("{count} проектов", { count: projectCount })
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
          paddingLeft: `${props.group.depth * 16}px`,
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
  return value
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
</script>
