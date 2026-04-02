<template>
  <v-card class="d-flex flex-column h-100" :data-testid="testId">
    <v-card-item v-if="mode === 'compact' && (title || editable)">
      <template #title>
        <span v-if="title">{{ title }}</span>
      </template>
      <template #append>
        <table-toolbar-actions
          v-if="editable"
          :disabled="loading"
          show-add
          :add-label="t('Добавить')"
          @add="openCreate"
        />
      </template>
    </v-card-item>
    <v-card-text :class="mode === 'full' ? 'px-6 pt-4 pb-2 d-flex flex-column flex-grow-1 min-h-0' : 'px-6 pb-5 pt-4'">
      <AdaptiveFilterBar
        :items="filterBarItems"
        :has-right-actions="editable"
        v-if="mode === 'full'"
      >
        <template #left-actions>
          <table-toolbar-actions
            :disabled="loading"
            show-refresh
            :refresh-label="t('Обновить данные')"
            @refresh="load"
          />
        </template>

        <template #filter-search>
          <v-text-field
            v-model="filter.search"
            :label="t('Поиск')"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            hide-details
            clearable
            :data-testid="`${testId}-search`"
          />
        </template>

        <template #filter-object-types>
          <v-autocomplete
            v-model="filter.responsibilityObjectTypes"
            :items="responsibilityObjectTypeOptions"
            item-title="title"
            item-value="value"
            :label="t('Тип объекта')"
            variant="outlined"
            density="compact"
            multiple
            clearable
            hide-details
            :data-testid="`${testId}-object-types`"
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="filter.responsibilityObjectTypes.length"
                :label="getFilterSelectionLabel(item)"
                :visible-count="3"
              />
            </template>
          </v-autocomplete>
        </template>

        <template #filter-department>
          <v-autocomplete
            v-model="filter.departments"
            :items="departments"
            item-title="name"
            item-value="id"
            :label="t('Отдел')"
            variant="outlined"
            density="compact"
            multiple
            clearable
            hide-details
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="filter.departments.length"
                :label="getEntityFilterSelectionLabel(item)"
                :visible-count="2"
              />
            </template>
          </v-autocomplete>
        </template>

        <template #filter-ba>
          <v-autocomplete
            v-model="filter.businessAccounts"
            :items="businessAccounts"
            item-title="name"
            item-value="id"
            :label="t('Бизнес аккаунт')"
            variant="outlined"
            density="compact"
            multiple
            clearable
            hide-details
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="filter.businessAccounts.length"
                :label="getEntityFilterSelectionLabel(item)"
                :visible-count="2"
              />
            </template>
          </v-autocomplete>
        </template>

        <template #filter-current-project>
          <v-autocomplete
            v-model="filter.currentProjects"
            :items="activeProjects"
            item-title="name"
            item-value="id"
            :label="t('Текущий проект')"
            variant="outlined"
            density="compact"
            multiple
            clearable
            hide-details
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="filter.currentProjects.length"
                :label="getEntityFilterSelectionLabel(item)"
                :visible-count="2"
              />
            </template>
          </v-autocomplete>
        </template>

        <template #right-actions>
          <table-toolbar-actions
            v-if="editable"
            :disabled="loading"
            show-add
            :add-label="t('Добавить')"
            @add="openCreate"
          />
        </template>
      </AdaptiveFilterBar>


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
        :height="mode === 'full' ? 'fill' : undefined"
        :fixed-header="mode === 'full'"
        density="compact"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :row-props="editable ? rowProps : undefined"
        hover
        @click:row="onClickRow"
      >
        <template #[`item.employee.name`]="{ item }">
          <div class="d-flex align-center ga-2 min-width-0">
            <span class="text-truncate">{{ item.employee?.name }}</span>
            <div
              v-if="editable && mode === 'compact'"
              class="manager-row-delete-slot d-inline-flex align-center justify-center flex-shrink-0"
            >
              <v-btn
                icon="mdi-delete"
                size="x-small"
                variant="text"
                color="error"
                class="manager-row-delete"
                :aria-label="t('Удалить')"
                @click.stop="openDeleteDialog(item)"
              />
            </div>
          </div>
        </template>
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

    <v-dialog v-model="deleteDialog" max-width="480">
      <v-card>
        <v-card-title>{{ t("Удалить") }}</v-card-title>
        <v-card-text>
          {{ t("Вы уверены, что хотите удалить менеджера?") }}
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="saving" @click="deleteDialog = false">
            {{ t("Отменить") }}
          </v-btn>
          <v-btn color="error" :loading="saving" @click="confirmDelete">
            {{ t("Удалить") }}
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
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
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
  departments: number[];
  businessAccounts: number[];
  currentProjects: number[];
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
const deleteDialog = ref(false);
const dialogMode = ref<"create" | "edit">("create");
const items = ref<Manager[]>([]);
const employees = ref<Employee[]>([]);
const projects = ref<ProjectDictDto[]>([]);
const businessAccounts = ref<DictItem[]>([]);
const departments = ref<DictItem[]>([]);
const current = ref<Manager | null>(null);
const pendingDelete = ref<Manager | null>(null);
const formRef = ref<VFormInstance | null>(null);

