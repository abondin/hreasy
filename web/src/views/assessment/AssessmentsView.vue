<template>
  <v-container class="py-6" fluid data-testid="assessments-view">
    <v-alert
      v-if="!canAccess"
      type="warning"
      variant="tonal"
      class="mb-4"
      data-testid="assessments-no-access"
    >
      {{ t("Недостаточно прав") }}
    </v-alert>

        <v-card v-else data-testid="assessments-card">
      <v-snackbar v-model="exportCompleted" timeout="5000">
        {{ t("Экспорт успешно завершён. Файл скачен.") }}
      </v-snackbar>
      <v-card-text class="pt-4 pb-2">
        <AdaptiveFilterBar
          :items="filterBarItems"
          :has-right-actions="canExport"
        >
          <template #left-actions>
            <table-toolbar-actions
              :disabled="loading"
              show-refresh
              :refresh-label="t('Обновить данные')"
              @refresh="load"
            />
          </template>

          <template #filter-search>
            <v-text-field
              v-model="filter.search"
              :label="t('Поиск')"
              prepend-inner-icon="mdi-magnify"
              clearable
              variant="outlined"
              density="compact"
              hide-details
              data-testid="assessments-filter-search"
            />
          </template>

          <template #filter-ba>
            <v-autocomplete
              v-model="filter.selectedBas"
              :items="businessAccounts.filter((item) => item.active !== false)"
              item-title="name"
              item-value="id"
              :label="t('Бизнес аккаунт')"
              multiple
              clearable
              variant="outlined"
              density="compact"
              hide-details
              data-testid="assessments-filter-ba"
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="filter.selectedBas.length"
                  :label="getFilterSelectionLabel(item)"
                />
              </template>
            </v-autocomplete>
          </template>

          <template #filter-project>
            <v-autocomplete
              v-model="filter.selectedProjects"
              :items="projects.filter((item) => item.active !== false)"
              item-title="name"
              item-value="id"
              :label="t('Текущий проект')"
              multiple
              clearable
              variant="outlined"
              density="compact"
              hide-details
              data-testid="assessments-filter-project"
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="filter.selectedProjects.length"
                  :label="getFilterSelectionLabel(item)"
                />
              </template>
            </v-autocomplete>
          </template>

          <template #right-actions>
            <table-toolbar-actions
              v-if="canExport"
              :disabled="loading"
              show-export
              :export-label="t('Экспорт в Excel')"
              @export="runExport"
            />
          </template>
        </AdaptiveFilterBar>


        <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          class="mb-4"
          data-testid="assessments-error"
        >
          {{ error }}
        </v-alert>

        <HREasyTableBase
          table-class="assessments-table text-truncate"
          :headers="headers"
          :items="filteredItems"
          :loading="loading"
          :loading-text="t('Загрузка_данных')"
          :no-data-text="t('Отсутствуют данные')"
          fixed-header
          hover
          density="compact"
          :sort-by="[{ key: 'lastAssessmentDate', order: 'desc' }]"
          item-key="employeeId"
          height="calc(100vh - 280px)"
          :row-props="rowProps"
          data-testid="assessments-table"
          @click:row="onRowClick"
        >
          <template #[`item.displayName`]="{ item }">
            <router-link
              :to="{ name: 'employee-assessments', params: { employeeId: String(item.employeeId) } }"
              @click.stop
            >
              {{ item.displayName }}
            </router-link>
          </template>
          <template #[`item.currentProject`]="{ item }">
            <span v-if="item.currentProject">
              {{ item.currentProject.name }}
              <span v-if="item.currentProject.role">({{ item.currentProject.role }})</span>
            </span>
          </template>
          <template #[`item.employeeDateOfEmployment`]="{ item }">
            {{ formatDate(item.employeeDateOfEmployment) }}
          </template>
          <template #[`item.lastAssessmentCompletedDate`]="{ item }">
            {{ formatDate(item.lastAssessmentCompletedDate) }}
          </template>
          <template #[`item.lastAssessmentDate`]="{ item }">
            {{ formatDate(item.lastAssessmentDate) }}
          </template>
        </HREasyTableBase>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { formatDate } from "@/lib/datetime";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import { extractDataTableRow } from "@/lib/data-table";
