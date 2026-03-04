<!--
  Dialog that shows detailed information about a project together with
  assignment history for the selected employee.
  Mirrors the legacy ProjectInfoCardComponent behaviour.
-->
<template>
  <v-dialog v-model="dialogOpen" max-width="960" scrollable>
    <v-card>
      <v-card-title class="d-flex align-center">
        <span>{{ t("Подробная информация по проекту ") }}</span>
        <v-spacer/>
        <v-btn icon="mdi-close" variant="text" @click="closeDialog"/>
      </v-card-title>

      <v-card-text>
        <v-alert
            v-if="errorMessage"
            type="error"
            variant="tonal"
            border="start"
            class="mb-4"
        >
          {{ errorMessage }}
        </v-alert>

        <v-progress-linear
            v-else-if="loading"
            color="primary"
            indeterminate
            class="mb-4"
        />

        <div v-else-if="project">
          <v-row class="mb-4" dense>
            <v-col cols="12" md="6">
              <v-list density="comfortable">
                <v-list-item>
                  <v-list-item-title>{{ t("Наименование") }}</v-list-item-title>
                  <v-list-item-subtitle>{{ project.name }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>{{ t("Отдел") }}</v-list-item-title>
                  <v-list-item-subtitle>
                    {{ project.department?.name ?? t("Не задан") }}
                  </v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>
                    {{ t("Бизнес аккаунт") }}
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    {{ project.businessAccount?.name ?? t("Не задан") }}
                  </v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>{{ t("Заказчик") }}</v-list-item-title>
                  <v-list-item-subtitle>
                    {{ project.customer ?? t("Не задан") }}
                  </v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>{{ t("Начало") }}</v-list-item-title>
                  <v-list-item-subtitle>
                  {{ formatPlanActual(project.planStartDate, project.startDate) }}
                  </v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>{{ t("Окончание") }}</v-list-item-title>
                  <v-list-item-subtitle>
                  {{ formatPlanActual(project.planEndDate, project.endDate) }}
                  </v-list-item-subtitle>
                </v-list-item>
                <v-list-item v-for="group in managerGroups" :key="group.type">
                  <v-list-item-title>
                    {{
                      group.type === "project"
                          ? t("Менеджеры проекта")
                          : t("Менеджеры бизнес аккаунта")
                    }}
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip
                        v-for="manager in group.managers"
                        :key="manager.id"
                        class="mr-1 mb-1"
                        size="small"
                        variant="outlined"
                    >
                      {{ manager.employeeName }}
                      <v-tooltip location="bottom">
                        <template #activator="{ props: tooltipProps }">
                          <v-icon
                              v-bind="tooltipProps"
                              icon="mdi-help-circle"
                              size="x-small"
                              class="ml-1"
                          />
                        </template>
                        <span>
                          {{
                            `${t("Основное направление")}: ${t(
                                `MANAGER_RESPONSIBILITY_TYPE.${manager.responsibilityType}`,
                            )}`
                          }}
                        </span>
                      </v-tooltip>
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>

            <v-col v-if="project.info" cols="12" md="6">
              <markdown-text-renderer
                  class="project-info__description"
                  :content="project.info"
              />
            </v-col>
          </v-row>

          <v-timeline align="start">
            <v-timeline-item
                v-for="change in projectChanges"
                :key="change.id"
                dot-color="primary"
                size="small"
            >
              <template #opposite>
                <div class="text-caption font-weight-medium">
                  {{ formatDateLocalized(change.changedAt) }}
                </div>
                <div
                    v-if="change.changedBy" class="text-caption text-medium-emphasis">
                  {{ change.changedBy.name }}
                </div>
              </template>

              <div class="project-history-entry">
                <div class="text-subtitle-2 font-weight-medium">
                  {{ change.project?.name ?? "-" }}
                </div>
                <div v-if="change.project?.role" class="text-body-2">
                  {{ change.project.role }}
                </div>
                <div v-if="change.ba" class="text-caption text-medium-emphasis">
                  {{ change.ba.name }}
                </div>
              </div>
            </v-timeline-item>
          </v-timeline>
        </div>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import {computed, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import type {ManagerOfObject, ProjectInfo} from "@/services/projects.service";
import {fetchProjectInfo} from "@/services/projects.service";
import MarkdownTextRenderer from "@/components/shared/MarkdownTextRenderer.vue";
import {type EmployeeProjectChange, fetchEmployeeProjectChanges,} from "@/services/employee.service";
import { formatDate } from "@/lib/datetime";

const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      projectId: number | null;
      employeeId: number | null;
    }>(),
    {
      modelValue: false,
      projectId: null,
      employeeId: null,
    },
);

const emit = defineEmits<{
  (event: "update:modelValue", value: boolean): void;
}>();

const dialogOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit("update:modelValue", value),
});

const {t} = useI18n();

const loading = ref(false);
const errorMessage = ref("");
const project = ref<ProjectInfo | null>(null);
const projectChanges = ref<EmployeeProjectChange[]>([]);

const managerGroups = computed(() => {
  if (!project.value) {
    return [];
  }
  const groups: { type: "project" | "business_account"; managers: ManagerOfObject[] }[] =
      [];
  if (project.value.managers?.length) {
    groups.push({
      type: "project",
      managers: project.value.managers,
    });
  }
  if (project.value.baManagers?.length) {
    groups.push({
      type: "business_account",
      managers: project.value.baManagers,
    });
  }
  return groups;
});

watch(
    () => dialogOpen.value,
    (open) => {
      if (open) {
        void loadData();
      } else {
        resetState();
      }
    },
);

watch(
    () => props.projectId,
    (newId, oldId) => {
      if (dialogOpen.value && newId && newId !== oldId) {
        void loadData();
      }
    },
);

async function loadData() {
  if (!props.projectId || !props.employeeId) {
    errorMessage.value = t("Профиль_недоступен");
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const [projectInfo, changes] = await Promise.all([
      fetchProjectInfo(props.projectId),
      fetchEmployeeProjectChanges(props.employeeId),
    ]);
    project.value = projectInfo;
    projectChanges.value = changes;
  } catch (error) {
    errorMessage.value = String(error);
  } finally {
    loading.value = false;
  }
}

function resetState() {
  loading.value = false;
  errorMessage.value = "";
  project.value = null;
  projectChanges.value = [];
}

function closeDialog() {
  dialogOpen.value = false;
}

function formatDateLocalized(value: string | undefined | null): string {
  const formatted = formatDate(value);
  return formatted || t("Не задан");
}

function formatPlanActual(
  plan: string | undefined | null,
  actual: string | undefined | null,
): string {
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
</script>

<style scoped>
.project-info__description {
  white-space: pre-wrap;
  font-size: 0.95rem;
  line-height: 1.4;
  color: rgba(0, 0, 0, 0.78);
}


.project-history-entry {
  padding: 8px 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
</style>
