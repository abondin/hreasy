<!--
  Dialog that allows updating the employee's current project and role.
-->
<template>
  <v-dialog v-model="dialogOpen" max-width="560" scrollable persistent data-testid="project-assignment-dialog">
    <v-card>
      <v-card-item>
        <template #title>{{ t("Обновление текущего проекта") }}</template>
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
          v-if="activeTransferRequest"
          type="info"
          variant="tonal"
          border="start"
          class="mb-4"
          data-testid="project-assignment-active-transfer-request"
        >
          {{
            t("Сотрудник уже планируется к переводу на проект {project}. Согласующий: {approver}.", {
              project: activeTransferRequest.toProjectName,
              approver: activeTransferRequest.approverDisplayName,
            })
          }}
        </v-alert>

        <v-autocomplete
          v-model="selectedProjectId"
          data-testid="project-assignment-project"
          :items="projectItems"
          :label="t('Проекты')"
          :loading="dictionaryLoading || activeTransferRequestLoading"
          :disabled="dictionaryLoading || activeTransferRequestLoading || activeTransferRequest !== null"
          clearable
          item-title="name"
          item-value="id"
          variant="underlined"
          autofocus
        />

        <v-combobox
          v-model="roleOnProject"
          data-testid="project-assignment-role"
          :items="roleItems"
          :label="t('Роль')"
          :loading="dictionaryLoading"
          :disabled="dictionaryLoading || activeTransferRequestLoading || activeTransferRequest !== null"
          clearable
          variant="underlined"
          :rules="[validateRoleLength]"
        >
          <template #no-data>
            <v-list-item>
              <v-list-item-title class="text-body-2">
                {{ t("Элемент не найден") }}
                "<strong>{{ roleOnProject }}</strong>"
                {{ t("Нажмите") }} <kbd>enter</kbd> {{ t("чтобы создать") }}.
              </v-list-item-title>
            </v-list-item>
          </template>
        </v-combobox>

        <v-autocomplete
          v-if="transferApprovalRequired"
          v-model="selectedApproverId"
          data-testid="project-assignment-transfer-approver"
          :items="transferApprovers"
          :label="t('Согласующий')"
          :loading="transferApproversLoading"
          :disabled="transferApproversLoading || approvalRequestSending"
          :error-messages="transferApproversLoadError"
          clearable
          item-title="displayName"
          item-value="employeeId"
          variant="underlined"
        >
          <template #item="{ props: itemProps, item }">
            <v-list-item
              v-bind="itemProps"
              :subtitle="approverSubtitle(item)"
            />
          </template>
          <template #no-data>
            <v-list-item>
              <v-list-item-title>{{ t("Согласующие не найдены") }}</v-list-item-title>
            </v-list-item>
          </template>
        </v-autocomplete>

        <v-alert
          v-if="transferApprovalRequired"
          type="info"
          variant="tonal"
          border="start"
          class="mt-2"
          data-testid="project-assignment-transfer-approval-required"
        >
          {{ t("Для перевода сотрудника на выбранный проект требуется согласование.") }}
        </v-alert>

        <v-alert
          v-if="approvalRequestSent"
          type="success"
          variant="tonal"
          border="start"
          class="mt-4"
          data-testid="project-assignment-transfer-approval-sent"
        >
          {{ t("Заявка на согласование отправлена.") }}
        </v-alert>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" :disabled="saving || approvalRequestSending" data-testid="project-assignment-cancel" @click="cancel">
          {{ t("Отменить") }}
        </v-btn>
        <v-btn
          color="primary"
          :loading="saving || approvalRequestSending || activeTransferRequestLoading"
          :disabled="submitDisabled"
          data-testid="project-assignment-submit"
          @click="submit"
        >
          {{ t("Применить") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { withArchivedOptionById, withCurrentOptionById } from "@/lib/dict-options";
import { BusinessError, errorUtils } from "@/lib/errors";
import type {
  CurrentProjectDict,
  CurrentProjectTransferApprover,
  CurrentProjectTransferRequest,
} from "@/services/employee.service";
import {
  fetchCurrentProjectRoles,
  fetchProjects,
  type CurrentProjectRole,
  type ProjectDictDto,
} from "@/services/projects.service";
import {
  fetchActiveCurrentProjectTransferRequest,
  fetchCurrentProjectTransferApprovers,
  requestCurrentProjectTransferApproval,
  updateEmployeeCurrentProject,
} from "@/services/employee.service";

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    employeeId: number | null;
    employeeName: string;
    currentProject?: CurrentProjectDict | null;
  }>(),
  {
    modelValue: false,
    employeeId: null,
    currentProject: null,
  },
);

