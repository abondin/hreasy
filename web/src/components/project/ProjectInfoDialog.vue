<!--
  Dialog that shows detailed information about a project together with
  assignment history for the selected employee.
-->
<template>
  <v-dialog v-model="dialogOpen" max-width="1280" scrollable>
    <v-card>
      <v-card-item>
        <template #title>
          <span>{{ t("Подробная информация по проекту") }}</span>
        </template>
        <template #append>
          <v-btn icon="mdi-close" variant="text" @click="closeDialog" />
        </template>
      </v-card-item>

      <v-card-text class="pa-6 d-flex flex-column ga-6">
        <v-alert
          v-if="errorMessage"
          type="error"
          variant="tonal"
          border="start"
        >
          {{ errorMessage }}
        </v-alert>

        <v-progress-linear
          v-else-if="loading"
          color="primary"
          indeterminate
        />

        <template v-else>
          <v-row align="start">
            <v-col v-if="project" cols="12" lg="6">
              <detail-section-block :title="t('Основная информация')">
                <property-list variant="aligned" density="compact">
                  <profile-summary-item :label="t('Наименование')">
                    {{ project.name }}
                  </profile-summary-item>
                  <profile-summary-item :label="t('Отдел')">
                    {{ project.department?.name ?? t("Не задан") }}
                  </profile-summary-item>
                  <profile-summary-item :label="t('Бизнес аккаунт')">
                    {{ project.businessAccount?.name ?? t("Не задан") }}
                  </profile-summary-item>
                  <profile-summary-item :label="t('Заказчик')">
                    {{ project.customer ?? t("Не задан") }}
                  </profile-summary-item>
                  <profile-summary-item :label="t('Начало')">
                    {{ formatPlanActual(project.planStartDate, project.startDate) }}
                  </profile-summary-item>
                  <profile-summary-item :label="t('Окончание')">
                    {{ formatPlanActual(project.planEndDate, project.endDate) }}
                  </profile-summary-item>
                  <profile-summary-item
                    v-for="group in managerGroups"
                    :key="group.type"
                    :label="group.type === 'project' ? t('Менеджеры проекта') : t('Менеджеры бизнес аккаунта')"
                  >
                    <div class="d-flex flex-wrap ga-1">
                      <v-chip
                        v-for="manager in group.managers"
                        :key="manager.id"
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
                            {{ `${t("Основное направление")}: ${t(`MANAGER_RESPONSIBILITY_TYPE.${manager.responsibilityType}`)}` }}
                          </span>
                        </v-tooltip>
                      </v-chip>
                    </div>
                  </profile-summary-item>
                </property-list>
              </detail-section-block>
            </v-col>

            <v-col v-if="project" cols="12" lg="6">
              <detail-section-block :title="t('Описание')">
                <markdown-text-renderer
                  v-if="project.info"
                  :content="project.info"
                />
                <div v-else class="text-body-2 text-medium-emphasis">
                  {{ t("Не задан") }}
                </div>
              </detail-section-block>
            </v-col>
          </v-row>

          <detail-section-block :title="t('История изменений')">
            <v-timeline v-if="projectChanges.length" align="start">
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
                    v-if="change.changedBy"
                    class="text-caption text-medium-emphasis"
                  >
                    {{ change.changedBy.name }}
                  </div>
                </template>

                <div class="py-2 d-flex flex-column ga-1">
                  <div class="text-subtitle-2 font-weight-medium">
                    {{ change.project?.name ?? t("Без проекта") }}
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
            <div v-else class="text-body-2 text-medium-emphasis">
              {{ t("РќРµС‚ РґР°РЅРЅС‹С…") }}
            </div>
          </detail-section-block>
        </template>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import DetailSectionBlock from "@/components/shared/DetailSectionBlock.vue";
import MarkdownTextRenderer from "@/components/shared/MarkdownTextRenderer.vue";
import { errorUtils } from "@/lib/errors";
import { formatDate } from "@/lib/datetime";
import PropertyList from "@/components/shared/PropertyList.vue";
import { type EmployeeProjectChange, fetchEmployeeProjectChanges } from "@/services/employee.service";
import { fetchProjectInfo, type ManagerOfObject, type ProjectInfo } from "@/services/projects.service";
import ProfileSummaryItem from "@/views/profile/components/ProfileSummaryItem.vue";

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

const { t } = useI18n();

const loading = ref(false);
const errorMessage = ref("");
const project = ref<ProjectInfo | null>(null);
const projectChanges = ref<EmployeeProjectChange[]>([]);

const managerGroups = computed(() => {
  if (!project.value) {
    return [];
  }

  const groups: { type: "project" | "business_account"; managers: ManagerOfObject[] }[] = [];
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
    if (dialogOpen.value && newId !== oldId) {
      void loadData();
    }
  },
);

watch(
  () => props.employeeId,
  (newId, oldId) => {
    if (dialogOpen.value && newId !== oldId) {
      void loadData();
    }
  },
);

async function loadData() {
  if (!props.employeeId) {
    errorMessage.value = t("Профиль_недоступен");
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const [projectInfo, changes] = await Promise.all([
      props.projectId ? fetchProjectInfo(props.projectId) : Promise.resolve(null),
      fetchEmployeeProjectChanges(props.employeeId),
    ]);
    project.value = projectInfo;
    projectChanges.value = changes;
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
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
