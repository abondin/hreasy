<template>
  <v-container class="py-6">
    <v-btn variant="text" prepend-icon="mdi-arrow-left" :to="{ name: 'mentorship' }">
      {{ t("Менторство") }}
    </v-btn>

    <v-alert v-if="error" type="error" class="mt-4" variant="tonal" border="start">
      {{ error }}
    </v-alert>

    <template v-else-if="junior">
      <v-card class="mt-4" :loading="loading">
        <v-card-title class="d-flex align-center">
          <span>{{ junior.juniorEmpl.name }}</span>
          <v-spacer />
          <v-btn
            v-if="canEditRegistry"
            icon="mdi-pencil"
            variant="text"
            @click="openEditDialog"
          />
          <v-btn
            v-if="canEditRegistry"
            icon="mdi-school"
            variant="text"
            :color="junior.graduation ? '' : 'success'"
            @click="graduateDialog = true"
          />
          <v-btn
            v-if="canEditRegistry"
            icon="mdi-delete"
            variant="text"
            color="error"
            :disabled="Boolean(junior.graduation)"
            @click="deleteDialog = true"
          />
        </v-card-title>

        <v-card-text>
          <v-row>
            <v-col cols="12" md="6"><strong>{{ t("Ментор") }}:</strong> {{ junior.mentor?.name || t("Нет") }}</v-col>
            <v-col cols="12" md="6"><strong>{{ t("Бюджет") }}:</strong> {{ junior.budgetingAccount?.name || t("Нет") }}</v-col>
            <v-col cols="12" md="6"><strong>{{ t("Роль") }}:</strong> {{ junior.role || t("Нет") }}</v-col>
            <v-col cols="12" md="6"><strong>{{ t("Дата трудоустройства") }}:</strong> {{ formatDate(junior.juniorDateOfEmployment) }}</v-col>
            <v-col cols="12" md="6"><strong>{{ t("Месяцев в компании") }}:</strong> {{ junior.juniorInCompanyMonths?.value }}</v-col>
            <v-col cols="12" md="6"><strong>{{ t("Месяцев без отчёта") }}:</strong> {{ junior.monthsWithoutReport?.value }}</v-col>
            <v-col v-if="junior.graduation" cols="12" md="6">
              <strong>{{ t("Обучение завершил") }}:</strong>
              {{ junior.graduation.graduatedBy?.name }} ({{ formatDateTime(junior.graduation.graduatedAt) }})
            </v-col>
            <v-col v-if="junior.graduation" cols="12">
              <strong>{{ t("Комментарий") }}:</strong> {{ junior.graduation.comment || t("Нет") }}
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <v-card class="mt-4" :loading="loading">
        <v-card-title class="d-flex align-center">
          <span>{{ t("Отчёты") }}</span>
          <v-spacer />
          <v-btn
            v-if="canViewMentorship"
            prepend-icon="mdi-plus"
            color="primary"
            variant="tonal"
            :disabled="Boolean(junior.graduation)"
            @click="openCreateReport"
          >
            {{ t("Добавить отчёт") }}
          </v-btn>
        </v-card-title>

        <v-card-text v-if="sortedReports.length === 0" class="text-medium-emphasis">
          {{ t("Оставьте ваш первый отчёт о работе сотрудника!") }}
        </v-card-text>

        <v-card-text v-for="report in sortedReports" :key="report.id" class="border-b-sm">
          <div class="d-flex align-center">
            <div class="text-subtitle-2">{{ report.createdBy.name }} ({{ formatDateTime(report.createdAt) }})</div>
            <v-spacer />
            <v-btn
              v-if="canEditReport(report.createdBy.id)"
              icon="mdi-pencil"
              variant="text"
              :disabled="Boolean(junior.graduation)"
              @click="openEditReport(report.id)"
            />
            <v-btn
              v-if="canEditReport(report.createdBy.id)"
              icon="mdi-delete"
              variant="text"
              :disabled="Boolean(junior.graduation)"
              @click="openDeleteReport(report.id)"
            />
          </div>
          <div class="mt-2 d-flex align-center ga-2">
            <v-icon :icon="getProgressIcon(report.progress).icon" :color="getProgressIcon(report.progress).color" />
            <span>{{ t(`JUNIOR_PROGRESS_TYPE.${report.progress}`) }}</span>
          </div>
          <div class="mt-2 d-flex flex-wrap ga-2">
            <v-chip v-for="(value, key) in report.ratings" :key="key" size="small" variant="outlined">
              {{ t(`JUNIOR_REPORT_RATING.${key}.title`) }}: {{ value }}
            </v-chip>
          </div>
          <markdown-text-renderer class="mt-3" :content="report.comment" />
        </v-card-text>
      </v-card>
    </template>

    <v-dialog v-model="editDialog" max-width="760">
      <v-card>
        <v-card-title>{{ t("Обновление записи в реестре") }}</v-card-title>
        <v-card-text>
          <v-autocomplete
            v-model="updateForm.mentorId"
            :items="employees"
            item-title="displayName"
            item-value="id"
            clearable
            :label="t('Ментор')"
          />
          <v-autocomplete
            v-model="updateForm.budgetingAccount"
            :items="allBusinessAccounts"
            item-title="name"
            item-value="id"
            clearable
            :label="t('Бюджет из бизнес аккаунта')"
          />
          <v-combobox
            v-model="updateForm.role"
            :items="projectRoles"
            item-title="value"
            item-value="value"
            clearable
            :label="t('Роль')"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="editDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitUpdateJunior">{{ t("Применить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="reportDialog" max-width="800">
      <v-card>
        <v-card-title>{{ editingReportId ? t("Применить") : t("Создать отчёт") }}</v-card-title>
        <v-card-text>
          <v-select
            v-model="reportForm.progress"
            :items="progressOptions"
            item-title="title"
            item-value="value"
            :label="t('Динамика роста')"
          />
          <v-row>
            <v-col v-for="field in ratingFields" :key="field" cols="12" md="6">
              <v-slider
                v-model="reportForm.ratings[field]"
                min="1"
                max="5"
                step="1"
                thumb-label
                :label="t(`JUNIOR_REPORT_RATING.${field}.title`)"
              />
            </v-col>
          </v-row>
          <markdown-text-editor v-model="reportForm.comment" :label="t('Комментарий')" :counter="4096" />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="reportDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitReport">{{ editingReportId ? t("Применить") : t("Создать") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <confirm-delete-dialog
      :open="deleteDialog"
      :title="t('удаление')"
      :message="t('Вы уверены, что хотите удалить запись?')"
      :loading="actionLoading"
      @close="deleteDialog = false"
      @confirm="deleteJunior"
    />

    <confirm-delete-dialog
      :open="deleteReportDialog"
      :title="t('Удалить отчёт')"
      :message="t('Вы уверены, что хотите удалить отчёт?')"
      :loading="actionLoading"
      @close="closeDeleteReport"
      @confirm="confirmDeleteReport"
    />

    <v-dialog v-model="graduateDialog" max-width="620">
      <v-card>
        <v-card-title>
          {{ junior?.graduation ? t("Отменить факт окончания обучения") : t("Завершение обучения") }}
        </v-card-title>
        <v-card-text>
          <v-textarea
            v-if="!junior?.graduation"
            v-model="graduationComment"
            :label="t('Комментарий')"
            :counter="1024"
          />
          <p v-else>{{ t("Вы уверены, что хотите отменить решение об окончании обучения?") }}</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="graduateDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitGraduation">{{ t("Применить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { usePermissions } from "@/lib/permissions";
import { formatDate, formatDateTime } from "@/lib/datetime";
import { errorUtils } from "@/lib/errors";
import ConfirmDeleteDialog from "@/components/shared/ConfirmDeleteDialog.vue";
import MarkdownTextRenderer from "@/components/shared/MarkdownTextRenderer.vue";
import MarkdownTextEditor from "@/components/shared/MarkdownTextEditor.vue";
import {
  cancelJuniorGraduation,
  createJuniorReport,
  deleteJuniorFromRegistry,
  deleteJuniorReport,
  fetchBusinessAccounts,
  fetchCurrentProjectRoles,
  fetchEmployeesForRegistry,
  fetchJuniorDetails,
  graduateJunior,
  JuniorProgressType,
  juniorProgressTypes,
  updateJuniorRegistry,
  updateJuniorReport,
  type AddOrUpdateJuniorReportBody,
  type CurrentProjectRole,
  type JuniorDto,
  type SimpleDict,
  type UpdateJuniorRegistryBody,
} from "@/services/junior-registry.service";
import type { Employee } from "@/services/employee.service";

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const permissions = usePermissions();

const loading = ref(false);
const actionLoading = ref(false);
const error = ref("");
const junior = ref<JuniorDto | null>(null);
const deleteDialog = ref(false);
const graduateDialog = ref(false);
const deleteReportDialog = ref(false);
const reportDialog = ref(false);
const editDialog = ref(false);
const reportToDelete = ref<number | null>(null);
const editingReportId = ref<number | null>(null);
const graduationComment = ref("");
const employees = ref<Employee[]>([]);
const allBusinessAccounts = ref<SimpleDict[]>([]);
const projectRoles = ref<CurrentProjectRole[]>([]);

const ratingFields: (keyof AddOrUpdateJuniorReportBody["ratings"])[] = [
  "overallReadiness",
  "competence",
  "process",
  "teamwork",
  "contribution",
  "motivation",
];

const reportForm = reactive<AddOrUpdateJuniorReportBody>({
  progress: JuniorProgressType.NO_PROGRESS,
  comment: "",
  ratings: {
    overallReadiness: 3,
    competence: 3,
    process: 3,
    teamwork: 3,
    contribution: 3,
    motivation: 3,
  },
});

const updateForm = reactive<UpdateJuniorRegistryBody>({
  mentorId: null,
  role: "",
  budgetingAccount: null,
});

const canViewMentorship = computed(
  () => permissions.canAccessJuniorsRegistry() || permissions.canAdminJuniorRegistry(),
);

const canEditRegistry = computed(() => {
  if (!junior.value) {
    return false;
  }
  return permissions.canUpdateJuniorRegistryInfo(junior.value.createdBy.id);
});

const sortedReports = computed(() => [...(junior.value?.reports ?? [])].sort((a, b) => b.createdAt.localeCompare(a.createdAt)));

const progressOptions = computed(() => juniorProgressTypes.map((type) => ({
  title: t(`JUNIOR_PROGRESS_TYPE.${type}`),
  value: type,
})));

function getProgressIcon(type: JuniorProgressType): { icon: string; color: string } {
  switch (type) {
    case JuniorProgressType.DEGRADATION:
      return { icon: "mdi-arrow-bottom-left", color: "error" };
    case JuniorProgressType.NO_PROGRESS:
      return { icon: "mdi-minus", color: "" };
    case JuniorProgressType.PROGRESS:
      return { icon: "mdi-arrow-top-right", color: "success" };
    case JuniorProgressType.GOOD_PROGRESS:
      return { icon: "mdi-arrow-up-bold", color: "success" };
    default:
      return { icon: "mdi-help", color: "warning" };
  }
}

function canEditReport(reportCreatorId: number): boolean {
  return permissions.canUpdateJuniorReport(reportCreatorId);
}

async function loadJunior(): Promise<void> {
  if (!canViewMentorship.value) {
    return;
  }
  loading.value = true;
  error.value = "";
  try {
    const juniorId = Number(route.params.juniorRegistryId);
    junior.value = await fetchJuniorDetails(juniorId);
  } catch (err: unknown) {
    error.value = errorUtils.shortMessage(err);
  } finally {
    loading.value = false;
  }
}

async function loadDictionaries(): Promise<void> {
  const [employeesList, baList, rolesList] = await Promise.all([
    fetchEmployeesForRegistry(),
    fetchBusinessAccounts(),
    fetchCurrentProjectRoles(),
  ]);
  employees.value = employeesList;
  allBusinessAccounts.value = baList;
  projectRoles.value = rolesList;
}

function openCreateReport() {
  editingReportId.value = null;
  reportForm.progress = JuniorProgressType.NO_PROGRESS;
  reportForm.comment = "";
  reportForm.ratings.overallReadiness = 3;
  reportForm.ratings.competence = 3;
  reportForm.ratings.process = 3;
  reportForm.ratings.teamwork = 3;
  reportForm.ratings.contribution = 3;
  reportForm.ratings.motivation = 3;
  reportDialog.value = true;
}

function openEditReport(reportId: number): void {
  const report = junior.value?.reports.find((item) => item.id === reportId);
  if (!report) {
    return;
  }
  editingReportId.value = report.id;
  reportForm.progress = report.progress;
  reportForm.comment = report.comment;
  reportForm.ratings.overallReadiness = report.ratings.overallReadiness;
  reportForm.ratings.competence = report.ratings.competence;
  reportForm.ratings.process = report.ratings.process;
  reportForm.ratings.teamwork = report.ratings.teamwork;
  reportForm.ratings.contribution = report.ratings.contribution;
  reportForm.ratings.motivation = report.ratings.motivation;
  reportDialog.value = true;
}

function openEditDialog(): void {
  if (!junior.value) {
    return;
  }
  updateForm.mentorId = junior.value.mentor?.id ?? null;
  updateForm.role = junior.value.role ?? "";
  updateForm.budgetingAccount = junior.value.budgetingAccount?.id ?? null;
  editDialog.value = true;
}

async function submitUpdateJunior(): Promise<void> {
  if (!junior.value) {
    return;
  }
  actionLoading.value = true;
  try {
    await updateJuniorRegistry(junior.value.id, updateForm);
    editDialog.value = false;
    await loadJunior();
  } finally {
    actionLoading.value = false;
  }
}

async function submitReport() {
  if (!junior.value) {
    return;
  }
  actionLoading.value = true;
  try {
    if (editingReportId.value) {
      await updateJuniorReport(junior.value.id, editingReportId.value, reportForm);
    } else {
      await createJuniorReport(junior.value.id, reportForm);
    }
    reportDialog.value = false;
    editingReportId.value = null;
    await loadJunior();
  } finally {
    actionLoading.value = false;
  }
}

async function deleteJunior() {
  if (!junior.value) {
    return;
  }
  actionLoading.value = true;
  try {
    await deleteJuniorFromRegistry(junior.value.id);
    deleteDialog.value = false;
    await router.push({ name: "mentorship" });
  } finally {
    actionLoading.value = false;
  }
}

function openDeleteReport(reportId: number) {
  reportToDelete.value = reportId;
  deleteReportDialog.value = true;
}

function closeDeleteReport() {
  deleteReportDialog.value = false;
  reportToDelete.value = null;
}

async function confirmDeleteReport() {
  if (!junior.value || !reportToDelete.value) {
    return;
  }
  actionLoading.value = true;
  try {
    await deleteJuniorReport(junior.value.id, reportToDelete.value);
    closeDeleteReport();
    await loadJunior();
  } finally {
    actionLoading.value = false;
  }
}

async function submitGraduation() {
  if (!junior.value) {
    return;
  }
  actionLoading.value = true;
  try {
    if (junior.value.graduation) {
      await cancelJuniorGraduation(junior.value.id);
    } else {
      await graduateJunior(junior.value.id, { comment: graduationComment.value || null });
    }
    graduateDialog.value = false;
    graduationComment.value = "";
    await loadJunior();
  } finally {
    actionLoading.value = false;
  }
}

onMounted(() => {
  Promise.all([loadJunior(), loadDictionaries()]).catch((err: unknown) => {
    error.value = errorUtils.shortMessage(err);
  });
});
</script>