const filter = reactive<ManagerFilterState>({
  search: "",
  responsibilityObjectTypes: [...managerResponsibilityObjectTypes],
  departments: [],
  businessAccounts: [],
  currentProjects: [],
});

const form = reactive<ManagerFormState>({
  employee: null,
  responsibilityObjectType: props.selectedObject?.type ?? "project",
  responsibilityObjectId: props.selectedObject?.id ?? null,
  responsibilityType: "organization",
  comment: "",
});

const editable = computed(() => props.editable !== false);
function getFilterSelectionLabel(item: unknown): string {
  if (typeof item === "string") {
    return item;
  }
  if (item && typeof item === "object" && "title" in item && typeof item.title === "string") {
    return item.title;
  }
  return "";
}

function getEntityFilterSelectionLabel(item: unknown): string {
  if (typeof item === "string") {
    return item;
  }
  if (item && typeof item === "object" && "name" in item && typeof item.name === "string") {
    return item.name;
  }
  return "";
}

const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: filter.search.trim().length > 0, grow: true },
  { id: "object-types", minWidth: 420, active: filter.responsibilityObjectTypes.length > 0 },
  { id: "department", minWidth: 320, active: filter.departments.length > 0 },
  { id: "ba", minWidth: 320, active: filter.businessAccounts.length > 0 },
  { id: "current-project", minWidth: 320, active: filter.currentProjects.length > 0 },
]);

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
    const employee = employees.value.find((employeeItem) => employeeItem.id === item.employee?.id);
    if (props.mode === "full" && filter.responsibilityObjectTypes.length > 0) {
      if (!filter.responsibilityObjectTypes.includes(item.responsibilityObject.type)) {
        return false;
      }
    }

    if (filter.departments.length > 0 && !filter.departments.includes(employee?.department?.id ?? -1)) {
      return false;
    }

    if (filter.businessAccounts.length > 0 && !filter.businessAccounts.includes(employee?.ba?.id ?? -1)) {
      return false;
    }

    if (filter.currentProjects.length > 0 && !filter.currentProjects.includes(employee?.currentProject?.id ?? -1)) {
      return false;
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
    const [loadedItems] = await Promise.all([
      props.selectedObject
        ? listManagersByObject(props.selectedObject)
        : listManagers(),
      ensureDictionariesLoaded(),
    ]);
    items.value = loadedItems;
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

function openDeleteDialog(item: Manager): void {
  pendingDelete.value = item;
  saveError.value = "";
  deleteDialog.value = true;
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

async function confirmDelete(): Promise<void> {
  if (!pendingDelete.value) {
    return;
  }
  saving.value = true;
  saveError.value = "";
  try {
    await deleteManager(pendingDelete.value.id);
    deleteDialog.value = false;
    pendingDelete.value = null;
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

.manager-row-delete-slot {
  width: 24px;
}

:deep(tbody tr .manager-row-delete) {
  opacity: 0;
  pointer-events: none;
  transition: opacity 120ms ease-in-out;
}

:deep(tbody tr:hover .manager-row-delete),
:deep(tbody tr:focus-within .manager-row-delete) {
  opacity: 1;
  pointer-events: auto;
}
</style>
