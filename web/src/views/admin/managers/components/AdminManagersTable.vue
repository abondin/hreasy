<template>
  <v-card :data-testid="testId">
    <v-card-title class="d-flex align-center ga-2 flex-wrap px-6 pt-5 pb-2">
      <span>{{ resolvedTitle }}</span>
      <v-spacer />
      <v-btn
        icon="mdi-refresh"
        variant="text"
        :disabled="loading"
        :data-testid="`${testId}-refresh`"
        @click="load"
      />
      <v-btn
        v-if="editable"
        icon="mdi-plus"
        color="primary"
        variant="text"
        :disabled="loading"
        :data-testid="`${testId}-add`"
        @click="openCreate"
      />
    </v-card-title>

    <v-card-text :class="mode === 'full' ? 'px-6 pb-0 pt-0' : 'px-6 pb-5 pt-0'">
      <v-row v-if="mode === 'full'" density="comfortable" class="mb-2">
        <v-col cols="12" md="6" lg="4">
          <v-text-field
            v-model="filter.search"
            :label="t('Поиск')"
            append-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            hide-details
            clearable
            :data-testid="`${testId}-search`"
          />
        </v-col>
        <v-col cols="12" md="6" lg="4">
          <v-autocomplete
            v-model="filter.responsibilityObjectTypes"
            :items="responsibilityObjectTypeOptions"
            item-title="title"
            item-value="value"
            :label="t('Тип объекта')"
            variant="outlined"
            density="compact"
            multiple
            chips
            clearable
            hide-details
            :data-testid="`${testId}-object-types`"
          />
        </v-col>
      </v-row>

      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        border="start"
        class="mb-4"
      >
        {{ error }}
      </v-alert>

      <HREasyTableBase
        :table-class="mode === 'full' ? 'admin-managers-table text-truncate' : ''"
        :headers="headers"
        :items="filteredItems"
        :height="mode === 'full' ? '70vh' : undefined"
        :fixed-header="mode === 'full'"
        density="compact"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :row-props="editable ? rowProps : undefined"
        hover
        @click:row="onClickRow"
      >
        <template #[`item.responsibilityType`]="{ item }">
          {{ t(`MANAGER_RESPONSIBILITY_TYPE.${item.responsibilityType}`) }}
        </template>
        <template v-if="mode === 'full'" #[`item.responsibilityObject.type`]="{ item }">
          {{ t(`MANAGER_RESPONSIBILITY_OBJECT.${item.responsibilityObject.type}`) }}
        </template>
      </HREasyTableBase>
    </v-card-text>

    <v-dialog v-model="dialog" persistent max-width="720">
      <v-card>
        <v-card-title>{{ dialogMode === "create" ? t("Добавить") : t("Изменить") }}</v-card-title>
        <v-card-text>
          <v-form ref="formRef" @submit.prevent="save">
            <v-autocomplete
              v-if="dialogMode === 'create'"
              v-model="form.employee"
              :items="employees"
              item-title="displayName"
              item-value="id"
              :label="t('Сотрудник')"
              :rules="[requiredRule]"
              clearable
              variant="outlined"
            />

            <template v-if="dialogMode === 'create' && mode === 'full' && !selectedObject">
              <v-autocomplete
                v-model="form.responsibilityObjectType"
                :items="responsibilityObjectTypeOptions"
                item-title="title"
                item-value="value"
                :label="t('Тип объекта')"
                :rules="[requiredRule]"
                clearable
                variant="outlined"
              />

              <v-autocomplete
                v-if="form.responsibilityObjectType === 'project'"
                v-model="form.responsibilityObjectId"
                :items="activeProjects"
                item-title="name"
                item-value="id"
                :label="t('Проект')"
                :rules="[requiredRule]"
                clearable
                variant="outlined"
              />
              <v-autocomplete
                v-else-if="form.responsibilityObjectType === 'business_account'"
                v-model="form.responsibilityObjectId"
                :items="businessAccounts"
                item-title="name"
                item-value="id"
                :label="t('Бизнес аккаунт')"
                :rules="[requiredRule]"
                clearable
                variant="outlined"
              />
              <v-autocomplete
                v-else-if="form.responsibilityObjectType === 'department'"
                v-model="form.responsibilityObjectId"
                :items="departments"
                item-title="name"
                item-value="id"
                :label="t('Отдел')"
                :rules="[requiredRule]"
                clearable
                variant="outlined"
              />
            </template>

            <v-autocomplete
              v-model="form.responsibilityType"
              :items="responsibilityTypeOptions"
              item-title="title"
              item-value="value"
              :label="t('Основное направление')"
              :rules="[requiredRule]"
              variant="outlined"
            />

            <v-textarea
              v-model="form.comment"
              :label="t('Примечание')"
              :counter="256"
              :rules="[commentRule]"
              variant="outlined"
            />
          </v-form>

          <v-alert
            v-if="saveError"
            type="error"
            variant="tonal"
            border="start"
            class="mt-4"
          >
            {{ saveError }}
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            v-if="dialogMode === 'edit'"
            color="error"
            variant="text"
            :disabled="saving"
            @click="remove"
          >
            {{ t("Удаление") }}
          </v-btn>
          <v-btn variant="text" :disabled="saving" @click="dialog = false">
            {{ t("Отменить") }}
          </v-btn>
          <v-btn color="primary" :loading="saving" @click="save">
            {{ t("Сохранить") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { VForm } from "vuetify/components";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import { extractDataTableRow } from "@/lib/data-table";
import { errorUtils } from "@/lib/errors";
import type { DictItem } from "@/services/dict.service";
import { fetchBusinessAccounts, fetchDepartments } from "@/services/dict.service";
import type { Employee } from "@/services/employee.service";
import { listEmployees } from "@/services/employee.service";
import { fetchProjects, type ProjectDictDto } from "@/services/projects.service";
import {
  createManager,
  deleteManager,
  listManagers,
  listManagersByObject,
  managerResponsibilityObjectTypes,
  managerResponsibilityTypes,
  type Manager,
  type ManagerResponsibilityObjectId,
  type ManagerResponsibilityObjectType,
  type ManagerResponsibilityType,
  updateManager,
} from "@/services/admin/admin-manager.service";

type VFormInstance = InstanceType<typeof VForm>;

interface ManagerFilterState {
  search: string;
  responsibilityObjectTypes: ManagerResponsibilityObjectType[];
}

interface ManagerFormState {
  employee: number | null;
  responsibilityObjectType: ManagerResponsibilityObjectType | null;
  responsibilityObjectId: number | null;
  responsibilityType: ManagerResponsibilityType | null;
  comment: string;
}

const props = withDefaults(defineProps<{
  selectedObject?: ManagerResponsibilityObjectId | null;
  mode?: "full" | "compact";
  title?: string | null;
  editable?: boolean;
  testId?: string;
}>(), {
  selectedObject: null,
  mode: "full",
  title: null,
  editable: true,
  testId: "admin-managers-table",
});

const { t } = useI18n();

const loading = ref(false);
const saving = ref(false);
const error = ref("");
const saveError = ref("");
const dialog = ref(false);
const dialogMode = ref<"create" | "edit">("create");
const items = ref<Manager[]>([]);
const employees = ref<Employee[]>([]);
const projects = ref<ProjectDictDto[]>([]);
const businessAccounts = ref<DictItem[]>([]);
const departments = ref<DictItem[]>([]);
const current = ref<Manager | null>(null);
const formRef = ref<VFormInstance | null>(null);

const filter = reactive<ManagerFilterState>({
  search: "",
  responsibilityObjectTypes: [...managerResponsibilityObjectTypes],
});

const form = reactive<ManagerFormState>({
  employee: null,
  responsibilityObjectType: props.selectedObject?.type ?? "project",
  responsibilityObjectId: props.selectedObject?.id ?? null,
  responsibilityType: "organization",
  comment: "",
});

const resolvedTitle = computed(() => props.title ?? t("Менеджеры"));
const editable = computed(() => props.editable !== false);
const responsibilityTypeOptions = computed(() =>
  managerResponsibilityTypes.map((value) => ({
    value,
    title: t(`MANAGER_RESPONSIBILITY_TYPE.${value}`),
  })),
);
const responsibilityObjectTypeOptions = computed(() =>
  managerResponsibilityObjectTypes.map((value) => ({
    value,
    title: t(`MANAGER_RESPONSIBILITY_OBJECT.${value}`),
  })),
);
const activeProjects = computed(() => projects.value.filter((item) => item.active !== false));
const headers = computed<Array<{ title: string; key: string; width?: string }>>(() => {
  const result: Array<{ title: string; key: string; width?: string }> = [
    { title: t("Менеджер"), key: "employee.name" },
  ];
  if (props.mode === "full") {
    result.push({ title: t("Тип объекта"), key: "responsibilityObject.type", width: "180px" });
    result.push({ title: t("Объект"), key: "responsibilityObject.name", width: "220px" });
  }
  result.push({ title: t("Основное направление"), key: "responsibilityType", width: "220px" });
  result.push({ title: t("Примечание"), key: "comment" });
  return result;
});
const filteredItems = computed(() => {
  const query = filter.search.trim().toLowerCase();
  return items.value.filter((item) => {
    if (props.mode === "full" && filter.responsibilityObjectTypes.length > 0) {
      if (!filter.responsibilityObjectTypes.includes(item.responsibilityObject.type)) {
        return false;
      }
    }

    if (!query) {
      return true;
    }

    return [
      item.employee?.name,
      item.responsibilityObject?.name,
      item.comment,
    ]
      .filter(Boolean)
      .join(" ")
      .toLowerCase()
      .includes(query);
  });
});

const requiredRule = (value: unknown) => Boolean(value) || t("Обязательное поле");
const commentRule = (value: string) =>
  (!value || value.length <= 256) || t("Не более N символов", { n: 256 });

watch(
  () => props.selectedObject,
  () => {
    resetCreateDefaults();
    void load();
  },
  { immediate: true },
);

async function ensureDictionariesLoaded(): Promise<void> {
  const tasks: Promise<unknown>[] = [];
  if (employees.value.length === 0) {
    tasks.push(listEmployees().then((value) => { employees.value = value; }));
  }
  if (projects.value.length === 0) {
    tasks.push(fetchProjects().then((value) => { projects.value = value; }));
  }
  if (businessAccounts.value.length === 0) {
    tasks.push(fetchBusinessAccounts().then((value) => { businessAccounts.value = value; }));
  }
  if (departments.value.length === 0) {
    tasks.push(fetchDepartments().then((value) => { departments.value = value; }));
  }
  await Promise.all(tasks);
}

function rowProps() {
  return { class: "cursor-pointer" };
}

function resetCreateDefaults(): void {
  form.employee = null;
  form.responsibilityObjectType = props.selectedObject?.type ?? "project";
  form.responsibilityObjectId = props.selectedObject?.id ?? null;
  form.responsibilityType = "organization";
  form.comment = "";
}

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  try {
    items.value = props.selectedObject
      ? await listManagersByObject(props.selectedObject)
      : await listManagers();
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

async function openCreate(): Promise<void> {
  await ensureDictionariesLoaded();
  dialogMode.value = "create";
  current.value = null;
  resetCreateDefaults();
  saveError.value = "";
  dialog.value = true;
}

function openEdit(item: Manager): void {
  dialogMode.value = "edit";
  current.value = item;
  form.employee = item.employee.id;
  form.responsibilityObjectType = item.responsibilityObject.type;
  form.responsibilityObjectId = item.responsibilityObject.id;
  form.responsibilityType = item.responsibilityType;
  form.comment = item.comment ?? "";
  saveError.value = "";
  dialog.value = true;
}

function onClickRow(_event: Event, row: unknown): void {
  if (!editable.value) {
    return;
  }
  const item = extractDataTableRow<Manager>(row);
  if (item) {
    openEdit(item);
  }
}

async function save(): Promise<void> {
  const validation = await formRef.value?.validate();
  if (validation && !validation.valid) {
    return;
  }
  if (!form.responsibilityType) {
    return;
  }

  saving.value = true;
  saveError.value = "";
  try {
    if (dialogMode.value === "create") {
      await createManager({
        employee: form.employee,
        responsibilityObjectType: props.selectedObject?.type ?? form.responsibilityObjectType ?? "project",
        responsibilityObjectId: props.selectedObject?.id ?? form.responsibilityObjectId,
        responsibilityType: form.responsibilityType,
        comment: form.comment || undefined,
      });
    } else if (current.value) {
      await updateManager(current.value.id, {
        responsibilityType: form.responsibilityType,
        comment: form.comment || undefined,
      });
    }
    dialog.value = false;
    await load();
  } catch (saveRequestError) {
    saveError.value = errorUtils.shortMessage(saveRequestError);
  } finally {
    saving.value = false;
  }
}

async function remove(): Promise<void> {
  if (!current.value) {
    return;
  }
  saving.value = true;
  saveError.value = "";
  try {
    await deleteManager(current.value.id);
    dialog.value = false;
    await load();
  } catch (removeError) {
    saveError.value = errorUtils.shortMessage(removeError);
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.admin-managers-table :deep(tbody tr:hover) {
  cursor: pointer;
}
</style>
