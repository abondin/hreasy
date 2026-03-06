<template>
  <div class="mt-4">
    <v-card>
      <HREasyTableBase
        table-class="admin-employees-table text-truncate"
        :headers="headers"
        :items="filteredItems"
        height="70vh"
        :fixed-header="true"
        density="compact"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :hover="true"
        :sort-by="[{ key: 'displayName', order: 'asc' }]"
        :row-props="rowProps"
        @click:row="onClickRow"
      >
        <template #filters>
          <v-card-title class="d-flex ga-2 align-center flex-wrap">
            <v-btn icon="mdi-refresh" variant="text" :loading="loading" @click="load" />

            <v-tooltip v-if="permissions.canEditEmployees()" location="bottom">
              <template #activator="{ props: tooltipProps }">
                <v-btn
                  v-bind="tooltipProps"
                  icon="mdi-plus"
                  color="primary"
                  variant="text"
                  :disabled="loading"
                  @click="openCreate"
                />
              </template>
              <span>{{ t("Добавить информацию о сотруднике") }}</span>
            </v-tooltip>

            <v-tooltip v-if="permissions.canExportEmployees()" location="bottom">
              <template #activator="{ props: tooltipProps }">
                <v-btn
                  v-bind="tooltipProps"
                  icon="mdi-file-excel"
                  color="success"
                  variant="text"
                  :loading="exportLoading"
                  :disabled="loading"
                  @click="downloadExport"
                />
              </template>
              <span>
                {{ t("Экспорт в Excel") }}.
                {{ t("Результат выгрузки зависит только от фильтра ") }}{{ t("Скрыть уволенных") }}
              </span>
            </v-tooltip>
          </v-card-title>

          <v-card-text class="pb-0">
            <v-row dense>
              <v-col cols="12" lg="3">
                <v-text-field
                  v-model="search"
                  append-inner-icon="mdi-magnify"
                  :label="t('Поиск')"
                  variant="outlined"
                  density="compact"
                  hide-details
                  clearable
                />
              </v-col>
              <v-col cols="12" lg="3">
                <v-autocomplete
                  v-model="selectedDepartments"
                  :items="activeDepartments"
                  item-title="name"
                  item-value="id"
                  :label="t('Отдел')"
                  variant="outlined"
                  density="compact"
                  multiple
                  chips
                  clearable
                  hide-details
                />
              </v-col>
              <v-col cols="12" lg="3">
                <v-autocomplete
                  v-model="selectedProjects"
                  :items="activeProjects"
                  item-title="name"
                  item-value="id"
                  :label="t('Текущий проект')"
                  variant="outlined"
                  density="compact"
                  multiple
                  chips
                  clearable
                  hide-details
                />
              </v-col>
              <v-col cols="12" lg="2">
                <v-autocomplete
                  v-model="selectedPositions"
                  :items="activePositions"
                  item-title="name"
                  item-value="id"
                  :label="t('Позиция')"
                  variant="outlined"
                  density="compact"
                  multiple
                  chips
                  clearable
                  hide-details
                />
              </v-col>
              <v-col cols="12" lg="1" class="d-flex align-center">
                <v-checkbox
                  v-model="hideDismissed"
                  density="compact"
                  hide-details
                  :label="t('Скрыть уволенных')"
                />
              </v-col>
            </v-row>
          </v-card-text>
        </template>

      <template #before-table>
        <v-alert v-if="error" type="error" variant="tonal" border="start" class="mb-3">
          {{ error }}
        </v-alert>
      </template>

      <template #[`item.departmentId`]="{ item }">
        {{ getById(departments, item.departmentId) }}
      </template>
      <template #[`item.organizationId`]="{ item }">
        {{ getById(organizations, item.organizationId) }}
      </template>
      <template #[`item.positionId`]="{ item }">
        {{ getById(positions, item.positionId) }}
      </template>
      <template #[`item.currentProjectId`]="{ item }">
        <span>{{ getById(projects, item.currentProjectId) }}</span>
        <span v-if="item.currentProjectRole">({{ item.currentProjectRole }})</span>
      </template>
      <template #[`item.baId`]="{ item }">
        {{ getById(businessAccounts, item.baId) }}
      </template>
      <template #[`item.levelId`]="{ item }">
        {{ getById(levels, item.levelId) }}
      </template>
      <template #[`item.officeLocationId`]="{ item }">
        {{ getById(officeLocations, item.officeLocationId) }}
      </template>
      <template #[`item.birthday`]="{ item }">
        {{ formatDate(item.birthday) }}
      </template>
      <template #[`item.dateOfEmployment`]="{ item }">
        {{ formatDate(item.dateOfEmployment) }}
      </template>
      <template #[`item.dateOfDismissal`]="{ item }">
        {{ formatDate(item.dateOfDismissal) }}
        </template>
      </HREasyTableBase>
    </v-card>

    <v-dialog v-model="dialog" persistent max-width="1280">
      <AdminEmployeeForm
        :input="current"
        :departments="departments"
        :organizations="organizations"
        :projects="projects"
        :positions="positions"
        :levels="levels"
        :office-locations="officeLocations"
        @close="dialog = false"
        @saved="onSaved"
      />
    </v-dialog>

    <v-snackbar v-model="exportCompleted" timeout="5000">
      {{ t("Экспорт успешно завершён. Файл скачен.") }}
      <template #actions>
        <v-btn icon="mdi-close-circle-outline" variant="text" @click="exportCompleted = false" />
      </template>
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import AdminEmployeeForm from "@/views/admin/employees/components/AdminEmployeeForm.vue";
import { usePermissions } from "@/lib/permissions";
import { errorUtils } from "@/lib/errors";
import { formatDate } from "@/lib/datetime";
import {
  fetchBusinessAccounts,
  fetchDepartments,
  fetchLevels,
  fetchOfficeLocations,
  fetchOrganizations,
  fetchPositions,
  type DictItem,
} from "@/services/dict.service";
import { fetchProjects, type ProjectDictDto } from "@/services/projects.service";
import {
  exportAdminEmployees,
  listAdminEmployees,
  type EmployeeWithAllDetails,
} from "@/services/admin/admin-employee.service";

