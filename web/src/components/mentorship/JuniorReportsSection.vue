<template>
  <v-card :loading="loading">
    <v-card-title class="d-flex align-center ga-3">
      <span class="d-flex align-center ga-2">
        <span>{{ t("Отчёты") }}</span>
        <value-with-status-chip
          v-if="Number(junior.monthsWithoutReport.status ?? 0) !== 1"
          :value="junior.monthsWithoutReport"
          dense
        >
          {{ t("months", junior.monthsWithoutReport.value) }} {{ t("без отчёта") }}
        </value-with-status-chip>
      </span>
      <v-spacer />
      <v-tooltip v-if="canViewMentorship" location="bottom">
        <template #activator="{ props }">
          <v-btn
            v-bind="props"
            icon="mdi-plus"
            color="primary"
            variant="text"
            size="default"
            :disabled="isGraduated"
            @click="$emit('open-create-report')"
          />
        </template>
        <span>{{ t("Добавить отчёт") }}</span>
      </v-tooltip>
    </v-card-title>

    <v-card-text v-if="sortedReports.length === 0" class="text-medium-emphasis">
      {{ t("Оставьте ваш первый отчёт о работе сотрудника!") }}
    </v-card-text>

    <v-card-text class="pt-0">
      <v-card
        v-for="report in sortedReports"
        :key="report.id"
        class="mb-4"
      >
        <v-card-text>
          <div class="d-flex align-center justify-space-between ga-3">
            <div class="text-subtitle-1">
              {{ report.createdBy.name }} ({{ formatDateTime(report.createdAt) }})
            </div>
            <div class="d-flex align-center ga-1">
              <v-btn
                v-if="canEditReport(report.createdBy.id)"
                icon="mdi-pencil"
                variant="text"
                size="default"
                :disabled="isGraduated"
                @click="$emit('open-edit-report', report.id)"
              />
              <v-btn
                v-if="canDeleteReport(report.createdBy.id)"
                icon="mdi-delete"
                variant="text"
                size="default"
                :disabled="isGraduated"
                @click="$emit('open-delete-report', report.id)"
              />
            </div>
          </div>

          <v-row class="mt-2" no-gutters>
            <v-col cols="12" xl="auto" class="pb-3 pb-xl-0 pr-0 pr-xl-6">
              <v-row no-gutters class="flex-nowrap">
                <v-col cols="auto" class="d-flex justify-center align-start pt-1 pr-2">
                  <v-tooltip location="bottom">
                    <template #activator="{ props }">
                      <v-icon
                        v-bind="props"
                        :icon="getProgressIcon(report.progress).icon"
                        :color="getProgressIcon(report.progress).color"
                        size="large"
                      />
                    </template>
                    <span>{{ t(`JUNIOR_PROGRESS_TYPE.${report.progress}`) }}</span>
                  </v-tooltip>
                </v-col>

                <v-col cols="auto" class="min-width-0">
                  <div class="d-flex flex-column ga-0">
                    <junior-report-rating-field
                      v-for="field in ratingFields"
                      :key="field"
                      :field="field"
                      :model-value="report.ratings[field]"
                      :prev="previousReport(report)?.ratings[field] ?? null"
                      compact
                      readonly
                    />
                  </div>
                </v-col>
              </v-row>
            </v-col>

            <v-col cols="12" xl class="pl-0 pl-xl-2">
              <markdown-text-renderer :content="report.comment" />
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>
    </v-card-text>
  </v-card>

  <v-dialog :model-value="reportDialog" max-width="800" @update:model-value="onReportDialogChange">
    <v-card>
      <v-card-title>{{ editingReportId ? t("Применить") : t("Создать отчёт") }}</v-card-title>
      <v-card-text>
        <v-form ref="reportFormRef">
        <v-select
          :model-value="reportForm.progress"
          :items="progressOptions"
          item-title="title"
          item-value="value"
          :label="t('Динамика роста')"
          :rules="progressRules"
          @update:model-value="updateReportFormField('progress', $event)"
        />
        <div class="mt-4 d-flex flex-column ga-1">
          <junior-report-rating-field
            v-for="field in ratingFields"
            :key="field"
            :field="field"
            :model-value="reportForm.ratings[field]"
            :prev="junior.latestReport?.ratings[field] ?? null"
            compact
            @update:model-value="updateRatingField(field, $event)"
          />
        </div>
        <markdown-text-editor
          :model-value="reportForm.comment"
          :label="t('Комментарий')"
          :counter="4096"
          :rules="commentRules"
          @update:model-value="updateReportFormField('comment', $event)"
        />
        </v-form>
        <v-alert v-if="reportFormError" type="error" class="mt-4" variant="tonal" border="start">
          {{ reportFormError }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" @click="$emit('close-report')">{{ t("Отмена") }}</v-btn>
        <v-btn color="primary" :loading="actionLoading" @click="onSubmitReport">
          {{ editingReportId ? t("Применить") : t("Создать") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <confirm-delete-dialog
    :open="deleteReportDialog"
    :title="t('Удалить отчёт')"
    :message="t('Вы уверены, что хотите удалить отчёт?')"
    :loading="actionLoading"
    @close="$emit('close-delete-report')"
    @confirm="$emit('confirm-delete-report')"
  />
</template>

<script setup lang="ts">
import { computed } from "vue";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { formatDateTime } from "@/lib/datetime";
import JuniorReportRatingField from "@/components/mentorship/JuniorReportRatingField.vue";
import ValueWithStatusChip from "@/components/shared/ValueWithStatusChip.vue";
import MarkdownTextRenderer from "@/components/shared/MarkdownTextRenderer.vue";
import MarkdownTextEditor from "@/components/shared/MarkdownTextEditor.vue";
import ConfirmDeleteDialog from "@/components/shared/ConfirmDeleteDialog.vue";
import type {
  AddOrUpdateJuniorReportBody,
  JuniorDto,
  JuniorProgressType,
  JuniorReport,
} from "@/services/junior-registry.service";

const props = defineProps<{
  junior: JuniorDto;
  loading: boolean;
  actionLoading: boolean;
  canViewMentorship: boolean;
  sortedReports: JuniorReport[];
  canEditReport: (reportCreatorId: number) => boolean;
  canDeleteReport: (reportCreatorId: number) => boolean;
  getProgressIcon: (type: JuniorProgressType) => { icon: string; color: string };
  reportDialog: boolean;
  deleteReportDialog: boolean;
  editingReportId: number | null;
  reportForm: AddOrUpdateJuniorReportBody;
  reportFormError: string;
  progressOptions: Array<{ title: string; value: JuniorProgressType }>;
  ratingFields: (keyof AddOrUpdateJuniorReportBody["ratings"])[];
}>();

const emit = defineEmits<{
  "open-create-report": [];
  "open-edit-report": [reportId: number];
  "open-delete-report": [reportId: number];
  "close-report": [];
  "submit-report": [];
  "close-delete-report": [];
  "confirm-delete-report": [];
  "update:report-form": [value: AddOrUpdateJuniorReportBody];
}>();

const { t } = useI18n();
const reportFormRef = ref();

const isGraduated = computed(() => Boolean(props.junior.graduation));
const progressRules = computed(() => [
  (value: JuniorProgressType | null | undefined) => value !== null && value !== undefined || t("Обязательное поле"),
]);
const commentRules = computed(() => [
  (value: unknown) => {
    const text = typeof value === "string" ? value : "";
    if (!text || text.length > 4096) {
      return t("Обязательное поле. Не более N символов", { n: 4096 });
    }
    return true;
  },
]);

function onReportDialogChange(value: boolean) {
  if (!value) {
    emit("close-report");
  }
}

async function onSubmitReport() {
  const validationResult = await reportFormRef.value?.validate?.();
  const isValid = typeof validationResult === "object"
    ? Boolean(validationResult?.valid)
    : Boolean(validationResult);

  if (!isValid) {
    return;
  }
  emit("submit-report");
}

function previousReport(report: JuniorReport): JuniorReport | null {
  const index = props.sortedReports.findIndex((item) => item.id === report.id);
  if (index < 0 || index === props.sortedReports.length - 1) {
    return null;
  }
  return props.sortedReports[index + 1] ?? null;
}

function updateReportFormField<K extends keyof AddOrUpdateJuniorReportBody>(
  key: K,
  value: AddOrUpdateJuniorReportBody[K],
) {
  emit("update:report-form", {
    ...props.reportForm,
    [key]: value,
  });
}

function updateRatingField(
  field: keyof AddOrUpdateJuniorReportBody["ratings"],
  value: number,
) {
  emit("update:report-form", {
    ...props.reportForm,
    ratings: {
      ...props.reportForm.ratings,
      [field]: value,
    },
  });
}
</script>