import { errorUtils } from "@/lib/errors";
import { usePermissions } from "@/lib/permissions";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import { fetchBusinessAccounts, type DictItem } from "@/services/dict.service";
import { fetchProjects, type ProjectDictDto } from "@/services/projects.service";
import {
  exportAssessments,
  fetchAssessmentsSummary,
  type AssessmentEmployeeSummary,
} from "@/services/assessment.service";

const { t } = useI18n();
const router = useRouter();
const permissions = usePermissions();

const loading = ref(false);
const error = ref("");
const exportCompleted = ref(false);
const items = ref<AssessmentEmployeeSummary[]>([]);
const businessAccounts = ref<DictItem[]>([]);
const projects = ref<ProjectDictDto[]>([]);

const filter = reactive({
  search: "",
  selectedBas: [] as number[],
  selectedProjects: [] as number[],
});

const canAccess = computed(() => permissions.canCreateAssessments());
const canExport = computed(() => permissions.canExportAssessments());

const headers = computed(() => [
  { title: t("Сотрудник"), key: "displayName", width: "260px" },
  { title: t("Бизнес аккаунт"), key: "ba.name", width: "220px" },
  { title: t("Проект"), key: "currentProject", width: "220px" },
  { title: t("Дата устройства"), key: "employeeDateOfEmployment", width: "150px" },
  { title: t("Последний ассессмент запланирован"), key: "lastAssessmentDate", width: "180px" },
  { title: t("Последний ассессмент завершен"), key: "lastAssessmentCompletedDate", width: "180px" },
  { title: t("Дней без ассессмента"), key: "daysWithoutAssessment", width: "140px" },
]);

const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: filter.search.trim().length > 0, grow: true },
  { id: "ba", minWidth: 320, active: filter.selectedBas.length > 0 },
  { id: "project", minWidth: 320, active: filter.selectedProjects.length > 0 },
]);

function getFilterSelectionLabel(item: unknown): string {
  if (typeof item === "string") {
    return item;
  }
  if (item && typeof item === "object" && "name" in item && typeof item.name === "string") {
    return item.name;
  }
  return "";
}

const filteredItems = computed(() => {
  const search = filter.search.toLowerCase().trim();

  return items.value.filter((item) => {
    if (search && !item.displayName.toLowerCase().includes(search)) {
      return false;
    }

    if (filter.selectedBas.length > 0) {
      const baId = item.ba?.id;
      if (!baId || !filter.selectedBas.includes(baId)) {
        return false;
      }
    }

    if (filter.selectedProjects.length > 0) {
      const projectId = item.currentProject?.id;
      if (!projectId || !filter.selectedProjects.includes(projectId)) {
        return false;
      }
    }

    return true;
  });
});

function rowProps() {
  return { class: "cursor-pointer" };
}

function onRowClick(_event: Event, payload: unknown): void {
  const row = extractDataTableRow<AssessmentEmployeeSummary>(payload);
  if (!row?.employeeId) {
    return;
  }

  router.push({ name: "employee-assessments", params: { employeeId: String(row.employeeId) } }).catch(() => undefined);
}

onMounted(() => {
  load().catch(() => undefined);
});

async function load(): Promise<void> {
  if (!canAccess.value) {
    return;
  }

  loading.value = true;
  error.value = "";
  try {
    const [summary, bas, projectItems] = await Promise.all([
      fetchAssessmentsSummary(),
      fetchBusinessAccounts(),
      fetchProjects(),
    ]);
    items.value = summary;
    businessAccounts.value = bas;
    projects.value = projectItems;
  } catch (err: unknown) {
    error.value = errorUtils.shortMessage(err);
  } finally {
    loading.value = false;
  }
}

async function runExport(): Promise<void> {
  loading.value = true;
  error.value = "";
  try {
    await exportAssessments();
    exportCompleted.value = true;
  } catch (err: unknown) {
    error.value = errorUtils.shortMessage(err);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.assessments-table :deep(tbody tr:hover) {
  cursor: pointer;
}
</style>
