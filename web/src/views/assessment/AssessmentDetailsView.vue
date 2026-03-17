<template>
  <v-container fluid class="py-6" data-testid="assessment-details-view">
    <div class="mx-auto" style="max-width: 1360px;">
      <v-skeleton-loader v-if="loading" type="heading, paragraph, paragraph" class="mt-4" />
      <v-alert v-else-if="fetchError" type="error" variant="tonal" class="mt-4">{{ fetchError }}</v-alert>
      <v-alert
        v-else-if="accessDenied"
        type="error"
        variant="tonal"
        class="mt-4"
      >
        {{ t("Недостаточно прав для просмотра детальной информации по ассессменту") }}
        <div class="mt-2">
          <router-link :to="{ name: 'employee-assessments', params: { employeeId: String(employeeId) } }">
            {{ t("Вернуться к карточке сотрудника") }}
          </router-link>
        </div>
      </v-alert>

      <template v-else-if="assessment">
        <v-breadcrumbs
          class="mt-4 mb-4 px-0"
          density="compact"
          divider="/"
          :items="breadcrumbs"
        >
          <template #item="{ item }">
            <router-link v-if="item.to" :to="item.to" class="text-decoration-none">
              {{ item.title }}
            </router-link>
            <span v-else>{{ item.title }}</span>
          </template>
        </v-breadcrumbs>

        <v-card class="mt-4">
          <v-card-title class="d-flex align-center ga-4 flex-wrap px-6 pt-6 pb-2">
            <div class="text-h6 font-weight-semibold">
              {{ assessment.employee?.name ?? "-" }}
            </div>
            <v-spacer />
            <div class="d-flex align-center ga-2">
              <v-btn
                icon="mdi-checkbox-marked-circle"
                color="success"
                variant="text"
                :disabled="actionLoading || Boolean(assessment.completedBy)"
                @click="completeDialog = true"
              />
              <v-btn
                icon="mdi-delete"
                color="error"
                variant="text"
                :disabled="actionLoading || Boolean(assessment.canceledBy)"
                @click="cancelDialog = true"
              />
            </div>
          </v-card-title>
          <v-card-text class="px-6 pb-6 pt-0">
            <v-row align="start" class="mt-1 ga-4">
              <v-col cols="12" lg="auto">
                <profile-summary
                  v-if="employee"
                  :employee="employee"
                  :read-only="true"
                  :show-name="false"
                />
              </v-col>
              <v-col cols="12" lg class="pt-2">
                <profile-summary-item :label="t('Дата ассессмента')">
                  <span :class="{ 'text-medium-emphasis text-decoration-line-through': Boolean(assessment.canceledBy) }">
                    {{ formatDate(assessment.plannedDate) }}
                  </span>
                </profile-summary-item>
                <profile-summary-item :label="t('Создано')">
                  {{ assessment.createdBy ? `${formatDateTime(assessment.createdAt)} (${assessment.createdBy.name})` : "-" }}
                </profile-summary-item>
                <profile-summary-item :label="t('Завершено')">
                  {{ assessment.completedBy ? `${formatDateTime(assessment.completedAt)} (${assessment.completedBy.name})` : "-" }}
                </profile-summary-item>
                <profile-summary-item :label="t('Отменено')">
                  {{ assessment.canceledBy ? `${formatDateTime(assessment.canceledAt)} (${assessment.canceledBy.name})` : "-" }}
                </profile-summary-item>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>

        <v-card class="mt-5">
          <v-card-title class="d-flex align-center">
            {{ t("Вложения") }}
            <v-spacer />
            <v-btn
              icon="mdi-plus"
              color="primary"
              variant="text"
              :disabled="actionLoading"
              @click="uploadDialog = true"
            />
          </v-card-title>
          <v-card-text>
            <div v-if="assessment.attachmentsFilenames.length" class="d-flex flex-wrap ga-2">
              <v-chip
                v-for="attachment in assessment.attachmentsFilenames"
                :key="attachment"
                variant="outlined"
              >
                <a
                  class="assessment-attachment-link"
                  :href="attachmentPath(attachment)"
                  :download="attachment"
                >
                  {{ attachment }}
                </a>
                <template #append>
                  <v-icon
                    size="16"
                    class="assessment-attachment-delete"
                    @click.stop="openDeleteAttachmentDialog(attachment)"
                  >
                    mdi-delete
                  </v-icon>
                </template>
              </v-chip>
            </div>
            <div v-else>{{ t("Отсутствуют данные") }}</div>
          </v-card-text>
        </v-card>
      </template>
    </div>

    <v-dialog v-model="uploadDialog" max-width="720" persistent>
      <v-card>
        <v-card-title>{{ t("Прикрепить файл") }}</v-card-title>
        <v-card-text>
          <file-upload-zone
            v-if="assessment"
            :file-id="`assessment-${assessment.id}`"
            :post-action="uploadPath"
            @close="handleUploadClose"
          />
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog v-model="deleteAttachmentDialog" max-width="480">
      <v-card>
        <v-card-title>{{ t("Удаление") }}</v-card-title>
        <v-card-text>
          <p>{{ t("Вы уверены, что хотите удалить вложение?") }}</p>
          <div>{{ attachmentToDelete }}</div>
          <v-alert v-if="deleteAttachmentError" type="error" variant="tonal" class="mt-3">
            {{ deleteAttachmentError }}
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="actionLoading" @click="deleteAttachmentDialog = false">{{ t("Нет") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitDeleteAttachment">{{ t("Да") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="cancelDialog" max-width="480">
      <v-card>
        <v-card-title>{{ t("Отмена") }}</v-card-title>
        <v-card-text>
          <p>{{ t("Вы уверены, что хотите отменить ассессмент?") }}</p>
          <v-alert v-if="cancelError" type="error" variant="tonal" class="mt-3">{{ cancelError }}</v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="actionLoading" @click="cancelDialog = false">{{ t("Нет") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitCancel">{{ t("Да") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="completeDialog" max-width="480">
      <v-card>
        <v-card-title>{{ t("Отметить как завершенный") }}</v-card-title>
        <v-card-text>
          <p>{{ t("Отметить ассессмент как завершенный?") }}</p>
          <v-alert v-if="completeError" type="error" variant="tonal" class="mt-3">{{ completeError }}</v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="actionLoading" @click="completeDialog = false">{{ t("Нет") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitComplete">{{ t("Да") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute, type RouteLocationRaw } from "vue-router";
import FileUploadZone, { type UploadCompleteEvent } from "@/components/FileUploadZone.vue";
import { formatDate, formatDateTime } from "@/lib/datetime";
import { errorUtils } from "@/lib/errors";
import { findEmployee, type Employee } from "@/services/employee.service";
import {
  cancelAssessment,
  completeAssessment,
  deleteAssessmentAttachment,
  fetchAssessmentDetails,
  getAssessmentAttachmentPath,
  getAssessmentAttachmentUploadUrl,
  type AssessmentWithFormsAndFiles,
} from "@/services/assessment.service";
import ProfileSummary from "@/views/profile/components/ProfileSummary.vue";
import ProfileSummaryItem from "@/views/profile/components/ProfileSummaryItem.vue";

const { t } = useI18n();
const route = useRoute();

const loading = ref(false);
const actionLoading = ref(false);
const accessDenied = ref(false);
const fetchError = ref("");
const cancelError = ref("");
const completeError = ref("");
const deleteAttachmentError = ref("");
const employee = ref<Employee | null>(null);
const assessment = ref<AssessmentWithFormsAndFiles | null>(null);

const uploadDialog = ref(false);
const deleteAttachmentDialog = ref(false);
const cancelDialog = ref(false);
const completeDialog = ref(false);
const attachmentToDelete = ref<string | null>(null);

const employeeId = computed(() => Number(route.params.employeeId));
const assessmentId = computed(() => Number(route.params.assessmentId));
const uploadPath = computed(() =>
  assessment.value ? getAssessmentAttachmentUploadUrl(employeeId.value, assessmentId.value) : "",
);
const breadcrumbs = computed<Array<{ title: string; to?: RouteLocationRaw }>>(() => [
  {
    title: t("Ассессменты"),
    to: { name: "assessments" },
  },
  {
    title: assessment.value?.employee?.name ?? "-",
    to: { name: "employee-assessments", params: { employeeId: String(employeeId.value) } },
  },
  {
    title: assessment.value ? formatDate(assessment.value.plannedDate) : "-",
  },
]);

watch([employeeId, assessmentId], () => {
  load().catch(() => undefined);
}, { immediate: true });

async function load(): Promise<void> {
  if (!Number.isInteger(employeeId.value) || !Number.isInteger(assessmentId.value)) {
    return;
  }

  loading.value = true;
  accessDenied.value = false;
  fetchError.value = "";
  employee.value = null;
  assessment.value = null;
  try {
    const [employeeData, assessmentData] = await Promise.all([
      findEmployee(employeeId.value),
      fetchAssessmentDetails(employeeId.value, assessmentId.value),
    ]);
    employee.value = employeeData;
    assessment.value = assessmentData;
  } catch (err: unknown) {
    if (errorUtils.isAccessDenied(err)) {
      accessDenied.value = true;
    } else {
      fetchError.value = errorUtils.shortMessage(err);
    }
  } finally {
    loading.value = false;
  }
}

function attachmentPath(filename: string): string {
  if (!assessment.value) {
    return "";
  }
  return getAssessmentAttachmentPath(
    employeeId.value,
    assessmentId.value,
    filename,
    assessment.value.attachmentsAccessToken,
  );
}

function openDeleteAttachmentDialog(filename: string): void {
  attachmentToDelete.value = filename;
  deleteAttachmentError.value = "";
  deleteAttachmentDialog.value = true;
}

async function submitDeleteAttachment(): Promise<void> {
  if (!attachmentToDelete.value) {
    return;
  }

  actionLoading.value = true;
  deleteAttachmentError.value = "";
  try {
    const result = await deleteAssessmentAttachment(
      employeeId.value,
      assessmentId.value,
      attachmentToDelete.value,
    );
    if (!result.deleted) {
      deleteAttachmentError.value = t("Не удалось удалить файл");
      return;
    }
    deleteAttachmentDialog.value = false;
    attachmentToDelete.value = null;
    await load();
  } catch (err: unknown) {
    deleteAttachmentError.value = errorUtils.shortMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

function handleUploadClose(event: UploadCompleteEvent): void {
  uploadDialog.value = false;
  if (event.uploaded) {
    load().catch(() => undefined);
  }
}

async function submitCancel(): Promise<void> {
  actionLoading.value = true;
  cancelError.value = "";
  try {
    await cancelAssessment(employeeId.value, assessmentId.value);
    cancelDialog.value = false;
    await load();
  } catch (err: unknown) {
    cancelError.value = errorUtils.shortMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

async function submitComplete(): Promise<void> {
  actionLoading.value = true;
  completeError.value = "";
  try {
    await completeAssessment(employeeId.value, assessmentId.value);
    completeDialog.value = false;
    await load();
  } catch (err: unknown) {
    completeError.value = errorUtils.shortMessage(err);
  } finally {
    actionLoading.value = false;
  }
}
</script>

<style scoped>
.assessment-attachment-link {
  color: inherit;
  text-decoration: none;
}

.assessment-attachment-delete {
  margin-left: 6px;
  cursor: pointer;
}
</style>
