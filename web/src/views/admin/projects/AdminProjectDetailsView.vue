<template>
  <admin-detail-page-layout
    :back-to="{ name: 'admin-projects' }"
    :back-label="t('Все проекты')"
    :title="project?.name ?? t('Отсутствуют данные')"
    :subtitle="projectSubtitle"
    :error="error"
    :show-primary-card="Boolean(project)"
    test-id="admin-project-details-view"
    card-test-id="admin-project-details-card"
  >
    <template #leading-actions>
      <v-btn
        icon="mdi-refresh"
        variant="text"
        :loading="loading"
        data-testid="admin-project-details-refresh"
        @click="load"
      />
    </template>

    <template #trailing-actions>
      <v-btn
        icon="mdi-pencil"
        color="primary"
        variant="text"
        :disabled="loading"
        data-testid="admin-project-details-edit"
        @click="editDialog = true"
      />
    </template>

    <template #summary>
      <admin-detail-summary-card
        v-if="project"
        :items="summaryItems"
      />
    </template>

    <template #content>
      <div class="admin-detail-section h-100">
        <markdown-text-renderer
          v-if="project?.info"
          :content="project.info"
          class="project-details-markdown"
        />
        <div
          v-else
          class="text-body-2 text-medium-emphasis py-2"
        >
          {{ t("Не задан") }}
        </div>
      </div>
    </template>

    <template v-if="project?.workstreams.length" #details>
      <div class="text-h6 mb-3">{{ t("Направления работ") }}</div>
      <v-table density="compact">
        <thead>
          <tr>
            <th>{{ t("Наименование") }}</th>
            <th>{{ t("Внешний идентификатор") }}</th>
            <th>{{ t("Описание") }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="workstream in project.workstreams" :key="workstream.id">
            <td>{{ workstream.displayName }}</td>
            <td>{{ workstream.externalId ?? t("Не задан") }}</td>
            <td>{{ workstream.description ?? t("Не задан") }}</td>
          </tr>
        </tbody>
      </v-table>
    </template>

    <admin-managers-table
      v-if="project"
      :selected-object="{ id: project.id, type: 'project' }"
      :title="t('Менеджеры проекта')"
      :editable="permissions.canAdminProjects()"
      mode="compact"
      test-id="admin-project-managers"
    />

    <v-card v-if="project" class="pa-6" data-testid="admin-project-employees">
      <div class="d-flex align-center flex-wrap ga-3 mb-3">
        <div class="text-h6">{{ t("Сотрудники") }}</div>
        <SearchTextField
          v-model="employeeFilter.search"
          v-model:settings="employeeFilter.searchSettings"
          :label="t('Поиск')"
          test-id="admin-project-employees-search"
          class="ms-auto flex-grow-0 w-100"
          max-width="360"
        />
      </div>
      <v-alert v-if="employeesError" type="error" class="mb-3">
        {{ errorUtils.shortMessage(employeesError) }}
      </v-alert>
      <EmployeesTable
        :items="projectEmployees"
        :headers="employeeHeaders"
        :loading="employeesLoading"
        :table-height="360"
        @select-employee="selectedEmployeeId = $event.id"
      />
    </v-card>

    <EmployeeDetailsDialog
      v-if="selectedEmployeeId != null"
      :key="selectedEmployeeId"
      :employee-id="selectedEmployeeId"
      @employee-updated="reloadEmployees"
      @close="selectedEmployeeId = null"
    />

    <v-dialog v-model="editDialog" persistent scrollable width="96vw" max-width="960">
      <admin-project-form
        :input="project"
        :departments="departments"
        :business-accounts="businessAccounts"
        @close="editDialog = false"
        @saved="onSaved"
      />
    </v-dialog>
  </admin-detail-page-layout>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import AdminDetailPageLayout from "@/components/shared/AdminDetailPageLayout.vue";
import AdminDetailSummaryCard, { type AdminDetailSummaryItem } from "@/components/shared/AdminDetailSummaryCard.vue";
import MarkdownTextRenderer from "@/components/shared/MarkdownTextRenderer.vue";
import SearchTextField from "@/components/shared/SearchTextField.vue";
import { useEmployeesDirectory } from "@/composables/useEmployeesDirectory";
import EmployeesTable from "@/views/employees/components/EmployeesTable.vue";
import EmployeeDetailsDialog from "@/components/employee/EmployeeDetailsDialog.vue";
import { formatDate } from "@/lib/datetime";
import { errorUtils } from "@/lib/errors";
import { usePermissions } from "@/lib/permissions";
import {
  fetchBusinessAccounts,
  fetchDepartments,
  type DictItem,
} from "@/services/dict.service";
import { fetchAdminProject, type AdminProjectInfo } from "@/services/admin/admin-project.service";
import AdminProjectForm from "@/views/admin/projects/components/AdminProjectForm.vue";
import AdminManagersTable from "@/views/admin/managers/components/AdminManagersTable.vue";

const { t } = useI18n();
const permissions = usePermissions();
const route = useRoute();

const loading = ref(false);
const editDialog = ref(false);
const error = ref("");
const project = ref<AdminProjectInfo | null>(null);
const departments = ref<DictItem[]>([]);
const businessAccounts = ref<DictItem[]>([]);
const selectedEmployeeId = ref<number | null>(null);
const {
  filteredEmployees: projectEmployees,
  loading: employeesLoading,
  error: employeesError,
  filter: employeeFilter,
  reload: reloadEmployees,
} = useEmployeesDirectory();
const employeeHeaders = computed(() => [
  { title: t("ФИО"), key: "displayName" },
  ...(permissions.canViewEmplCurrentProjectRole()
    ? [{ title: t("Роль на проекте"), key: "currentProject.role" }]
    : []),
  { title: t("Отдел"), key: "department.name" },
  { title: t("E-mail"), key: "email" },
]);
const projectSubtitle = computed(() => {
  if (!project.value) {
    return "";
  }

  return [
    project.value.department?.name,
    project.value.businessAccount?.name,
  ].filter(Boolean).join(" • ");
});

const summaryItems = computed<AdminDetailSummaryItem[]>(() => {
  if (!project.value) {
    return [];
  }

  return [
    { label: t("Наименование"), value: project.value.name },
    { label: t("Внешний идентификатор"), value: project.value.externalId ?? t("Не задан") },
    { label: t("Отдел"), value: project.value.department?.name ?? t("Не задан") },
    { label: t("Бизнес аккаунт"), value: project.value.businessAccount?.name ?? t("Не задан") },
    { label: t("Заказчик"), value: project.value.customer ?? t("Не задан") },
    { label: t("Начало"), value: formatPlanActual(project.value.planStartDate, project.value.startDate) },
    { label: t("Окончание"), value: formatPlanActual(project.value.planEndDate, project.value.endDate) },
  ];
});

async function load(): Promise<void> {
  const projectId = Number(route.params.projectId);
  if (!projectId) {
    error.value = t("Отсутствуют данные");
    return;
  }

  loading.value = true;
  error.value = "";
  employeeFilter.value.projects = [projectId];
  try {
    const [projectInfo, departmentItems, baItems] = await Promise.all([
      fetchAdminProject(projectId),
      fetchDepartments(),
      fetchBusinessAccounts(),
      reloadEmployees(),
    ]);
    project.value = projectInfo;
    departments.value = departmentItems;
    businessAccounts.value = baItems;
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
    project.value = null;
  } finally {
    loading.value = false;
  }
}

function onSaved(): void {
  editDialog.value = false;
  void load();
}

function formatPlanActual(plan?: string, actual?: string): string {
  const parts: string[] = [];
  const actualFormatted = formatDate(actual);
  if (actualFormatted) {
    parts.push(`${actualFormatted} (${t("факт")})`);
  }
  const planFormatted = formatDate(plan);
  if (planFormatted) {
    parts.push(`${planFormatted} (${t("план")})`);
  }
  if (parts.length === 0) {
    return t("Не задан");
  }
  return parts.join(", ");
}

void load();
</script>

<style scoped>
.admin-detail-section {
  min-height: 100%;
}

.project-details-markdown {
  min-height: 100%;
}
</style>
