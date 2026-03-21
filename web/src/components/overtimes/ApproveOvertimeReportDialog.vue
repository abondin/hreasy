<template>
  <v-dialog v-model="dialog" max-width="700">
    <template #activator="{ props }">
      <v-tooltip location="bottom">
        <template #activator="{ props: tooltipProps }">
          <div v-bind="tooltipProps">
            <v-btn-toggle>
              <v-btn
                icon="mdi-checkbox-marked-circle-outline"
                :disabled="approveDisabled"
                @click="approveNoDialog"
              />
              <v-btn
                v-bind="props"
                icon="mdi-dots-horizontal"
                :disabled="approveDisabled"
              />
            </v-btn-toggle>
          </div>
        </template>
        <span>{{ t("Согласование овертаймов") }}</span>
      </v-tooltip>
    </template>

    <v-card>
      <v-card-title>{{ t("Согласование овертаймов") }}</v-card-title>
      <v-card-text>
        <v-textarea
          v-model="comment"
          autofocus
          :label="t('Комментарий')"
          rows="4"
          :counter="1024"
        />

        <v-alert v-if="errorMessage" type="error" variant="tonal" class="mt-2">
          {{ errorMessage }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-btn @click="closeDialog">{{ t("Закрыть") }}</v-btn>
        <v-spacer />
        <v-btn color="success" :loading="submitting" @click="approve">
          {{ t("Согласовать") }}
        </v-btn>
        <v-btn color="error" :loading="submitting" @click="decline">
          {{ t("Отклонить") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { errorUtils } from "@/lib/errors";
import {
  approveOvertimeReport,
  declineOvertimeReport,
  type ApprovalDecision,
  type OvertimeReport,
} from "@/services/overtime.service";

const props = defineProps<{
  employeeId: number;
  periodId: number;
  periodClosed: boolean;
  previousDecision?: ApprovalDecision | null;
}>();

const emit = defineEmits<{
  submitted: [report: OvertimeReport];
  close: [];
}>();

const { t } = useI18n();

const dialog = ref(false);
const comment = ref<string | null>(null);
const errorMessage = ref<string | null>(null);
const submitting = ref(false);

const approveDisabled = computed(() => {
  return Boolean(
    props.periodClosed ||
      (props.previousDecision?.decision === "APPROVED" && !props.previousDecision.outdated),
  );
});

watch(dialog, (value) => {
  if (value) {
    errorMessage.value = null;
    comment.value = null;
  } else {
    emit("close");
  }
});

function validateCommentLength(): string | null {
  if (comment.value && comment.value.length > 1024) {
    return t("Не более N символов", { n: 1024 });
  }
  return null;
}

async function approve(): Promise<void> {
  const validationError = validateCommentLength();
  errorMessage.value = validationError;
  if (validationError) {
    return;
  }

  submitting.value = true;
  try {
    const report = await approveOvertimeReport(
      props.employeeId,
      props.periodId,
      comment.value,
      props.previousDecision?.id ?? null,
    );
    emit("submitted", report);
    closeDialog();
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
  } finally {
    submitting.value = false;
  }
}

async function decline(): Promise<void> {
  const validationError = validateCommentLength();
  errorMessage.value = validationError;
  if (validationError) {
    return;
  }

  if (!comment.value || comment.value.trim().length === 0) {
    errorMessage.value = t("Комментарий обязателен при отклонении");
    return;
  }

  submitting.value = true;
  try {
    const report = await declineOvertimeReport(
      props.employeeId,
      props.periodId,
      comment.value,
      props.previousDecision?.id ?? null,
    );
    emit("submitted", report);
    closeDialog();
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
  } finally {
    submitting.value = false;
  }
}

async function approveNoDialog(): Promise<void> {
  submitting.value = true;
  try {
    const report = await approveOvertimeReport(
      props.employeeId,
      props.periodId,
      null,
      props.previousDecision?.id ?? null,
    );
    emit("submitted", report);
    closeDialog();
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
    dialog.value = true;
  } finally {
    submitting.value = false;
  }
}

function closeDialog(): void {
  dialog.value = false;
}
</script>
