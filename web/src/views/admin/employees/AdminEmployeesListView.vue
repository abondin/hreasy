<template>
  <v-card class="mt-4">
    <v-card-title class="d-flex ga-2 align-center flex-wrap">
      <v-btn icon="mdi-refresh" variant="text" :loading="loading" @click="load" />
      <v-btn
        v-if="permissions.canEditEmployees()"
        icon="mdi-plus"
        color="primary"
        variant="text"
        :disabled="loading"
        @click="openCreate"
      />
      <v-btn
        v-if="permissions.canExportEmployees()"
        icon="mdi-file-excel"
        color="success"
        variant="text"
        :loading="exportLoading"
        @click="downloadExport"
      />
      <v-text-field v-model="search" density="compact" :label="t('Поиск')" hide-details class="ml-2" />
      <v-checkbox v-model="hideDismissed" density="compact" hide-details :label="t('Скрыть уволенных')" />
    </v-card-title>

    <v-alert v-if="error" type="error" variant="tonal">{{ error }}</v-alert>

    <v-data-table
      :headers="headers"
      :items="filteredItems"
      :loading="loading"
      :loading-text="t('Загрузка_данных')"
      :no-data-text="t('Отсутствуют данные')"
      :items-per-page="25"
      @click:row="onClickRow"
    />

    <v-dialog v-model="dialog" max-width="760">
      <v-card>
        <v-card-title>{{ current?.id ? t("Изменение карточки сотрудника") : t("Создание карточки сотрудника") }}</v-card-title>
        <v-card-text>
          <v-text-field v-model="form.displayName" :label="t('ФИО')" />
          <v-text-field v-model="form.email" :label="t('Email')" />
          <v-text-field v-model="form.telegram" :label="t('Telegram')" />
          <v-text-field v-model="form.currentProjectRole" :label="t('Роль')" />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="dialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="saving" @click="save">{{ t("Сохранить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { useI18n } from "vue-i18n";
import { usePermissions } from "@/lib/permissions";
import { errorUtils } from "@/lib/errors";
import {
  createAdminEmployee,
  exportAdminEmployees,
  listAdminEmployees,
  updateAdminEmployee,
  type CreateOrUpdateEmployeeBody,
  type EmployeeWithAllDetails,
} from "@/services/admin/admin-employee.service";

const { t } = useI18n();
const permissions = usePermissions();
const loading = ref(false);
const saving = ref(false);
const exportLoading = ref(false);
const dialog = ref(false);
const search = ref("");
const hideDismissed = ref(true);
const error = ref<string | null>(null);
const items = ref<EmployeeWithAllDetails[]>([]);
const current = ref<EmployeeWithAllDetails | null>(null);

const form = reactive<CreateOrUpdateEmployeeBody>({
  displayName: "",
  currentProjectRole: null,
  telegram: "",
  email: "",
});

const headers = computed(() => [
  { title: t("ФИО"), key: "displayName" },
  { title: t("Email"), key: "email" },
  { title: t("Отдел"), key: "departmentId" },
  { title: t("Позиция"), key: "positionId" },
  { title: t("Текущий проект"), key: "currentProjectId" },
  { title: t("Роль"), key: "currentProjectRole" },
]);

const filteredItems = computed(() => {
  const q = search.value.trim().toLowerCase();
  return items.value.filter((it) => {
    if (hideDismissed.value && !it.active) {
      return false;
    }
    if (!q) {
      return true;
    }
    return [it.displayName, it.email, it.telegram].filter(Boolean).join(" ").toLowerCase().includes(q);
  });
});

function fillForm(item?: EmployeeWithAllDetails): void {
  form.displayName = item?.displayName ?? "";
  form.email = item?.email ?? "";
  form.telegram = item?.telegram ?? "";
  form.currentProjectRole = item?.currentProjectRole ?? null;
}

function openCreate(): void {
  current.value = null;
  fillForm();
  dialog.value = true;
}

function onClickRow(_: unknown, row: { item: EmployeeWithAllDetails }): void {
  if (!permissions.canEditEmployees()) {
    return;
  }
  current.value = row.item;
  fillForm(row.item);
  dialog.value = true;
}

async function load(): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    items.value = await listAdminEmployees(true);
  } catch (e: unknown) {
    error.value = errorUtils.shortMessage(e);
  } finally {
    loading.value = false;
  }
}

async function save(): Promise<void> {
  saving.value = true;
  try {
    if (current.value?.id) {
      await updateAdminEmployee(current.value.id, form);
    } else {
      await createAdminEmployee(form);
    }
    dialog.value = false;
    await load();
  } catch (e: unknown) {
    error.value = errorUtils.shortMessage(e);
  } finally {
    saving.value = false;
  }
}

async function downloadExport(): Promise<void> {
  exportLoading.value = true;
  try {
    const blob = await exportAdminEmployees(!hideDismissed.value);
    const link = document.createElement("a");
    link.href = window.URL.createObjectURL(blob);
    link.download = "AllEmployees.xlsx";
    link.click();
  } finally {
    exportLoading.value = false;
  }
}

load().catch(() => undefined);
</script>
