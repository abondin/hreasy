<!--
  Dialog with active current project transfer request details.
-->
<template>
  <v-dialog v-model="dialogOpen" max-width="560" scrollable data-testid="project-transfer-request-dialog">
    <v-card>
      <v-card-item>
        <template #title>
          <div class="d-flex align-center ga-2">
            <span>{{ t("Заявка на перевод") }}</span>
            <v-chip v-if="request" size="small" color="warning" variant="tonal">
              {{ t("Ожидает согласования") }}
            </v-chip>
          </div>
        </template>
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
          density="compact"
          class="mb-5"
        >
          {{ t("Сотрудник уже планируется к переводу на другой проект.") }}
        </v-alert>

        <property-list v-if="request" variant="aligned" density="compact" label-width="156px">
          <profile-summary-item
            v-for="field in requestFields"
            :key="field.label"
            :label="field.label"
          >
            {{ field.value }}
          </profile-summary-item>
        </property-list>

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
        <v-spacer />
        <v-btn variant="text" :disabled="actionLoading !== null" data-testid="project-transfer-request-close" @click="dialogOpen = false">
          {{ t("Закрыть") }}
        </v-btn>
        <v-menu v-if="requestActions.length > 0" location="top end">
          <template #activator="{ props: menuProps }">
            <v-btn
              v-bind="menuProps"
              color="primary"
              variant="flat"
              :loading="actionLoading !== null"
              :disabled="actionLoading !== null"
              append-icon="mdi-chevron-up"
              data-testid="project-transfer-request-actions"
            >
              {{ t("Действия") }}
            </v-btn>
          </template>
          <v-list density="compact">
            <v-list-item
              v-for="action in requestActions"
              :key="action.type"
              :title="action.title"
              :base-color="action.color"
              :prepend-icon="action.icon"
              :data-testid="action.testId"
              @click="processRequest(action.type)"
            />
          </v-list>
        </v-menu>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import PropertyList from "@/components/shared/PropertyList.vue";
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
import ProfileSummaryItem from "@/views/profile/components/ProfileSummaryItem.vue";

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

type RequestAction = {
  type: "approve" | "reject" | "cancel";
  title: string;
  color: string;
  icon: string;
  testId: string;
};

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

const requestFields = computed(() => {
  if (!props.request) {
    return [];
  }
  return [
    { label: t("Текущий проект"), value: props.request.fromProjectName },
    { label: t("Новый проект"), value: props.request.toProjectName },
    { label: t("Роль"), value: props.request.requestedProjectRole || t("Не задана") },
    { label: t("Автор заявки"), value: props.request.createdByDisplayName },
    { label: t("Согласующий"), value: props.request.approverDisplayName },
    { label: t("Создана"), value: formatDateTime(props.request.createdAt) },
    { label: t("Действует до"), value: formatDateTime(props.request.expiresAt) },
  ];
});

const requestActions = computed<RequestAction[]>(() => {
  const actions: RequestAction[] = [];
  if (canApproveOrRejectRequest.value) {
    actions.push({
      type: "approve",
      title: t("Согласовать"),
      color: "success",
      icon: "mdi-check",
      testId: "project-transfer-request-approve",
    });
    actions.push({
      type: "reject",
      title: t("Отклонить"),
      color: "error",
      icon: "mdi-close",
      testId: "project-transfer-request-reject",
    });
  }
  if (canCancelRequest.value) {
    actions.push({
      type: "cancel",
      title: t("Отменить заявку"),
      color: "warning",
      icon: "mdi-cancel",
      testId: "project-transfer-request-cancel",
    });
  }
  return actions;
});

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
