<!--
  Dialog with active current project transfer request details.
-->
<template>
  <v-dialog v-model="dialogOpen" max-width="560" scrollable data-testid="project-transfer-request-dialog">
    <v-card>
      <v-card-item>
        <template #title>{{ t("Заявка на перевод") }}</template>
        <template #subtitle>{{ employeeName }}</template>
      </v-card-item>

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

        <v-alert
          type="info"
          variant="tonal"
          border="start"
          class="mb-4"
        >
          {{ t("Сотрудник уже планируется к переводу на другой проект.") }}
        </v-alert>

        <v-list v-if="request" density="compact">
          <v-list-item :title="t('Текущий проект')" :subtitle="request.fromProjectName" />
          <v-list-item :title="t('Новый проект')" :subtitle="request.toProjectName" />
          <v-list-item :title="t('Роль')" :subtitle="request.requestedProjectRole || t('Не задана')" />
          <v-list-item :title="t('Автор заявки')" :subtitle="request.createdByDisplayName" />
          <v-list-item :title="t('Согласующий')" :subtitle="request.approverDisplayName" />
          <v-list-item :title="t('Создана')" :subtitle="formatDateTime(request.createdAt)" />
        </v-list>

        <v-textarea
          v-if="canProcessRequest"
          v-model="comment"
          :label="t('Комментарий')"
          variant="underlined"
          rows="2"
          auto-grow
          class="mt-2"
          data-testid="project-transfer-request-comment"
        />
      </v-card-text>

      <v-card-actions>
        <v-btn
          v-if="canCancelRequest"
          color="warning"
          variant="text"
          :loading="actionLoading === 'cancel'"
          :disabled="actionLoading !== null"
          data-testid="project-transfer-request-cancel"
          @click="processRequest('cancel')"
        >
          {{ t("Отменить заявку") }}
        </v-btn>
        <v-spacer />
        <v-btn
          v-if="canApproveOrRejectRequest"
          color="error"
          variant="text"
          :loading="actionLoading === 'reject'"
          :disabled="actionLoading !== null"
          data-testid="project-transfer-request-reject"
          @click="processRequest('reject')"
        >
          {{ t("Отклонить") }}
        </v-btn>
        <v-btn
          v-if="canApproveOrRejectRequest"
          color="primary"
          variant="flat"
          :loading="actionLoading === 'approve'"
          :disabled="actionLoading !== null"
          data-testid="project-transfer-request-approve"
          @click="processRequest('approve')"
        >
          {{ t("Согласовать") }}
        </v-btn>
        <v-spacer />
        <v-btn variant="text" :disabled="actionLoading !== null" data-testid="project-transfer-request-close" @click="dialogOpen = false">
          {{ t("Закрыть") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { formatDateTime } from "@/lib/datetime";
import { errorUtils } from "@/lib/errors";
import { usePermissions } from "@/lib/permissions";
import {
  approveCurrentProjectTransferRequest,
  cancelCurrentProjectTransferRequest,
  rejectCurrentProjectTransferRequest,
  type CurrentProjectTransferRequest,
} from "@/services/employee.service";
import { useAuthStore } from "@/stores/auth";

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    employeeName: string;
    request: CurrentProjectTransferRequest | null;
  }>(),
  {
    modelValue: false,
    request: null,
  },
);

const emit = defineEmits<{
  (event: "update:modelValue", value: boolean): void;
  (event: "updated"): void;
}>();

const { t } = useI18n();
const authStore = useAuthStore();
const permissions = usePermissions();

const comment = ref("");
const errorMessage = ref("");
const actionLoading = ref<"approve" | "reject" | "cancel" | null>(null);

const dialogOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit("update:modelValue", value),
});

const canApproveOrRejectRequest = computed(() =>
  props.request?.canMakeDecision === true,
);

const canCancelRequest = computed(() =>
  Boolean(props.request) &&
  (props.request?.createdBy === authStore.employeeId || permissions.canUpdateCurrentProjectGlobally()),
);

const canProcessRequest = computed(() =>
  canApproveOrRejectRequest.value || canCancelRequest.value,
);

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      comment.value = "";
      errorMessage.value = "";
      actionLoading.value = null;
    }
  },
);

async function processRequest(action: "approve" | "reject" | "cancel") {
  if (!props.request || actionLoading.value !== null) {
    return;
  }
  actionLoading.value = action;
  errorMessage.value = "";
  const payload = { comment: comment.value.trim() || null };
  try {
    if (action === "approve") {
      await approveCurrentProjectTransferRequest(props.request.id, payload);
    } else if (action === "reject") {
      await rejectCurrentProjectTransferRequest(props.request.id, payload);
    } else {
      await cancelCurrentProjectTransferRequest(props.request.id, payload);
    }
    dialogOpen.value = false;
    emit("updated");
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
  } finally {
    actionLoading.value = null;
  }
}
</script>