const emit = defineEmits<{
  (event: "update:modelValue", value: boolean): void;
  (event: "updated"): void;
}>();

const { t } = useI18n();

const CURRENT_PROJECT_TRANSFER_APPROVAL_REQUIRED =
  "errors.current_project.transfer_approval_required";
const CURRENT_PROJECT_TRANSFER_REQUEST_ALREADY_PENDING =
  "errors.current_project.transfer_request.already_pending";

const dialogOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit("update:modelValue", value),
});

const dictionaryLoading = ref(false);
const saving = ref(false);
const errorMessage = ref("");
const transferApprovalRequired = ref(false);
const transferApproversLoading = ref(false);
const transferApproversLoadError = ref("");
const transferApprovers = ref<CurrentProjectTransferApprover[]>([]);
const selectedApproverId = ref<number | null>(null);
const approvalRequestSending = ref(false);
const approvalRequestSent = ref(false);
const transferApprovalFromProjectId = ref<number | null>(null);
const transferApprovalToProjectId = ref<number | null>(null);
const activeTransferRequest = ref<CurrentProjectTransferRequest | null>(null);
const activeTransferRequestLoading = ref(false);

const projects = ref<ProjectDictDto[]>([]);
const projectRoles = ref<CurrentProjectRole[]>([]);

const selectedProjectId = ref<number | null>(null);
const roleOnProject = ref<string | null>(null);

const projectItems = computed(() => {
  const activeProjects = projects.value.filter((project) => project.active !== false);
  const currentProject = props.currentProject
    ? projects.value.find((project) => project.id === props.currentProject?.id) ?? {
      id: props.currentProject.id,
      name: props.currentProject.name,
      baId: -1,
      active: false,
    }
    : null;

  const withCurrent = withCurrentOptionById(activeProjects, currentProject);
  return withArchivedOptionById(withCurrent, selectedProjectId.value, (id) => ({
    id,
    name: `${t("Архив")} #${id}`,
    baId: -1,
    active: false,
  }));
});
const roleItems = computed(() =>
  projectRoles.value.map((role) => role.value),
);

const submitDisabled = computed(() =>
  activeTransferRequest.value !== null || activeTransferRequestLoading.value
    ? true
    : transferApprovalRequired.value
    ? approvalRequestSent.value || approvalRequestSending.value || transferApproversLoading.value || selectedApproverId.value === null
    : saving.value,
);

watch(
  () => dialogOpen.value,
  (open) => {
    if (open) {
      initialiseForm();
      void loadActiveTransferRequest();
      if (!projects.value.length || !projectRoles.value.length) {
        void loadDictionaries();
      }
    } else {
      resetState();
    }
  },
);

watch(
  () => props.currentProject,
  () => {
    if (dialogOpen.value) {
      initialiseForm();
      void loadActiveTransferRequest();
    }
  },
);

watch([selectedProjectId, roleOnProject], () => {
  resetTransferApprovalState();
});

function initialiseForm() {
  selectedProjectId.value = props.currentProject?.id ?? null;
  roleOnProject.value = props.currentProject?.role ?? null;
  errorMessage.value = "";
  activeTransferRequest.value = null;
  resetTransferApprovalState();
}

function resetState() {
  errorMessage.value = "";
  activeTransferRequest.value = null;
  activeTransferRequestLoading.value = false;
  resetTransferApprovalState();
  saving.value = false;
}

async function loadDictionaries() {
  dictionaryLoading.value = true;
  try {
    const [projectsResponse, rolesResponse] = await Promise.all([
      fetchProjects(),
      fetchCurrentProjectRoles(),
    ]);
    projects.value = projectsResponse;
    projectRoles.value = rolesResponse;
  } catch (error) {
    errorMessage.value = String(error);
  } finally {
    dictionaryLoading.value = false;
  }
}

async function loadActiveTransferRequest() {
  if (!props.employeeId) {
    return;
  }
  activeTransferRequestLoading.value = true;
  try {
    activeTransferRequest.value = await fetchActiveCurrentProjectTransferRequest(props.employeeId);
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
  } finally {
    activeTransferRequestLoading.value = false;
  }
}

function validateRoleLength(value: string | null): true | string {
  if (!value) {
    return true;
  }
  return value.length <= 64
    ? true
    : t("Не более N символов", { n: 64 }) as string;
}