const { t } = useI18n();
const permissions = usePermissions();

const loading = ref(false);
const exportLoading = ref(false);
const exportCompleted = ref(false);
const dialog = ref(false);
const error = ref<string | null>(null);

const search = ref("");
const hideDismissed = ref(true);
const selectedProjects = ref<number[]>([]);
const selectedDepartments = ref<number[]>([]);
const selectedPositions = ref<number[]>([]);

const items = ref<EmployeeWithAllDetails[]>([]);
const current = ref<EmployeeWithAllDetails | null>(null);

const departments = ref<DictItem[]>([]);
const organizations = ref<DictItem[]>([]);
const positions = ref<DictItem[]>([]);
const levels = ref<DictItem[]>([]);
const officeLocations = ref<DictItem[]>([]);
const projects = ref<ProjectDictDto[]>([]);
const businessAccounts = ref<DictItem[]>([]);

const headers = computed(() => [
  { title: t("ФИО"), key: "displayName", width: "240px" },
  { title: t("Email"), key: "email", width: "240px" },
  { title: t("Текущий проект"), key: "currentProjectId", width: "240px" },
  { title: t("Бизнес Аккаунт"), key: "baId", width: "200px" },
  { title: t("Телефон"), key: "phone", width: "160px" },
  { title: t("Telegram"), key: "telegram", width: "160px" },
  { title: t("Дата трудоустройства"), key: "dateOfEmployment", width: "170px" },
  { title: t("День рождения"), key: "birthday", width: "150px" },
  { title: t("Организация"), key: "organizationId", width: "180px" },
  { title: t("Подразделение"), key: "departmentId", width: "240px" },
  { title: t("Позиция"), key: "positionId", width: "220px" },
  { title: t("Уровень экспертизы"), key: "levelId", width: "180px" },
  { title: t("Рабочее место"), key: "officeLocationId", width: "220px" },
  { title: t("Документ УЛ"), key: "documentFull", width: "220px" },
  { title: t("Адрес по регистрации"), key: "registrationAddress", width: "260px" },
  { title: t("Пол"), key: "sex", width: "120px" },
  { title: t("ФИО супруга/супруги"), key: "spouseName", width: "220px" },
  { title: t("Рабочий день"), key: "workDay", width: "180px" },
  { title: t("Место работы"), key: "workType", width: "160px" },
  { title: t("Город проживания"), key: "cityOfResidence", width: "180px" },
  { title: t("Дети"), key: "children", width: "180px" },
  { title: t("Семейный статус"), key: "familyStatus", width: "180px" },
  { title: t("Загранпаспорт"), key: "foreignPassport", width: "160px" },
  { title: t("Уровень английского"), key: "englishLevel", width: "180px" },
  { title: t("Skype"), key: "skype", width: "160px" },
  { title: t("Дата увольнения"), key: "dateOfDismissal", width: "160px" },
]);

