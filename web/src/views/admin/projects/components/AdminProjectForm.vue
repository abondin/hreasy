<template>
  <v-card data-testid="admin-project-form">
    <v-card-title>
      {{ isCreateMode ? t("Создание проекта") : t("Изменение проекта") }}
    </v-card-title>
    <v-card-text>
      <v-form ref="formRef" @submit.prevent="submit">
        <v-select
          v-model="form.departmentId"
          :items="departments"
          item-title="name"
          item-value="id"
          :label="t('Отдел')"
          clearable
          variant="outlined"
        />

        <v-select
          v-model="form.baId"
          :items="businessAccountsWithCurrent"
          item-title="name"
          item-value="id"
          :label="t('Бизнес аккаунт')"
          clearable
          variant="outlined"
        />

        <v-text-field
          v-model="form.externalId"
          :label="t('Внешний идентификатор')"
          :counter="255"
          variant="outlined"
          data-testid="admin-project-external-id"
        />

        <v-text-field
          v-model="form.name"
          :label="t('Наименование')"
          :counter="255"
          :rules="requiredTextRules"
          variant="outlined"
        />

        <v-text-field
          v-model="form.customer"
          :label="t('Заказчик')"
          :counter="255"
          :rules="requiredTextRules"
          variant="outlined"
        />

        <my-date-form-component
          v-model="form.planStartDate"
          :label="t('Планируемая дата начала')"
          :rules="dateRules"
        />
        <my-date-form-component
          v-model="form.startDate"
          :label="t('Фактическая дата начала проекта')"
          :rules="dateRules"
        />
        <my-date-form-component
          v-model="form.planEndDate"
          :label="t('Планируемая дата окончания проекта')"
          :rules="dateRules"
        />
        <my-date-form-component
          v-model="form.endDate"
          :label="t('Фактическая дата окончания проекта')"
          :rules="dateRules"
        />

        <markdown-text-editor
          v-model="form.info"
          :label="t('Информация о проекте (Markdown)')"
          :counter="4000"
        />

        <v-divider class="my-4" />
        <div class="d-flex align-center mb-3">
          <div class="text-h6">{{ t("Направления работ") }}</div>
          <v-spacer />
          <v-btn prepend-icon="mdi-plus" variant="text" data-testid="admin-project-add-workstream" @click="addWorkstream">
            {{ t("Добавить") }}
          </v-btn>
        </div>
        <v-table class="admin-project-workstreams-table" density="compact">
          <thead>
            <tr>
              <th>{{ t("Наименование") }}</th>
              <th>{{ t("Внешний идентификатор") }}</th>
              <th>{{ t("Описание") }}</th>
              <th class="admin-project-workstream-actions" />
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(workstream, index) in form.workstreams"
              :key="workstream.key"
              :data-testid="`admin-project-workstream-${index}`"
            >
              <td>
                <v-text-field
                  v-model="workstream.displayName"
                  :counter="255"
                  :rules="requiredTextRules"
                  variant="outlined"
                  density="compact"
                  hide-details="auto"
                />
              </td>
              <td>
                <v-text-field
                  v-model="workstream.externalId"
                  :counter="255"
                  variant="outlined"
                  density="compact"
                  hide-details="auto"
                />
              </td>
              <td>
                <v-textarea
                  v-model="workstream.description"
                  rows="1"
                  auto-grow
                  variant="outlined"
                  density="compact"
                  hide-details
                />
              </td>
              <td>
                <v-btn
                  icon="mdi-delete"
                  color="error"
                  variant="text"
                  :aria-label="t('Удалить')"
                  @click="form.workstreams.splice(index, 1)"
                />
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-form>

      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        border="start"
        class="mt-4"
      >
        {{ error }}
      </v-alert>
    </v-card-text>
    <v-card-actions>
      <v-spacer />
      <v-btn variant="text" :disabled="saving" @click="$emit('close')">
        {{ t("Закрыть") }}
      </v-btn>
      <v-btn color="primary" :loading="saving" @click="submit">
        {{ isCreateMode ? t("Создать") : t("Изменить") }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { VForm } from "vuetify/components";
import type { DictItem } from "@/services/dict.service";
import { errorUtils } from "@/lib/errors";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import MarkdownTextEditor from "@/components/shared/MarkdownTextEditor.vue";
import {
  createAdminProject,
  type AdminProjectInfo,
  type CreateOrUpdateProjectBody,
  updateAdminProject,
} from "@/services/admin/admin-project.service";
import type { ProjectWorkstream } from "@/services/projects.service";

type VFormInstance = InstanceType<typeof VForm>;

/** Form state for admin project create/update dialogs. */
interface ProjectFormState {
  externalId: string;
  name: string;
  startDate: string;
  endDate: string;
  planStartDate: string;
  planEndDate: string;
  customer: string;
  departmentId?: number;
  baId: number | null;
  info: string;
  workstreams: WorkstreamFormState[];
}

interface WorkstreamFormState extends ProjectWorkstream {
  key: string;
}

const props = defineProps<{
  input?: AdminProjectInfo | null;
  departments: DictItem[];
  businessAccounts: DictItem[];
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved", projectId: number): void;
}>();

const { t } = useI18n();

const formRef = ref<VFormInstance | null>(null);
const saving = ref(false);
const error = ref("");

const form = reactive<ProjectFormState>({
  externalId: "",
  name: "",
  startDate: "",
  endDate: "",
  planStartDate: "",
  planEndDate: "",
  customer: "",
  departmentId: undefined,
  baId: null,
  info: "",
  workstreams: [],
});

let workstreamKey = 0;

const isCreateMode = computed(() => !props.input?.id);
const businessAccountsWithCurrent = computed(() => {
  const items = [...props.businessAccounts];
  const current = props.input?.businessAccount;
  if (current && !items.some((item) => item.id === current.id)) {
    items.push(current);
  }
  return items;
});

const requiredTextRules = computed(() => [
  (value: string) =>
    Boolean(value && value.length <= 255)
    || t("Обязательное поле. Не более N символов", { n: 255 }),
]);
const dateRules = computed(() => [
  (value: unknown) => !value || typeof value === "string" || t("Дата в формате ДД.ММ.ГГ"),
]);

watch(
  () => props.input,
  () => {
    resetForm();
  },
  { immediate: true },
);

function resetForm(): void {
  error.value = "";
  form.externalId = props.input?.externalId ?? "";
  form.name = props.input?.name ?? "";
  form.startDate = props.input?.startDate ?? "";
  form.endDate = props.input?.endDate ?? "";
  form.planStartDate = props.input?.planStartDate ?? "";
  form.planEndDate = props.input?.planEndDate ?? "";
  form.customer = props.input?.customer ?? "";
  form.departmentId = props.input?.department?.id;
  form.baId = props.input?.businessAccount?.id ?? null;
  form.info = props.input?.info ?? "";
  form.workstreams = (props.input?.workstreams ?? []).map((workstream) => ({
    ...workstream,
    externalId: workstream.externalId ?? "",
    description: workstream.description ?? "",
    key: String(workstream.id ?? `new-${workstreamKey++}`),
  }));
  formRef.value?.resetValidation();
}

function buildPayload(): CreateOrUpdateProjectBody {
  return {
    externalId: form.externalId.trim() || undefined,
    name: form.name.trim(),
    customer: form.customer.trim(),
    startDate: form.startDate || undefined,
    endDate: form.endDate || undefined,
    planStartDate: form.planStartDate || undefined,
    planEndDate: form.planEndDate || undefined,
    departmentId: form.departmentId,
    baId: form.baId,
    info: form.info || undefined,
    workstreams: form.workstreams.map((workstream) => ({
      id: workstream.id,
      externalId: workstream.externalId?.trim() || undefined,
      displayName: workstream.displayName.trim(),
      description: workstream.description?.trim() || undefined,
    })),
  };
}

function addWorkstream(): void {
  form.workstreams.push({
    key: `new-${workstreamKey++}`,
    externalId: "",
    displayName: "",
    description: "",
  });
}

async function submit(): Promise<void> {
  const validation = await formRef.value?.validate();
  if (validation && !validation.valid) {
    return;
  }

  saving.value = true;
  error.value = "";
  try {
    const payload = buildPayload();
    const projectId = props.input?.id
      ? await updateAdminProject(props.input.id, payload)
      : await createAdminProject(payload);
    emit("saved", projectId);
  } catch (submitError) {
    error.value = errorUtils.shortMessage(submitError);
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.admin-project-workstream-actions {
  width: 48px;
}

.admin-project-workstreams-table td {
  vertical-align: top;
}
</style>