async function submit() {
  if (!props.employeeId) {
    errorMessage.value = t("Профиль_недоступен");
    return;
  }

  if (activeTransferRequest.value !== null) {
    return;
  }

  if (transferApprovalRequired.value) {
    await submitTransferApprovalRequest();
    return;
  }

  saving.value = true;
  errorMessage.value = "";
  resetTransferApprovalState();

  const payload =
    selectedProjectId.value !== null
      ? {
          id: selectedProjectId.value,
          role: roleOnProject.value ?? null,
        }
      : undefined;

  try {
    await updateEmployeeCurrentProject(props.employeeId, payload);
    dialogOpen.value = false;
    emit("updated");
    if (
      payload?.role &&
      !projectRoles.value.some((role) => role.value === payload.role)
    ) {
      await loadCurrentProjectRoles();
    }
  } catch (error) {
    if (isTransferApprovalRequired(error)) {
      transferApprovalRequired.value = true;
      await loadTransferApprovers(error);
    } else if (isTransferRequestAlreadyPending(error)) {
      await loadActiveTransferRequest();
    } else {
      errorMessage.value = String(error);
    }
  } finally {
    saving.value = false;
  }
}

async function loadCurrentProjectRoles() {
  try {
    projectRoles.value = await fetchCurrentProjectRoles();
  } catch (error) {
    // silently ignore
    console.error(error);
  }
}

function cancel() {
  dialogOpen.value = false;
}

function isTransferApprovalRequired(error: unknown): boolean {
  return error instanceof BusinessError
    && error.code === CURRENT_PROJECT_TRANSFER_APPROVAL_REQUIRED;
}

function isTransferRequestAlreadyPending(error: unknown): boolean {
  return error instanceof BusinessError
    && error.code === CURRENT_PROJECT_TRANSFER_REQUEST_ALREADY_PENDING;
}

async function loadTransferApprovers(error: unknown) {
  if (!(error instanceof BusinessError) || !props.employeeId) {
    return;
  }
  const fromProjectId = Number(error.attrs?.fromProjectId);
  const newProjectId = Number(error.attrs?.toProjectId);
  if (!Number.isInteger(fromProjectId) || !Number.isInteger(newProjectId)) {
    transferApproversLoadError.value = t("Не удалось определить параметры перевода");
    return;
  }
  transferApprovalFromProjectId.value = fromProjectId;
  transferApprovalToProjectId.value = newProjectId;
  transferApproversLoading.value = true;
  transferApproversLoadError.value = "";
  try {
    transferApprovers.value = await fetchCurrentProjectTransferApprovers(props.employeeId, newProjectId);
    selectedApproverId.value = transferApprovers.value[0]?.employeeId ?? null;
  } catch (loadError) {
    transferApproversLoadError.value = errorUtils.shortMessage(loadError);
  } finally {
    transferApproversLoading.value = false;
  }
}

async function submitTransferApprovalRequest() {
  if (
    !props.employeeId
    || transferApprovalFromProjectId.value === null
    || transferApprovalToProjectId.value === null
    || selectedApproverId.value === null
  ) {
    errorMessage.value = t("Выберите согласующего для перевода.");
    return;
  }
  approvalRequestSending.value = true;
  errorMessage.value = "";
  try {
    await requestCurrentProjectTransferApproval(props.employeeId, {
      fromProjectId: transferApprovalFromProjectId.value,
      toProjectId: transferApprovalToProjectId.value,
      role: roleOnProject.value ?? null,
      approverEmployeeId: selectedApproverId.value,
    });
    approvalRequestSent.value = true;
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
  } finally {
    approvalRequestSending.value = false;
  }
}

function resetTransferApprovalState() {
  transferApprovalRequired.value = false;
  transferApprovers.value = [];
  transferApproversLoading.value = false;
  transferApproversLoadError.value = "";
  selectedApproverId.value = null;
  approvalRequestSending.value = false;
  approvalRequestSent.value = false;
  transferApprovalFromProjectId.value = null;
  transferApprovalToProjectId.value = null;
}

function approverSubtitle(approver: CurrentProjectTransferApprover): string {
  const type = approver.managerType === "project"
    ? t("Менеджер проекта")
    : approver.managerType === "business_account"
      ? t("Менеджер BA")
      : t("Руководитель отдела");
  return approver.email ? `${type} · ${approver.email}` : String(type);
}

onMounted(() => {
  if (dialogOpen.value) {
    void loadDictionaries();
  }
});
</script>