const activeProjects = computed(() =>
  projects.value.filter((project) => project.active !== false),
);
const activeDepartments = computed(() =>
  departments.value.filter((department) => department.active !== false),
);
const activePositions = computed(() =>
  positions.value.filter((position) => position.active !== false),
);

const filteredItems = computed(() => {
  const q = search.value.trim().toLowerCase();
  return items.value.filter((item) => {
    if (hideDismissed.value && !item.active) {
      return false;
    }
    if (
      selectedProjects.value.length > 0
      && !selectedProjects.value.includes(item.currentProjectId ?? -1)
    ) {
      return false;
    }
    if (
      selectedPositions.value.length > 0
      && !selectedPositions.value.includes(item.positionId ?? -1)
    ) {
      return false;
    }
    if (
      selectedDepartments.value.length > 0
      && !selectedDepartments.value.includes(item.departmentId ?? -1)
    ) {
      return false;
    }
    if (!q) {
      return true;
    }
    return [item.displayName, item.email, item.skype, item.phone]
      .filter(Boolean)
      .join(" ")
      .toLowerCase()
      .includes(q);
  });
});

function rowProps() {
  return { class: "cursor-pointer" };
}

function getById(
  source: Array<{ id: number; name: string }>,
  id: number | null | undefined,
): string {
  if (!id) {
    return "-";
  }
  const found = source.find((entity) => entity.id === id);
  return found ? found.name : `${t("Не найден: ")}${id}`;
}

function openCreate(): void {
  current.value = null;
  dialog.value = true;
}

function onClickRow(
  _event: Event,
  payload: unknown,
): void {
  if (!permissions.canEditEmployees()) {
    return;
  }
  const selected = extractRow(payload);
  if (!selected) {
    return;
  }
  current.value = selected;
  dialog.value = true;
}

function extractRow(payload: unknown): EmployeeWithAllDetails | null {
  if (!payload || typeof payload !== "object") {
    return null;
  }
  if ("item" in payload) {
    const rowItem = payload.item;
    if (!rowItem) {
      return null;
    }
    if (typeof rowItem === "object" && "raw" in rowItem) {
      const rawRow = rowItem.raw;
      return isEmployeeWithAllDetails(rawRow) ? rawRow : null;
    }
    return isEmployeeWithAllDetails(rowItem) ? rowItem : null;
  }
  return isEmployeeWithAllDetails(payload) ? payload : null;
}

function isEmployeeWithAllDetails(value: unknown): value is EmployeeWithAllDetails {
  if (!value || typeof value !== "object") {
    return false;
  }
  return "id" in value && "displayName" in value;
}

async function load(): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    const [
      allEmployees,
      allDepartments,
      allOrganizations,
      allPositions,
      allLevels,
      allOfficeLocations,
      allProjects,
      allBusinessAccounts,
    ] = await Promise.all([
      listAdminEmployees(true),
      fetchDepartments(),
      fetchOrganizations(),
      fetchPositions(),
      fetchLevels(),
      fetchOfficeLocations(),
      fetchProjects(),
      fetchBusinessAccounts(),
    ]);

    items.value = allEmployees;
    departments.value = allDepartments;
    organizations.value = allOrganizations;
    positions.value = allPositions;
    levels.value = allLevels;
    officeLocations.value = allOfficeLocations;
    projects.value = allProjects;
    businessAccounts.value = allBusinessAccounts;
  } catch (e: unknown) {
    error.value = errorUtils.shortMessage(e);
  } finally {
    loading.value = false;
  }
}

async function onSaved(): Promise<void> {
  dialog.value = false;
  await load();
}

async function downloadExport(): Promise<void> {
  exportLoading.value = true;
  try {
    const blob = await exportAdminEmployees(!hideDismissed.value);
    const link = document.createElement("a");
    const url = window.URL.createObjectURL(blob);
    link.href = url;
    link.download = "AllEmployees.xlsx";
    link.click();
    window.URL.revokeObjectURL(url);
    exportCompleted.value = true;
  } catch (e: unknown) {
    error.value = errorUtils.shortMessage(e);
  } finally {
    exportLoading.value = false;
  }
}

load().catch(() => undefined);
</script>

<style scoped>
.admin-employees-table :deep(tbody tr:hover) {
  cursor: pointer;
}
</style>
