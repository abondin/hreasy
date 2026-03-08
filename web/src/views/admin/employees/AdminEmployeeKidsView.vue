<template>
  <v-card class="mt-4">
    <HREasyTableBase
      table-class="admin-kids-table text-truncate"
      :headers="headers"
      :items="filteredItems"
      height="62vh"
      fixed-header
      :loading="loading"
      :loading-text="t('Загрузка_данных')"
      :no-data-text="t('Отсутствуют данные')"
      density="compact"
      hover
      :sort-by="[{ key: 'displayName', order: 'asc' }]"
      :row-props="rowProps"
      @click:row="onClickRow"
    >
      <template #filters>
        <v-card-title class="d-flex ga-3 align-center flex-wrap">
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
            <span>{{ t("Добавить информацию о ребёнке") }}</span>
          </v-tooltip>
          <v-text-field
            v-model="search"
            append-inner-icon="mdi-magnify"
            density="compact"
            :label="t('Поиск')"
            variant="outlined"
            hide-details
            clearable
            class="kids-search-field"
          />
          <v-checkbox
            v-model="hideDismissed"
            density="compact"
            hide-details
            :label="t('Скрыть детей уволенных сотрудников')"
            class="ml-auto"
          />
        </v-card-title>
      </template>

      <template #before-table>
        <v-alert v-if="error" type="error" variant="tonal" border="start" class="mb-3">
          {{ error }}
        </v-alert>
      </template>

      <template #[`item.birthday`]="{ item }">
        {{ formatDate(item.birthday) }}
      </template>
      <template #[`item.parent.active`]="{ item }">
        {{ item.parent?.active ? t("Нет") : t("Да") }}
      </template>
    </HREasyTableBase>

    <v-dialog v-model="dialog" persistent max-width="760">
      <AdminEmployeeKidForm
        :input="current"
        :employees="employees"
        @close="dialog = false"
        @saved="onSaved"
      />
    </v-dialog>
  </v-card>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import AdminEmployeeKidForm from "@/views/admin/employees/components/AdminEmployeeKidForm.vue";
import { usePermissions } from "@/lib/permissions";
import { errorUtils } from "@/lib/errors";
import { formatDate } from "@/lib/datetime";
import { listEmployees, type Employee } from "@/services/employee.service";
import {
  listEmployeeKids,
  type EmployeeKid,
} from "@/services/admin/admin-employee.service";

const { t } = useI18n();
const permissions = usePermissions();
const loading = ref(false);
const dialog = ref(false);
const error = ref<string | null>(null);
const search = ref("");
const hideDismissed = ref(true);
const kids = ref<EmployeeKid[]>([]);
const employees = ref<Employee[]>([]);
const current = ref<EmployeeKid | null>(null);

const headers = computed(() => [
  { title: t("ФИО"), key: "displayName", width: 280 },
  { title: t("День рождения"), key: "birthday", width: 150 },
  { title: t("Возраст (лет)"), key: "age", width: 150 },
  { title: t("Родитель"), key: "parent.name", width: 280 },
  { title: t("Родитель уволен"), key: "parent.active", width: 120 },
]);

const filteredItems = computed(() => {
  const q = search.value.trim().toLowerCase();
  return kids.value.filter((it) => {
    if (hideDismissed.value && !it.parent?.active) {
      return false;
    }
    if (!q) {
      return true;
    }
    return [it.displayName, it.parent?.name]
      .filter(Boolean)
      .join(" ")
      .toLowerCase()
      .includes(q);
  });
});

function rowProps() {
  return permissions.canEditEmployees() ? { class: "cursor-pointer" } : undefined;
}

function onClickRow(_event: Event, payload: unknown): void {
  if (!permissions.canEditEmployees()) {
    return;
  }
  const row = extractRow(payload);
  if (!row) {
    return;
  }
  current.value = row;
  dialog.value = true;
}

function openCreate(): void {
  if (!permissions.canEditEmployees()) {
    return;
  }
  current.value = null;
  dialog.value = true;
}

function extractRow(payload: unknown): EmployeeKid | null {
  if (!payload || typeof payload !== "object") {
    return null;
  }
  if ("item" in payload) {
    const rowItem = (payload as { item?: { raw?: EmployeeKid } | EmployeeKid }).item;
    if (!rowItem) {
      return null;
    }
    if (typeof rowItem === "object" && "raw" in rowItem) {
      const rawRow = rowItem.raw;
      return isEmployeeKid(rawRow) ? rawRow : null;
    }
    return isEmployeeKid(rowItem) ? rowItem : null;
  }
  return isEmployeeKid(payload) ? payload : null;
}

function isEmployeeKid(value: unknown): value is EmployeeKid {
  if (!value || typeof value !== "object") {
    return false;
  }
  return "id" in value && "displayName" in value && "parent" in value;
}

async function load(): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    const [allKids, allEmployees] = await Promise.all([listEmployeeKids(), listEmployees()]);
    kids.value = allKids;
    employees.value = allEmployees;
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

load().catch(() => undefined);
</script>

<style scoped>
.kids-search-field {
  width: 340px;
  max-width: 100%;
}
</style>
