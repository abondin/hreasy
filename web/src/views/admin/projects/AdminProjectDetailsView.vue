<template>
  <admin-detail-page-layout
    :back-to="{ name: 'admin-projects' }"
    :back-label="t('\u0412\u0441\u0435 \u043F\u0440\u043E\u0435\u043A\u0442\u044B')"
    :title="project?.name ?? t('\u041E\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044E\u0442 \u0434\u0430\u043D\u043D\u044B\u0435')"
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
          {{ t("\u041D\u0435 \u0437\u0430\u0434\u0430\u043D") }}
        </div>
      </div>
    </template>

    <admin-managers-table
      v-if="project"
      :selected-object="{ id: project.id, type: 'project' }"
      :title="t('\u041C\u0435\u043D\u0435\u0434\u0436\u0435\u0440\u044B \u043F\u0440\u043E\u0435\u043A\u0442\u0430')"
      :editable="permissions.canAdminProjects()"
      mode="compact"
      test-id="admin-project-managers"
    />

    <v-dialog v-model="editDialog" persistent width="96vw" max-width="960">
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
const projectSubtitle = computed(() => {
  if (!project.value) {
    return "";
  }

  return [
    project.value.department?.name,
    project.value.businessAccount?.name,
  ].filter(Boolean).join(" \u2022 ");
});

const summaryItems = computed<AdminDetailSummaryItem[]>(() => {
  if (!project.value) {
    return [];
  }

  return [
    { label: t("\u041D\u0430\u0438\u043C\u0435\u043D\u043E\u0432\u0430\u043D\u0438\u0435"), value: project.value.name },
    { label: t("\u041E\u0442\u0434\u0435\u043B"), value: project.value.department?.name ?? t("\u041D\u0435 \u0437\u0430\u0434\u0430\u043D") },
    { label: t("\u0411\u0438\u0437\u043D\u0435\u0441 \u0430\u043A\u043A\u0430\u0443\u043D\u0442"), value: project.value.businessAccount?.name ?? t("\u041D\u0435 \u0437\u0430\u0434\u0430\u043D") },
    { label: t("\u0417\u0430\u043A\u0430\u0437\u0447\u0438\u043A"), value: project.value.customer ?? t("\u041D\u0435 \u0437\u0430\u0434\u0430\u043D") },
    { label: t("\u041D\u0430\u0447\u0430\u043B\u043E"), value: formatPlanActual(project.value.planStartDate, project.value.startDate) },
    { label: t("\u041E\u043A\u043E\u043D\u0447\u0430\u043D\u0438\u0435"), value: formatPlanActual(project.value.planEndDate, project.value.endDate) },
  ];
});

async function load(): Promise<void> {
  const projectId = Number(route.params.projectId);
  if (!projectId) {
    error.value = t("\u041E\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044E\u0442 \u0434\u0430\u043D\u043D\u044B\u0435");
    return;
  }

  loading.value = true;
  error.value = "";
  try {
    const [projectInfo, departmentItems, baItems] = await Promise.all([
      fetchAdminProject(projectId),
      fetchDepartments(),
      fetchBusinessAccounts(),
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
    parts.push(`${actualFormatted} (${t("\u0444\u0430\u043A\u0442")})`);
  }
  const planFormatted = formatDate(plan);
  if (planFormatted) {
    parts.push(`${planFormatted} (${t("\u043F\u043B\u0430\u043D")})`);
  }
  if (parts.length === 0) {
    return t("\u041D\u0435 \u0437\u0430\u0434\u0430\u043D");
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
