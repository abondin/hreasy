<template>
  <v-container class="py-6" data-testid="admin-project-details-view">
    <div class="mx-auto" style="max-width: 1360px;">
      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        border="start"
        class="mb-4"
        data-testid="admin-project-details-error"
      >
        {{ error }}
      </v-alert>

      <v-card v-if="project" data-testid="admin-project-details-card">
        <v-card-title class="d-flex align-center ga-2 flex-wrap px-6 pt-6 pb-2">
          <v-btn
            icon="mdi-arrow-left"
            variant="text"
            data-testid="admin-project-details-back"
            :to="{ name: 'admin-projects' }"
          />
          <v-btn
            icon="mdi-refresh"
            variant="text"
            :loading="loading"
            data-testid="admin-project-details-refresh"
            @click="load"
          />
          <span>
            {{ t("\u041f\u043e\u0434\u0440\u043e\u0431\u043d\u0430\u044f \u0438\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044f \u043f\u043e \u043f\u0440\u043e\u0435\u043a\u0442\u0443 ") }}{{ project.name }}
          </span>
          <v-spacer />
          <v-btn
            icon="mdi-pencil"
            color="primary"
            variant="text"
            :disabled="loading"
            data-testid="admin-project-details-edit"
            @click="editDialog = true"
          />
        </v-card-title>

        <v-card-text class="px-6 pb-6 pt-0">
          <v-row align="start" class="ga-4">
            <v-col cols="12" lg="auto">
              <v-card variant="outlined" min-width="320">
                <v-list density="comfortable">
                  <v-list-item>
                    <v-list-item-title>{{ t("\u041d\u0430\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u043d\u0438\u0435") }}</v-list-item-title>
                    <v-list-item-subtitle>{{ project.name }}</v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title>{{ t("\u041e\u0442\u0434\u0435\u043b") }}</v-list-item-title>
                    <v-list-item-subtitle>{{ project.department?.name ?? t("\u041d\u0435 \u0437\u0430\u0434\u0430\u043d") }}</v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title>{{ t("\u0411\u0438\u0437\u043d\u0435\u0441 \u0430\u043a\u043a\u0430\u0443\u043d\u0442") }}</v-list-item-title>
                    <v-list-item-subtitle>{{ project.businessAccount?.name ?? t("\u041d\u0435 \u0437\u0430\u0434\u0430\u043d") }}</v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title>{{ t("\u0417\u0430\u043a\u0430\u0437\u0447\u0438\u043a") }}</v-list-item-title>
                    <v-list-item-subtitle>{{ project.customer ?? t("\u041d\u0435 \u0437\u0430\u0434\u0430\u043d") }}</v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title>{{ t("\u041d\u0430\u0447\u0430\u043b\u043e") }}</v-list-item-title>
                    <v-list-item-subtitle>{{ formatPlanActual(project.planStartDate, project.startDate) }}</v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title>{{ t("\u041e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u0435") }}</v-list-item-title>
                    <v-list-item-subtitle>{{ formatPlanActual(project.planEndDate, project.endDate) }}</v-list-item-subtitle>
                  </v-list-item>
                </v-list>
              </v-card>
            </v-col>

            <v-col cols="12" lg>
              <v-card variant="outlined" class="h-100">
                <v-card-title class="text-subtitle-1">
                  {{ t("\u0418\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044f \u043e \u043f\u0440\u043e\u0435\u043a\u0442\u0435 (Markdown)") }}
                </v-card-title>
                <v-card-text>
                  <markdown-text-renderer
                    v-if="project.info"
                    :content="project.info"
                    class="project-details-markdown"
                  />
                  <v-alert
                    v-else
                    type="info"
                    variant="tonal"
                    border="start"
                  >
                    {{ t("\u041d\u0435 \u0437\u0430\u0434\u0430\u043d") }}
                  </v-alert>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <admin-managers-table
        v-if="project"
        class="mt-4"
        :selected-object="{ id: project.id, type: 'project' }"
        :title="t('\u041c\u0435\u043d\u0435\u0434\u0436\u0435\u0440\u044b \u043f\u0440\u043e\u0435\u043a\u0442\u0430')"
        :editable="permissions.canAdminProjects()"
        mode="compact"
        test-id="admin-project-managers"
      />
    </div>

    <v-dialog v-model="editDialog" persistent width="96vw" max-width="960">
      <admin-project-form
        :input="project"
        :departments="departments"
        :business-accounts="businessAccounts"
        @close="editDialog = false"
        @saved="onSaved"
      />
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
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

async function load(): Promise<void> {
  const projectId = Number(route.params.projectId);
  if (!projectId) {
    error.value = t("\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0442 \u0434\u0430\u043d\u043d\u044b\u0435");
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
    parts.push(`${actualFormatted} (${t("\u0444\u0430\u043a\u0442")})`);
  }
  const planFormatted = formatDate(plan);
  if (planFormatted) {
    parts.push(`${planFormatted} (${t("\u043f\u043b\u0430\u043d")})`);
  }
  if (parts.length === 0) {
    return t("\u041d\u0435 \u0437\u0430\u0434\u0430\u043d");
  }
  return parts.join(", ");
}

void load();
</script>

<style scoped>
.project-details-markdown {
  min-height: 100%;
}
</style>
