<template>
  <TableFirstPageLayout test-id="resource-allocations-view" @activated="reload">
    <TablePageCard test-id="resource-allocations-card">
      <div class="d-flex flex-wrap align-center ga-3 mb-4">
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
        <v-btn-toggle v-model="projectScope" mandatory density="compact" color="primary">
          <v-btn value="mine" data-testid="resource-allocations-scope-mine">{{ t("Мои проекты") }}</v-btn>
          <v-btn value="all" data-testid="resource-allocations-scope-all">{{ t("Все проекты") }}</v-btn>
        </v-btn-toggle>
        <v-text-field
          v-model="search"
          density="compact"
          clearable
          hide-details
          variant="outlined"
          prepend-inner-icon="mdi-magnify"
          :label="t('Поиск')"
          class="resource-allocation-search"
        />
        <v-spacer />
        <v-btn
          color="primary"
          prepend-icon="mdi-content-save"
          :disabled="edits.size === 0 || loading"
          :loading="saving"
          data-testid="resource-allocations-save"
          @click="save"
        >
          {{ t("Сохранить") }}
        </v-btn>
      </div>

      <v-alert v-if="error" type="error" variant="tonal" class="mb-3" closable @click:close="error = ''">
        {{ error }}
      </v-alert>
      <v-alert v-if="saved" type="success" variant="tonal" class="mb-3" closable @click:close="saved = false">
        {{ t("Изменения сохранены") }}
      </v-alert>

      <template #table>
        <HREasyTableBase
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
          data-testid="resource-allocations-table"
        >
          <template #[`item.total`]="{ item }">
            <v-chip :color="totalColor(item.total)" size="small" variant="tonal">
              {{ item.total }}%
            </v-chip>
          </template>
          <template
            v-for="project in visibleProjects"
            :key="project.id"
            #[`item.project_${project.id}`]="{ item }"
          >
            <input
              class="resource-allocation-input"
              type="number"
              min="0"
              max="1000"
              step="1"
              :value="cellValue(item.id, project.id) || ''"
              :disabled="!project.editable || saving"
              :aria-label="`${item.employee} — ${project.name}`"
              :data-testid="`resource-allocation-cell-${item.id}-${project.id}`"
              @change="setCell(item.id, project.id, $event)"
            />
          </template>
        </HREasyTableBase>
      </template>
    </TablePageCard>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import PeriodSwitcherControl from "@/components/shared/PeriodSwitcherControl.vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";
import { errorUtils } from "@/lib/errors";
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

const { t } = useI18n();
const currentPeriodId = ReportPeriod.currentPeriod().id;
const periodId = ref(currentPeriodId);
const sheet = ref<ResourceAllocationSheet | null>(null);
const edits = ref(new Map<string, number>());
const search = ref("");
const projectScope = ref<"mine" | "all">("mine");
const loading = ref(false);
const saving = ref(false);
const saved = ref(false);
const error = ref("");

const periodLabel = computed(() => ReportPeriod.fromPeriodId(periodId.value).toString());
const initialValues = computed(() => new Map(
  (sheet.value?.allocations ?? []).map(value => [cellKey(value.employeeId, value.projectId), value.percent]),
));
const visibleProjects = computed(() => (sheet.value?.projects ?? [])
  .filter(project => projectScope.value === "all" || project.editable)
  .sort((left, right) => Number(right.editable) - Number(left.editable)
    || Number(right.active) - Number(left.active)
    || left.name.localeCompare(right.name)));
const headers = computed(() => [
  { title: t("Сотрудник"), key: "employee", width: "240px", fixed: true },
  { title: t("Суммарная аллокация"), key: "total", width: "150px", fixed: true },
  ...visibleProjects.value.map(project => ({
    title: project.name,
    key: `project_${project.id}`,
    width: "130px",
    sortable: false,
  })),
]);
const rows = computed<AllocationRow[]>(() => (sheet.value?.employees ?? [])
  .filter(employee => employee.displayName.toLocaleLowerCase().includes(search.value.toLocaleLowerCase()))
  .map(employee => ({
    id: employee.id,
    employee: employee.displayName,
    total: (sheet.value?.projects ?? []).reduce(
      (sum, project) => sum + cellValue(employee.id, project.id),
      0,
    ),
  })));

onMounted(load);

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  saved.value = false;
  try {
    sheet.value = await fetchResourceAllocations(periodId.value);
    edits.value.clear();
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
  const key = cellKey(employeeId, projectId);
  return edits.value.get(key) ?? initialValues.value.get(key) ?? 0;
}

function setCell(employeeId: number, projectId: number, event: Event): void {
  const input = event.target as HTMLInputElement;
  const percent = Math.min(1000, Math.max(0, Math.round(Number(input.value) || 0)));
  input.value = percent === 0 ? "" : String(percent);
  const key = cellKey(employeeId, projectId);
  if (percent === (initialValues.value.get(key) ?? 0)) {
    edits.value.delete(key);
  } else {
    edits.value.set(key, percent);
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
  max-width: 320px;
}

.resource-allocation-input {
  width: 92px;
  padding: 6px 8px;
  border: 1px solid rgb(var(--v-theme-outline));
  border-radius: 4px;
  text-align: right;
}

.resource-allocation-input:disabled {
  border-color: transparent;
  color: rgb(var(--v-theme-on-surface));
  opacity: 0.55;
}
</style>
